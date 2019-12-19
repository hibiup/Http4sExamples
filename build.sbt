name := "http4sexamples"
version := "0.1"
scalaVersion := "2.13.1"

// DEPENDENCIES
lazy val logging ={
    val logbackV = "1.2.3"
    val scalaLoggingV = "3.9.2"
    Seq(
        "ch.qos.logback" % "logback-classic" % logbackV,
        "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingV
    )
}

lazy val testing = Seq(
    "org.scalatest" %% "scalatest" % "3.0.8",
    "com.storm-enroute" %% "scalameter" % "0.19"
)

lazy val http4s = {
    val http4sVersion = "0.21.0-M6"
    val circeVersion = "0.12.3"
    Seq(
        "org.http4s" %% "http4s-dsl" % http4sVersion,
        "org.http4s" %% "http4s-blaze-server" % http4sVersion,
        "org.http4s" %% "http4s-blaze-client" % http4sVersion,

        // Json
        "org.http4s" %% "http4s-circe" % http4sVersion,
        // Optional for auto-derivation of JSON codecs
        "io.circe" %% "circe-generic" % circeVersion,
        // Optional for string interpolation to JSON model
        "io.circe" %% "circe-literal" % circeVersion,

        // twirl
        "org.http4s" %% "http4s-twirl" % http4sVersion
    )
}

lazy val akka = {
    val akkaVersion = "0.5.10"
    Seq(
        "com.softwaremill.akka-http-session" %% "core" % akkaVersion
    )
}

lazy val monix = Seq(
    "io.monix" %% "monix" % "3.0.0"
)

// SETTINGS
lazy val settings = Seq(
    scalacOptions ++= compilerOptions,
    resolvers ++= Seq(
        "Typesafe" at "http://repo.typesafe.com/typesafe/releases/",
        "Maven2 Repository" at "https://repo1.maven.org/maven2/"
    )
)

lazy val compilerOptions = Seq(
    "-unchecked",
    "-feature",
    "-language:existentials",
    "-language:higherKinds",
    "-language:implicitConversions",
    "-language:postfixOps",
    "-deprecation",
    "-encoding",
    "utf8",
    "-Ypartial-unification",
    "-Xplugin-require:macroparadise",
)

lazy val http4sExamples = project.in(file(".")).aggregate(
    Examples
)

lazy val Examples = project.in(file("example1")).settings(
    settings,
    name:="example1",
    libraryDependencies ++= testing ++ logging ++ http4s ++ akka ++ monix,
    //addCompilerPlugin("org.scalameta" %% "paradise" % "3.0.0-M11" cross CrossVersion.full),
    addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.4")
)
