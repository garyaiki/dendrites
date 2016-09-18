
lazy val commonSettings = Seq(
	organization := "org.gs",
	version := "0.2.1",
	scalaVersion := "2.11.8"
)
lazy val akkaV = "2.4.10"
lazy val scalaTestV = "3.0.0"
lazy val algebirdV = "0.12.1"
lazy val root = (project in file(".")).
  configs(IntegrationTest).
	settings(commonSettings: _*).
	settings(Defaults.itSettings: _*).
	settings(
		name := "dendrites",
		libraryDependencies ++= Seq(
		  "org.scalactic" % "scalactic_2.11" % "3.0.0",
			"org.scalatest" % "scalatest_2.11" % scalaTestV % "it,test",
		   "junit" % "junit" % "4.12" % "it,test",
		   "org.scala-lang" % "scala-reflect" % scalaVersion.value % "provided",
		   "com.typesafe.akka" %% "akka-actor" % "2.4.10",
		   "ch.qos.logback" % "logback-classic" % "1.1.7",
  	   "com.typesafe.akka" %% "akka-slf4j" % "2.4.10",
		   "com.typesafe.akka" %% "akka-agent" % "2.4.10", 
		   "com.typesafe.akka" %% "akka-testkit" % "2.4.10" % "it,test",
		   "com.typesafe.akka" %% "akka-contrib" % "2.4.10",
			 "com.typesafe.akka" % "akka-stream_2.11" % "2.4.10",
			 "com.typesafe.akka" % "akka-http-core_2.11" % "2.4.10",
       "com.typesafe.akka" % "akka-http-experimental_2.11" % "2.4.10",
			 "com.typesafe.akka" % "akka-stream-testkit_2.11" % "2.4.10",
       "com.typesafe.akka" % "akka-http-spray-json-experimental_2.11" % "2.4.10",
			 "com.typesafe.akka" % "akka-http-testkit_2.11" % "2.4.10",
		   "com.twitter" % "algebird_2.11" % algebirdV,
		   "com.twitter" % "algebird-core_2.11" % algebirdV,
		   "com.twitter" % "algebird-test_2.11" % algebirdV,
		   "io.spray" %%  "spray-json" % "1.3.2",
		   "commons-io" % "commons-io" % "2.5" % "it,test",
			 "com.chuusai" %% "shapeless" % "2.3.2",
			 "org.apache.kafka" % "kafka-clients" % "0.10.0.1",
			 "com.datastax.cassandra" % "cassandra-driver-core" % "3.1.0",
			 "com.datastax.cassandra" % "cassandra-driver-mapping" % "3.1.0",
			 "com.datastax.cassandra" % "cassandra-driver-extras" % "3.1.0",
			 "org.apache.avro" % "avro" % "1.8.1",
			 "com.google.guava" % "guava" % "19.0",
			 "com.google.code.findbugs" % "jsr305" % "3.0.1",
			 "com.github.thurstonsand" %% "scalacass" % "0.4.8"
		),
		javacOptions ++= Seq("-source", "1.8", "-target", "1.8"),
		scalacOptions ++= Seq("-encoding", "UTF-8", "-deprecation", "-feature"),
		scalacOptions in (Compile, doc) ++= Seq("-doc-root-content", baseDirectory.value+"/root-doc.txt"),
		resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
		resolvers += "spray repo" at "http://repo.spray.io",
		resolvers += "ivy2 cache" at "file://"+Path.userHome+"/.ivy2/cache",
		resolvers += Resolver.jcenterRepo,
		javaOptions += "-Xmx500M",
		scalastyleFailOnError := false,
		(scalastyleConfig in Test) := baseDirectory.value / "scalastyle-test-config.xml"                                                                                                                  
	).
	settings(site.settings : _*)










