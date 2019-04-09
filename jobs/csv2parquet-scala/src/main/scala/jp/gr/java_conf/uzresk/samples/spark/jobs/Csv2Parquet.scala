package jp.gr.java_conf.uzresk.samples.spark.jobs

import org.apache.spark.sql.{SaveMode, SparkSession}

object Csv2Parquet {
  def main(args: Array[String]) {

    val input = args(0)
    val output = args(1)

    val spark = SparkSession.builder.appName("Csv2ParquetJavaAppliation").getOrCreate
    val df = spark.read
      .option("header", "true")
      .option("inferSchema", "true")
      .csv(input)
    df.write.mode(SaveMode.Overwrite).parquet(output)
  }
}
