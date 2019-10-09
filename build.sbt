
lazy val http4s = {
    val Http4sVersion = "0.20.8"
    Seq(
        "org.http4s"      %% "http4s-blaze-server" % Http4sVersion,
        "org.http4s"      %% "http4s-blaze-client" % Http4sVersion,
        "org.http4s"      %% "http4s-circe"        % Http4sVersion,
        "org.http4s"      %% "http4s-dsl"          % Http4sVersion
    )
}

lazy val testing = {
    val Specs2Version = "4.1.0"
    Seq(
        "org.specs2"      %% "specs2-core"         % Specs2Version % "test"
    )
}

lazy val logging = {
    val LogbackVersion = "1.2.3"
    Seq(
        "ch.qos.logback"  %  "logback-classic"     % LogbackVersion
    )
}

lazy val circe = {
    val CirceVersion = "0.11.1"
    Seq(
        "io.circe"        %% "circe-generic"       % CirceVersion
    )
}

lazy val root = (project in file("."))
  .settings(
      organization := "com.hibiup.http4s",
      name := "com/hibiup/http4s/examples",
      version := "0.0.1-SNAPSHOT",
      scalaVersion := "2.12.8",
      libraryDependencies ++= http4s ++ testing ++ logging ++ circe,
      addCompilerPlugin("org.typelevel" %% "kind-projector"     % "0.10.3"),
      addCompilerPlugin("com.olegpy"    %% "better-monadic-for" % "0.3.0")
  )

scalacOptions ++= Seq(
    "-deprecation",
    "-encoding", "UTF-8",
    "-language:higherKinds",
    "-language:postfixOps",
    "-feature",
    "-Ypartial-unification",
    "-Xfatal-warnings",
)
