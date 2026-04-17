package etsit.ging.etl.etl_bronze

import org.apache.log4j.Level
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{col, from_json}
import org.apache.spark.sql.types._

object EtlBronze {
  def main(args: Array[String]): Unit = {

    /** SparkSession builder
      * For using DeltaLake connector, mind the configurations
      */
    val spark = SparkSession
      .builder()
      .appName("ETLBronze")
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
      .format("kafka")
      .options(
        Map(
          "subscribe" -> "iss",
          "kafka.bootstrap.servers" -> "localhost:9092", 
          "failOnDataLoss" -> "false"
        )
      )
      .load

    val schema = StructType(
      List(
        StructField("name", StringType, nullable = false),
        StructField("id", IntegerType, nullable = false),
        StructField("latitude", FloatType, nullable = false),
        StructField("longitude", FloatType, nullable = false),
        StructField("altitude", FloatType, nullable = false),
        StructField("velocity", FloatType, nullable = false),
        StructField("visibility", StringType, nullable = false),
        StructField("footprint", FloatType, nullable = false),
        StructField("timestamp", IntegerType, nullable = false),
        StructField("daynum", FloatType, nullable = false),
        StructField("solar_lat", FloatType, nullable = false),
        StructField("solar_lon", FloatType, nullable = false),
        StructField("units", StringType, nullable = false)
      )
    )

    val df_cast = df
      .select(
        $"value".cast("string").as("raw_data")
      )
      .withColumn("data", from_json(col("raw_data"), schema))
      .select(
        $"data.name".as("name"),
        $"data.latitude".as("latitude"),
        $"data.longitude".as("longitude"),
        $"data.altitude".as("altitude"),
        $"data.velocity".as("velocity"),
        $"data.visibility".as("visibility"),
        $"data.footprint".as("footprint"),
        $"data.timestamp".as("timestamp"),
        $"data.daynum".as("daynum"),
        $"data.solar_lat".as("solar_lat"),
        $"data.solar_lon".as("solar_lon"),
        $"data.units".as("units")
      )

    val console_stream = df_cast.writeStream
      .format("console")
      .start

    val iceberg_stream = df_cast.writeStream
      .format("iceberg")
      .outputMode("append")
      .option("checkpointLocation", "/tmp/iceberg/bronze/_checkpoints/")
      .toTable("spark_catalog.bronze.iss_historical")

    console_stream.awaitTermination
    iceberg_stream.awaitTermination

  }
}
