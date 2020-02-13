package com.hibiup.http4s.example5

import cats.implicits._
import cats.effect._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.{Router, Server}
import org.http4s.implicits._

object MainWithIOApp extends IOApp{
    val app: Resource[IO, Server[IO]] =
        BlazeServerBuilder[IO]
              .bindHttp(8080)
              .withHttpApp(
                  Router(
                      "/duration" -> com.hibiup.http4s.example5.controllers.StreamController.stream
                  ).orNotFound)
              .resource

    override def run(args: List[String]): IO[ExitCode] =
        app.use(_ => IO.never).as(ExitCode.Success)
}
