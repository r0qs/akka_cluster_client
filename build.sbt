name := "AkkaClusterClient"
version := "0.1-SNAPSHOT"
scalaVersion := "2.11.8"
val akkaVersion = "2.4.10"

scalacOptions in Global ++= Seq(
  "-deprecation",
  "-unchecked",
  "-encoding", "UTF-8",
  "-feature",
  "-Xlint",
  "-Xverify",
  "-Xfuture",
  "-Yinline",
  "-Yclosure-elim",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Ywarn-unused-import",
  "-language:_",
  "-target:jvm-1.8"
)

// Debug
javaOptions in run ++= Seq("-Xms256M", "-Xmx256M", "-Xss1M", "-XX:+CMSClassUnloadingEnabled", "-XX:+PrintGCDetails", "-XX:+PrintGCTimeStamps", "-XX:-HeapDumpOnOutOfMemoryError", "-XX:+UseG1GC", "-XX:MaxGCPauseMillis=3000")
//fork in run := true

javacOptions ++= Seq(
  "-source", "1.8",
  "-target", "1.8",
  "-Xlint:unchecked",
  "-Xlint:deprecation"
)
resolvers ++= Seq(
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
  "Sonatype OSS Releases" at "https://oss.sonatype.org/content/repositories/releases/",
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"
)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "com.typesafe.akka" %% "akka-contrib" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "com.github.romix.akka" %% "akka-kryo-serialization" % "0.4.1",
  "ch.qos.logback" % "logback-classic" % "1.1.3",
  "org.scalatest" %% "scalatest" % "2.2.1" % "test"
)

lazy val cfabcast = ProjectRef(uri("https://github.com/r0qs/cfabcast.git#master"), "cfabcast")
lazy val root = (project in file(".")).dependsOn(cfabcast)
