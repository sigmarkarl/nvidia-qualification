package com.netapp.wave.rapids;

import org.apache.spark.sql.SparkSession;

public class ProduceDemoEventLogs {
    public static void main(String[] args) {
        var sb = SparkSession.builder()
                .master("local[*]")
                .config("spark.eventLog.enabled", true)
                .config("spark.eventLog.dir", "s3a://simmi-test-bucket/eventLogs")
                .config("spark.hadoop.fs.s3a.impl", "org.apache.hadoop.fs.s3a.S3AFileSystem")
                .config("spark.hadoop.com.amazonaws.services.s3.enableV4", "true")
                .config("spark.hadoop.fs.s3a.aws.credentials.provider","com.amazonaws.auth.EnvironmentVariableCredentialsProvider");
                //.config("spark.hadoop.fs.s3a.aws.credentials.provider","org.apache.hadoop.fs.s3a.AnonymousAWSCredentialsProvider")
                //.config("spark.hadoop.fs.s3a.aws.credentials.provider", "org.apache.hadoop.fs.s3a.SimpleAWSCredentialsProvider")
                //.config("spark.hadoop.fs.s3a.access.key", "***")
                //.config("spark.hadoop.fs.s3a.secret.key", "***");
                //.config("spark.hadoop.fs.s3a.endpoint","localhost:4566")
                //.config("spark.hadoop.fs.s3a.connection.ssl.enabled","false")
                //.config("spark.hadoop.fs.s3a.path.style.access", true)
                //.config("spark.hadoop.fs.s3a.bucket.all.committer.magic.enabled", true)
                //.config("spark.hadoop.fs.s3a.committer.staging.conflict-mode","replace")
                //.config("spark.hadoop.fs.s3a.committer.name","directory")
                //.config("spark.hadoop.fs.s3a.committer.staging.tmp.path","s3a://simmi-test-bucket/tmp")
                //.config("spark.hadoop.fs.s3a.directory.marker.retention", "keep")
                //.config("spark.hadoop.fs.s3a.committer.name", "partitioned")
                //.config("spark.hadoop.fs.s3a.committer.staging.conflict-mode", "replace");
        try (var spark = sb.getOrCreate()) {
            var df = spark.range(100000);
            var count = df.where("id > 5000 and id < 10000").count();
            System.out.println("count: " + count);
        }
    }
}
