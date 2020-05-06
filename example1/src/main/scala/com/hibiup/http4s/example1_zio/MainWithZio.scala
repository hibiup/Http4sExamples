package com.hibiup.http4s.example1_zio

import cats.data.Kleisli
import cats.effect.ExitCode
import org.http4s.implicits._
import com.hibiup.http4s.example1_zio.controllers.HomeController
import org.http4s.{Request, Response}
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import zio.{Task, ZIO}
import zio.interop.catz._
import zio.interop.catz.implicits._

object MainWithZio extends CatsApp{
    val routes: Kleisli[Task, Request[Task], Response[Task]] =
        Router(
            "/" -> HomeController.homeRoutes
        )
          .orNotFound

    def run(args:List[String]): ZIO[zio.ZEnv, Nothing, Int] = {
        BlazeServerBuilder[Task]
          .bindHttp(9000, "0.0.0.0")
          .enableHttp2(true)
          .withHttpApp(routes)
          .serve
          .compile[Task, Task, ExitCode]
          .drain
          .fold(_ => 1, _ => 0)
    }
}
