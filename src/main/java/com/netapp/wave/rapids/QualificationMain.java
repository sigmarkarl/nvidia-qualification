package com.netapp.wave.rapids;

import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import org.apache.spark.sql.SparkSession;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class QualificationMain {
    void runContentType(String contentType, String fileUrl) throws IOException, InterruptedException {
        var pb = new ProcessBuilder("aws","s3","cp","--content-type","'"+contentType+"'","--acl","public-read",fileUrl,fileUrl,"--metadata-directive","REPLACE","--cache-control","no-cache");
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);
        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        var process = pb.start();
        process.getInputStream().transferTo(System.out);
        process.waitFor();
    }

    void moveIndex(String contentType, String fileUrl, String path, URI bucketURL) throws IOException, InterruptedException {
        var aws1 = new ProcessBuilder("aws","s3","cp",fileUrl,"-");
        aws1.redirectError(ProcessBuilder.Redirect.INHERIT);
        var sed = new ProcessBuilder("sed","s/\\.\\.\\//"+path.replace("/","\\/")+"/");
        sed.redirectError(ProcessBuilder.Redirect.INHERIT);
        var aws2 = new ProcessBuilder("aws","s3","cp","--content-type","'"+contentType+"'","--acl","public-read","-",bucketURL.resolve("index.html").toString());
        aws2.redirectError(ProcessBuilder.Redirect.INHERIT);
        aws2.redirectOutput(ProcessBuilder.Redirect.INHERIT);

        var plist = ProcessBuilder.startPipeline(Arrays.asList(aws1, sed, aws2));
        var process = plist.get(plist.size()-1);
        process.getInputStream().transferTo(System.out);
        process.waitFor();
    }

    List<URI> getFileList(URI url, URI bucketUrl) throws IOException {
        var pb = new ProcessBuilder("aws","s3","ls","--recursive",url.toString());
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);
        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        var process = pb.start();
        try (var br = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            return br.lines().map(s -> s.split("[ ]+")).map(s -> s[3]).map(bucketUrl::resolve).toList();
        }
    }

    URI getBaseBucketUrl(URI url) {
        return URI.create(url.getScheme()+"://"+url.getHost()+"/");
    }

    void init(URI out) throws IOException {
        var url = out.resolve("rapids_4_spark_qualification_output/ui/");
        var bucketUrl = getBaseBucketUrl(out);
        var fileList = getFileList(url, bucketUrl);
        fileList.parallelStream().forEach(htmlFile -> {
            var htmlFileName = htmlFile.toString();
            try {
                /*if (htmlFileName.endsWith("index.html")) {
                    moveIndex(es, "text/html", htmlFileName, path, bucketUrl);
                } else */if (htmlFileName.endsWith(".html")) {
                    runContentType("text/html", htmlFileName);
                } else if (htmlFileName.endsWith(".css")) {
                    runContentType("text/css", htmlFileName);
                } else if (htmlFileName.endsWith(".js")) {
                    runContentType("text/javascript", htmlFileName);
                }
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    URI getOutputDir(String[] args) {
        var i = Arrays.asList(args).indexOf("--output-directory");
        var s3 = args[i+1].replace("s3a://","s3://");
        if (!s3.endsWith("/")) s3 += "/";
        return URI.create(s3);
    }

    int getUploadDirIndex(List<String> largs) {
        return largs.indexOf("--upload-directory");
    }

    void fixContentType(String[] args) throws IOException {
        var out = getOutputDir(args);
        init(out);
    }

    void syncAwsCli(String[] args) throws IOException, InterruptedException {
        var url = getOutputDir(args);
        var bucketUrl = getBaseBucketUrl(url);
        var pb = new ProcessBuilder("aws","s3","sync","--acl","public-read","--delete","rapids_4_spark_qualification_output",bucketUrl.toString());
        pb.inheritIO();
        var process = pb.start();
        process.waitFor();
    }

    void syncAws(String uploadDir) {
        var uri = URI.create(uploadDir);
        var transferManager = TransferManagerBuilder.defaultTransferManager();
        transferManager.uploadDirectory(uri.getHost(), uri.getPath().substring(1), Path.of("rapids_4_spark_qualification_output").toFile(), true);
    }

    public static void main(String[] args) {
        try (var spark = SparkSession.builder().getOrCreate()) {
            var largs = new ArrayList<>(Arrays.asList(args));
            var qm = new QualificationMain();
            var uploadDirIndex = qm.getUploadDirIndex(largs);
            if (uploadDirIndex >= 0) {
                largs.remove(uploadDirIndex);
                var uploadDir = largs.remove(uploadDirIndex);
                com.nvidia.spark.rapids.tool.qualification.QualificationMain.main(largs.toArray(String[]::new));
                qm.syncAws(uploadDir);
            } else {
                com.nvidia.spark.rapids.tool.qualification.QualificationMain.main(args);
            }
        } finally {
            System.exit(0);
        }
    }
}
