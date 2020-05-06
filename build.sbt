name := "http4sexamples"
version := "0.1"
scalaVersion := "2.13.1"

// DEPENDENCIES
lazy val ver = new {
    val logback = "1.2.3"
    val scalaLogging = "3.9.2"
    val scalatest = "3.1.1"
    val http4s = "0.21.0"
    val monix = "3.2.1"
    val zio = "1.0.0-RC18-2"
    val zioInteropCats = "2.0.0.0-RC13"
    val circe = "0.13.0"
    val akka = "0.5.10"
}

lazy val logging =
    Seq(
        "ch.qos.logback" % "logback-classic" % ver.logback,
        "com.typesafe.scala-logging" %% "scala-logging" % ver.scalaLogging
    )

lazy val testing = Seq(
    "org.scalatest" %% "scalatest" % ver.scalatest,
    "com.storm-enroute" %% "scalameter" % "0.19"
)

lazy val http4s =
    Seq(
        "org.http4s" %% "http4s-dsl" % ver.http4s,
        "org.http4s" %% "http4s-blaze-server" % ver.http4s,
        "org.http4s" %% "http4s-blaze-client" % ver.http4s,

        // Json
        "org.http4s" %% "http4s-circe" % ver.http4s,

        // twirl
        "org.http4s" %% "http4s-twirl" % ver.http4s
    )

lazy val circe = Seq(
    // Optional for auto-derivation of JSON codecs
    "io.circe" %% "circe-generic" % ver.circe,
    // Optional for string interpolation to JSON model
    "io.circe" %% "circe-literal" % ver.circe,
)

lazy val akka =
    Seq(
        "com.softwaremill.akka-http-session" %% "core" % ver.akka
    )

lazy val monix = Seq(
    "io.monix" %% "monix" % ver.monix
)

lazy val zio = Seq(
    "dev.zio" %% "zio" % ver.zio,
    "dev.zio" %% "zio-interop-cats" % ver.zioInteropCats
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
    //"-Xplugin-require:macroparadise",
)

lazy val http4sExamples = project.in(file(".")).aggregate(
    Examples
)

lazy val Examples = project.in(file("example1")).settings(
    settings,
    name:="example1",
    libraryDependencies ++= testing ++ logging ++ http4s ++ circe ++ zio ++ monix,
    //addCompilerPlugin("org.scalameta" %% "paradise" % "3.0.0-M11" cross CrossVersion.full),
    addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.4")
).enablePlugins(SbtTwirl)
