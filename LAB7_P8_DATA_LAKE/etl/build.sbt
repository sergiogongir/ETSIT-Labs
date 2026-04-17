ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.13"

ThisBuild / scalacOptions ++= Seq("-java-output-version", "8")


lazy val root = (project in file("."))
  .settings(
    name := "etl"
  )

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "3.5.1",
  "org.apache.spark" %% "spark-sql" % "3.5.1",
  "org.apache.spark" %% "spark-streaming" % "3.5.1",
  "org.apache.spark" %% "spark-streaming-kafka-0-10" % "3.5.1",
  "org.apache.spark" %% "spark-sql-kafka-0-10" % "3.5.1"
)

libraryDependencies += "org.apache.iceberg" %% "iceberg-spark-runtime-3.5" % "1.5.2"
libraryDependencies += "org.apache.iceberg" % "iceberg-kafka-connect" % "1.5.2"
