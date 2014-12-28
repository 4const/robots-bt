name := "robots-bt"

version := "1.0"

scalaVersion := "2.10.3"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-contrib" % "2.3.8",
  "com.twitter" %% "finatra" % "1.5.3",
  "org.scream3r" % "jssc" % "2.8.0",
  "junit" % "junit" % "4.11" % "test"
)

resolvers +=
  "Twitter" at "http://maven.twttr.com"
