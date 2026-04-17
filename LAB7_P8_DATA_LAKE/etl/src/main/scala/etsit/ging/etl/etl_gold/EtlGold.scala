package etsit.ging.etl.etl_gold

// https://medium.com/expedia-group-tech/apache-spark-structured-streaming-operations-5-of-6-40d907866fa7

import org.apache.log4j.Level
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._

object EtlGold {
  def main(args: Array[String]): Unit = {

    /** SparkSession builder
      * For using DeltaLake connector, mind the configurations
      */
    val spark = SparkSession
      .builder()
      .appName("ETLGold")
      .master("local[*]")
      .config("spark.sql.streaming.forceDeleteTempCheckpointLocation", "true")
      .config("spark.sql.extensions", "org.apache.iceberg.spark.extensions.IcebergSparkSessionExtensions")
      .config("spark.sql.catalog.spark_catalog", "org.apache.iceberg.spark.SparkSessionCatalog")
      .config("spark.sql.catalog.spark_catalog.type", "hadoop")
      .config("spark.sql.catalog.spark_catalog.warehouse", "../data/iceberg_warehouse")
      .getOrCreate()

    import spark.implicits._

    spark.sparkContext.setLogLevel(Level.WARN.toString)

    val df = spark.readStream
      .format("iceberg")
      .load("spark_catalog.bronze.iss_historical")

    val df_max_value = df
      .withColumn(
        "timestamp_as_timestamp",
        to_timestamp(from_unixtime($"timestamp"))
      )
      .withWatermark("timestamp_as_timestamp", "10 seconds")
      .groupBy(
        window($"timestamp_as_timestamp", "5 seconds")
      )
      .agg(
        max($"altitude").as("max_altitude"),
        min($"altitude").as("min_altitude")
      )
      .select(
        $"window.start".as("start"),
        $"window.end".as("end"),
        $"max_altitude",
        $"min_altitude"
      )

    val console_stream = df_max_value.writeStream
      .format("console")
      .start

    val iceberg_stream = df_max_value.writeStream
      .format("iceberg")
      .outputMode("append")
      .option("checkpointLocation", "/tmp/iceberg/gold/_checkpoints/")
      .toTable("spark_catalog.gold.iss_aggregation")

    console_stream.awaitTermination
    iceberg_stream.awaitTermination
  }
}
