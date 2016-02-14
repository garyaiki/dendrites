
lazy val commonSettings = Seq(
	organization := "org.gs",
	version := "0.1.0",
	scalaVersion := "2.11.7"
)
lazy val akkaV = "2.3.14"
lazy val scalaTestV = "2.2.5"
lazy val algebirdV = "0.11.0"
lazy val akkaHttpV = "2.0.1"
lazy val root = (project in file(".")).
  configs(IntegrationTest).
	settings(commonSettings: _*).
	settings(Defaults.itSettings: _*).
	settings(
		name := "dendrites",
		libraryDependencies ++= Seq(
			"org.scalatest" % "scalatest_2.11" % scalaTestV % "it,test",
		    "junit" % "junit" % "4.11" % "it,test",
		    "org.scala-lang" % "scala-reflect" % scalaVersion.value % "provided",
		    "com.typesafe.akka" %% "akka-actor" % akkaV,
		    "ch.qos.logback" % "logback-classic" % "1.1.3",
		    "com.typesafe.akka" %% "akka-slf4j" % akkaV,
		    "com.typesafe.akka" %% "akka-remote" % akkaV,
		    "com.typesafe.akka" %% "akka-cluster" % akkaV,
		    "com.typesafe.akka" %% "akka-agent" % akkaV, 
		    "com.typesafe.akka" %% "akka-testkit" % akkaV % "it,test",
		    "com.typesafe.akka" %% "akka-contrib" % akkaV,
		    "com.typesafe.akka" %% "akka-persistence-experimental" % akkaV,
		    "com.typesafe.akka" % "akka-stream-experimental_2.11" % "2.0.1",
		    "com.typesafe.akka" % "akka-http-core-experimental_2.11" % "2.0.1",
		    "com.typesafe.akka" % "akka-http-experimental_2.11" % "2.0.1",
		    "com.typesafe.akka" % "akka-stream-testkit-experimental_2.11" % "2.0.1",
		    "com.typesafe.akka" % "akka-http-spray-json-experimental_2.11" % "2.0.1",
		    "com.typesafe.akka" % "akka-http-testkit-experimental_2.11" % "2.0.1",
		    "com.twitter" % "algebird_2.11" % algebirdV,
		    "com.twitter" % "algebird-core_2.11" % algebirdV,
		    "com.twitter" % "algebird-test_2.11" % algebirdV,
		    "io.spray" %%  "spray-json" % "1.3.2",
		    "commons-io" % "commons-io" % "2.4" % "it,test",
			"com.github.nscala-time" %% "nscala-time" % "2.4.0",
			"joda-time" % "joda-time" % "2.9",
			"org.joda" % "joda-convert" % "1.8",
			"com.chuusai" %% "shapeless" % "2.2.5",
			"org.apache.kafka" % "kafka-clients" % "0.9.0.0",
			"com.datastax.cassandra" % "cassandra-driver-core" % "3.0.0-rc1",
			"com.datastax.cassandra" % "cassandra-driver-mapping" % "3.0.0-rc1",
			"com.datastax.cassandra" % "cassandra-driver-extras" % "3.0.0-rc1",
			"org.apache.avro" % "avro" % "1.8.0"
		),
		javacOptions ++= Seq("-source", "1.8", "-target", "1.8"),
		scalacOptions ++= Seq("-encoding", "UTF-8", "-deprecation", "-feature"),
		resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
		resolvers += "spray repo" at "http://repo.spray.io",
		resolvers += "ivy2 cache" at "file://"+Path.userHome+"/.ivy2/cache",
		javaOptions += "-Xmx500M",
		scalastyleFailOnError := false,
		(scalastyleConfig in Test) := baseDirectory.value / "scalastyle-test-config.xml"                                                                                                                  
	).
	settings(site.settings : _*)










