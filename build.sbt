lazy val root = (project in file(".")).
  settings(
    name := "moea-spark",
    version := "1.0",
    scalaVersion := "2.10.6",
    mainClass in Compile := Some("EFEP_MOEA")
  )

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "2.2.0" % "provided",
  "org.apache.spark" %% "spark-sql" % "2.2.0" % "provided"
)


run in Compile := Defaults.runTask(fullClasspath in Compile, mainClass in (Compile, run), runner in (Compile, run)).evaluated