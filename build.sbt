name := "BD-EFEP"
version := "1.0"
scalaVersion := "2.10.6"
mainClass in Compile := Some("EFEP_MOEA")
val sparkVersion = "2.2.0"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % sparkVersion,
  "org.apache.spark" %% "spark-sql" % sparkVersion
)
