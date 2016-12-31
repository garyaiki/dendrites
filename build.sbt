
val gitHeadCommitSha = taskKey[String]("Determines the current git commit SHA")

gitHeadCommitSha in ThisBuild := Process("git rev-parse HEAD").lines.head

val makeVersionProperties = taskKey[Seq[File]]("Create version.properties file runtime findable")

makeVersionProperties := {
  val propFile = (resourceManaged in Compile).value / "version.properties"
  val content = "version=%s" format (gitHeadCommitSha.value)
  IO.write(propFile, content)
  Seq(propFile)
}

mappings in packageBin in Compile += (baseDirectory.value / "LICENSE.md") -> "dendrites-LICENSE"

lazy val commonSettings = Seq(
	organization := "com.github.garyaiki",
	version := "0.4.2",
	scalaVersion := "2.11.8"
)
lazy val akkaV = "2.4.16"
lazy val akkaHttpV = "10.0.1"
lazy val scalaTestV = "3.0.1"
lazy val algebirdV = "0.12.2"
lazy val cassandraDriverV = "3.1.2"
lazy val root = (project in file(".")).
  configs(IntegrationTest).
	settings(commonSettings: _*).
	settings(Defaults.itSettings: _*).
	settings(
		name := "dendrites",
		libraryDependencies ++= Seq(
			"org.scalatest" %% "scalatest" % scalaTestV % "it,test",
			"org.scala-lang" % "scala-reflect" % scalaVersion.value % "provided",
			"ch.qos.logback" % "logback-classic" % "1.1.8",
			"com.typesafe.akka" %% "akka-actor" % akkaV,
			"com.typesafe.akka" %% "akka-slf4j" % akkaV,
			"com.typesafe.akka" %% "akka-agent" % akkaV, 
			"com.typesafe.akka" %% "akka-testkit" % akkaV % "it,test",
			"com.typesafe.akka" %% "akka-stream" % akkaV,
			"com.typesafe.akka" %% "akka-http-core" % akkaHttpV,
			"com.typesafe.akka" %% "akka-http" % akkaHttpV,
			"com.typesafe.akka" %% "akka-stream-testkit" % akkaV % "it,test",
			"com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpV,
			"com.typesafe.akka" %% "akka-http-testkit" % akkaHttpV % "it,test",
			"com.twitter" %% "algebird" % algebirdV,
			"com.twitter" %% "algebird-core" % algebirdV,
			"io.spray" %% "spray-json" % "1.3.2",
			"commons-io" % "commons-io" % "2.5" % "it,test",
			"com.chuusai" %% "shapeless" % "2.3.2",
			"org.apache.kafka" % "kafka-clients" % "0.10.1.0",
			"com.datastax.cassandra" % "cassandra-driver-core" % cassandraDriverV,
			"com.datastax.cassandra" % "cassandra-driver-mapping" % cassandraDriverV,
			"com.datastax.cassandra" % "cassandra-driver-extras" % cassandraDriverV,
			"org.apache.avro" % "avro" % "1.8.1",
			"com.google.guava" % "guava" % "19.0",
			"com.google.code.findbugs" % "jsr305" % "3.0.1",
			"com.github.thurstonsand" %% "scalacass" % "0.6.11"
		),
		javacOptions ++= Seq("-source", "1.8", "-target", "1.8"),
		scalacOptions ++= Seq("-encoding", "UTF-8", "-deprecation", "-feature"),
		scalacOptions in (Compile, doc) ++= Seq("-doc-root-content", baseDirectory.value+"/root-doc.txt"),
		resolvers += "ivy2 cache" at "file://"+Path.userHome+"/.ivy2/cache",
		resolvers += Resolver.jcenterRepo,
		javaOptions += "-Xmx500M",
		scalastyleFailOnError := false,
		(scalastyleConfig in Test) := baseDirectory.value / "scalastyle-test-config.xml"                                                                                                                  
	).
	settings(site.settings : _*)

