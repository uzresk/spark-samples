package jp.gr.java_conf.uzresk.samples.spark.jobs;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;

public class Csv2Parquet {

    public static void main(String[] args) {

        String input = args[0];
        String output = args[1];

        SparkSession spark = SparkSession.builder().appName("Csv2ParquetJavaApplication").getOrCreate();
        Dataset<Row> df = spark.read().option("header", "true").option("inferSchema", "true").csv(input);
        df.write().mode(SaveMode.Overwrite).parquet(output);
    }

}
