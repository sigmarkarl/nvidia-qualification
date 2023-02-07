package com.netapp.wave.rapids;

import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;

public class QualificationTest {
    public static void main(String[] args) {
        var spark = SparkSession.builder()
                .master("local[*]")
                .config("spark.eventLog.enabled",true)
                .config("spark.eventLog.dir","s3a://mytestbucket/eventLogs")
                .config("spark.hadoop.fs.s3a.impl","org.apache.hadoop.fs.s3a.S3AFileSystem")
                .config("spark.hadoop.com.amazonaws.services.s3.enableV4","true")
                .config("spark.hadoop.fs.s3a.aws.credentials.provider","org.apache.hadoop.fs.s3a.AnonymousAWSCredentialsProvider")
                .config("spark.hadoop.fs.s3a.endpoint","localhost:4566")
                .config("spark.hadoop.fs.s3a.connection.ssl.enabled","false")
                .config("spark.hadoop.fs.s3a.path.style.access","true")
                .config("spark.hadoop.fs.s3a.committer.name","partitioned")
                .config("spark.hadoop.fs.s3a.committer.staging.conflict-mode","replace")
                .getOrCreate();
        var df = spark.range(100000);
        var count = df.where("id > 5000 and id < 10000").count();
        df.write().mode(SaveMode.Overwrite).save("s3a://mytestbucket/test.parquet");
        System.out.println("count: " + count);
        spark.stop();
    }
}
