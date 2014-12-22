name := "robots-bt"

version := "1.0"

scalaVersion := "2.11.4"

libraryDependencies += "com.typesafe.akka" % "akka-contrib_2.11" % "2.3.7"

libraryDependencies += "junit" % "junit" % "4.11"

libraryDependencies += "org.scream3r" % "jssc" % "2.8.0"

libraryDependencies ++= Seq(
  "org.scalatra" % "scalatra" % "2.3.0",
  "org.scalatra" % "scalatra-scalate" % "2.3.0",
  "net.databinder.dispatch" %% "dispatch-core" % "0.11.1"
)


libraryDependencies ++= Seq(
  "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016" % "provided" artifacts Artifact("javax.servlet", "jar", "jar"),
  "org.eclipse.jetty" % "jetty-webapp" % "8.1.5.v20120716" % "container"
)