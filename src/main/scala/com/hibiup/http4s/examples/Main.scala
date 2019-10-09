package com.hibiup.http4s.examples

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._

object Main extends IOApp {
  def run(args: List[String]) =
    QquickServer.stream[IO].compile.drain.as(ExitCode.Success)
}