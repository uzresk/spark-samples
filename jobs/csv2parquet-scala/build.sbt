name := "csv2parquet-scala"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.11.11"

libraryDependencies += "org.apache.spark" %% "spark-core" % "2.4.0"
libraryDependencies += "org.apache.spark" %% "spark-sql" % "2.4.0"

assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)
