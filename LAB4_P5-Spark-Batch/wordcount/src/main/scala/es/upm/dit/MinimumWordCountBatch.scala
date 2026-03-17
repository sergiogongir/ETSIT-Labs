package es.upm.dit

import org.apache.spark.sql.{SparkSession, SaveMode}

object MinimumWordCountBatch{
  final val BASE_PATH = "../"
  def main(args: Array[String]) {

    val spark = SparkSession
    .builder
    .appName("MinimumWordCountBatch")
    .master("local[*]")
    .getOrCreate()
    
    spark.conf.set("mapreduce.fileoutputcommitter.marksuccessfuljobs", "false")

    import spark.implicits._


    val folderPath = BASE_PATH + "/books"
    val resultFolder = BASE_PATH + "/result4"


    val textFiles = spark
    .sparkContext
    .wholeTextFiles(folderPath)
    .map(_._2)


    val wordCounts = textFiles.flatMap(line => line.split("\\s+"))
                          .map(word => (
                            word
                              .toLowerCase()
                              .replaceAll("[^a-zA-Z0-9áàâäéèêëíìîïóòôöúùûüñÁÀÂÄÉÈÊËÍÌÎÏÓÒÔÖÚÙÛÜÑ]", ""), 
                            1))
                          .reduceByKey(_ + _)

    val df =  wordCounts.filter(_._2 > 100)

    val sum = if (df.isEmpty()) 0 else df.map(_._2).reduce(_ + _)

    val resultDF = Seq(("Suma Total", sum)).toDF("Concepto", "Valor")

    resultDF
        .coalesce(1) //in a single file, not recommended for huge csv files
        .write
        .mode(SaveMode.Overwrite)
        .option("header", "true")
        .format("csv")
        .save(resultFolder)

    spark.stop()
  }
}