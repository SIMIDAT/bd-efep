lazy val root = (project in file(".")).
  settings(
    name := "moea-spark",
    version := "1.1",
    scalaVersion := "2.10.6",
    mainClass in Compile := Some("EFEP_MOEA")
  )


val sparkVersion = "2.2.0"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % sparkVersion % "provided",
  "org.apache.spark" %% "spark-sql" % sparkVersion % "provided"
)


run in Compile := Defaults.runTask(fullClasspath in Compile, mainClass in (Compile, run), runner in (Compile, run)).evaluated
