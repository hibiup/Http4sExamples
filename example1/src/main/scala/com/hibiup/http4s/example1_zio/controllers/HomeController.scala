package com.hibiup.http4s.example1_zio.controllers

import java.util.UUID

import cats.implicits._
import org.http4s.HttpRoutes
import io.circe.syntax._
import org.http4s.dsl.Http4sDsl
import zio.Task
import zio.interop.catz._

import scala.concurrent.Future

final case class Tweet(value: Option[UUID], str: String)

object HomeController {
    val dsl = new Http4sDsl[Task]{}
    import dsl._

    val homeRoutes: HttpRoutes[Task] = HttpRoutes.of[Task] {
        case GET -> Root / "async"/  name => {
            import io.circe.generic.auto._
            val json = Tweet(UUID.randomUUID().some, s"Hello, $name").asJson

            import org.http4s.circe.CirceEntityEncoder._
            Ok(Task.fromFuture(implicit ec => Future{
                json
            }))
        }
    }
}
