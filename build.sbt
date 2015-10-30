name := "AkkaAlgebird"

version := "0.0.1"

scalaVersion := "2.11.7"

scalacOptions ++= Seq("-encoding", "UTF-8", "-deprecation", "-feature")

site.settings

site.includeScaladoc()

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "spray repo" at "http://repo.spray.io"

libraryDependencies ++= {
  val akkaV = "2.3.14"
  val scalaTestV = "2.2.5"
  val algebirdV = "0.11.0"
  val akkaHttpV = "1.0"
  Seq("org.scalatest" % "scalatest_2.11" % scalaTestV % "test",
    "junit" % "junit" % "4.11" % "test",
    "org.scala-lang" % "scala-reflect" % scalaVersion.value % "provided",
    "com.typesafe.akka" %% "akka-actor" % akkaV,
    "ch.qos.logback" % "logback-classic" % "1.1.3",
    "com.typesafe.akka" %% "akka-slf4j" % akkaV,
    "com.typesafe.akka" %% "akka-remote" % akkaV,
    "com.typesafe.akka" %% "akka-cluster" % akkaV,
    "com.typesafe.akka" %% "akka-agent" % akkaV, 
    "com.typesafe.akka" %% "akka-testkit" % akkaV % "test",
    "com.typesafe.akka" %% "akka-contrib" % akkaV,
    "com.typesafe.akka" %% "akka-persistence-experimental" % akkaV,
    "com.typesafe.akka" %% "akka-stream-experimental" % akkaHttpV,
    "com.typesafe.akka" %% "akka-stream-testkit-experimental" % akkaHttpV,
    "com.typesafe.akka" %% "akka-http-core-experimental" % akkaHttpV,
    "com.typesafe.akka" %% "akka-http-experimental" % akkaHttpV,
    "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaHttpV,
    "com.typesafe.akka" %% "akka-http-testkit-experimental" % akkaHttpV,
    "com.twitter" % "algebird_2.11" % algebirdV,
    "com.twitter" % "algebird-core_2.11" % algebirdV,
    "com.twitter" % "algebird-test_2.11" % algebirdV,
    "io.spray" %%  "spray-json" % "1.3.2",
    "commons-io" % "commons-io" % "2.4" % "test",
	"com.github.nscala-time" %% "nscala-time" % "2.4.0",
	"joda-time" % "joda-time" % "2.9",
	"org.joda" % "joda-convert" % "1.8",
	"com.chuusai" %% "shapeless" % "2.2.5"
  )
}

scalastyleFailOnError := false

(scalastyleConfig in Test) := baseDirectory.value / "scalastyle-test-config.xml"
