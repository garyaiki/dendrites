import Dependencies._
import scala.sys.process.Process

val gitHeadCommitSha = taskKey[String]("Determines the current git commit SHA")

gitHeadCommitSha in ThisBuild := Process("git rev-parse HEAD").lineStream.head

val makeVersionProperties = taskKey[Seq[File]]("Create version.properties file runtime findable")

makeVersionProperties := {
  val propFile = (resourceManaged in Compile).value / "version.properties"
  val content = "version=%s" format (gitHeadCommitSha.value)
  IO.write(propFile, content)
  Seq(propFile)
}

mappings in packageBin in Compile += (baseDirectory.value / "LICENSE.md") -> "dendrites-LICENSE"

scalastyleConfig in Test := file("scalastyle-test-config.xml")

scalastyleConfig in Compile := file("scalastyle-config.xml")

lazy val commonSettings = Seq(
	organization := "com.github.garyaiki",
	version := "0.6.3",
	scalaVersion := "2.12.4",
	crossScalaVersions := Seq("2.12.4", "2.11.11")
)
lazy val akkaV = "2.5.6"
lazy val akkaHttpV = "10.0.10"
lazy val scalaTestV = "3.0.3"
lazy val root = (project in file(".")).
  configs(IntegrationTest).
	settings(commonSettings: _*).
	settings(Defaults.itSettings: _*).
	settings(
		name := "dendrites",
		libraryDependencies ++= coreDeps,
		libraryDependencies ++= actorDeps,
		libraryDependencies ++= agentDeps,
		libraryDependencies ++= akkaStreamDeps,
		libraryDependencies ++= akkaHTTPDeps,
		libraryDependencies ++= algebirdDeps,
		libraryDependencies ++= kafkaDeps,
		libraryDependencies ++= cassandraDeps,
		libraryDependencies ++= Seq(
			"org.scalatest" %% "scalatest" % scalaTestV % "it,test",
			"org.scala-lang" % "scala-reflect" % scalaVersion.value % "provided",
			"com.typesafe.akka" %% "akka-stream-testkit" % akkaV % "it,test",
			"com.typesafe.akka" %% "akka-http-testkit" % akkaHttpV % "it,test",
			"commons-io" % "commons-io" % "2.5" % "it,test"
		),
		javacOptions ++= Seq("-source", "1.8", "-target", "1.8"),
		scalacOptions ++= Seq("-encoding", "UTF-8", "-deprecation", "-feature"),
		scalacOptions in (Compile, doc) ++= Seq("-doc-root-content", baseDirectory.value+"/root-doc.txt"),
		resolvers += "ivy2 cache" at "file://"+Path.userHome+"/.ivy2/cache",
		resolvers += Resolver.jcenterRepo,
		javaOptions += "-Xmx500M",
		scalastyleFailOnError := false
	)


