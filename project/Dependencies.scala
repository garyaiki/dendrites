import sbt._

object Dependencies {
  lazy val akkaVersion = "2.5.6"
  lazy val akkaHttpVersion = "10.0.10"
  lazy val algebirdVersion = "0.13.3"
  lazy val cassandraDriverVersion = "3.3.2"

  val logbackClassic = "ch.qos.logback" % "logback-classic" % "1.2.3"
  val akkaActor = "com.typesafe.akka" %% "akka-actor" % akkaVersion
  val akkaSlf4j = "com.typesafe.akka" %% "akka-slf4j" % akkaVersion
  val akkaAgent = "com.typesafe.akka" %% "akka-agent" % akkaVersion
  val akkaStream = "com.typesafe.akka" %% "akka-stream" % akkaVersion
  val akkaHttpCore = "com.typesafe.akka" %% "akka-http-core" % akkaHttpVersion
  val akkaHttp = "com.typesafe.akka" %% "akka-http" % akkaHttpVersion
  val akkaHttpSprayJson = "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion
  val algebra = "org.typelevel" %% "algebra" % "0.7.0"
  val algebirdCore = "com.twitter" %% "algebird-core" % algebirdVersion
  val sprayJson = "io.spray" %% "spray-json" % "1.3.4"
  val shapeless = "com.chuusai" %% "shapeless" % "2.3.2"
  val kafkaClients = "org.apache.kafka" % "kafka-clients" % "1.0.0"
  val cassandraDriverCore = "com.datastax.cassandra" % "cassandra-driver-core" % cassandraDriverVersion
  val cassandraDriverMapping = "com.datastax.cassandra" % "cassandra-driver-mapping" % cassandraDriverVersion
  val cassandraDriverExtras = "com.datastax.cassandra" % "cassandra-driver-extras" % cassandraDriverVersion
  val avro = "org.apache.avro" % "avro" % "1.8.2"
  val avro4sCore = "com.sksamuel.avro4s" %% "avro4s-core" % "1.7.0"
  val guava = "com.google.guava" % "guava" % "23.5-jre"
  val jsr305 = "com.google.code.findbugs" % "jsr305" % "3.0.2"
  val scalaCass = "com.github.thurstonsand" %% "scala-cass" % "2.1.0"

// Projects
  val coreDeps = Seq(logbackClassic, akkaSlf4j, shapeless, guava, jsr305)
  val actorDeps = Seq(akkaActor)
  val agentDeps = Seq(akkaAgent)
  val akkaStreamDeps = Seq(akkaStream)
  val akkaHTTPDeps = Seq(akkaHttpCore, akkaHttp, akkaHttpSprayJson, sprayJson)
  val algebirdDeps = Seq(algebra, algebirdCore)
  val kafkaDeps = Seq(kafkaClients, avro, avro4sCore)
  val cassandraDeps = Seq(cassandraDriverCore, cassandraDriverMapping, cassandraDriverExtras, scalaCass)
}