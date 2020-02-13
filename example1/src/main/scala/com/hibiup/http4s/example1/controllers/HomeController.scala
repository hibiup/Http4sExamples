package com.hibiup.http4s.example1.controllers

import java.util.UUID

import cats.implicits._
import cats.effect._
import com.hibiup.http4s.example1.common.models.Tweet
import org.http4s.{HttpRoutes, ResponseCookie}
import org.http4s.dsl.io._
import io.circe.syntax._

object HomeController {
    // HttpRoutes[IO] 就是 Kleisli[IO, Request[IO], Response[IO]] 的别名
    val homeRoutes: HttpRoutes[IO] = HttpRoutes.of[IO] {
        /**
         * 普通 Http 服务
         */
        case GET -> Root / "hello" / name => {
            /**
             * 1) Tweet -> Json 需要 Encoder. 或 io.circe.generic.auto._ (see models.jsonEncoder)
             *
             * io.circe.generic.auto 提供 case class -> Json 的自动转换。 .asJson 语法由 io.circe.syntax 提供
             *
             * 同样，如果我们需要从客户端提交一个 Json，也需要相同的 EntityEncoder 将 Json 装入 Request body(参见 Test case)
             */
            import io.circe.generic.auto._
            val json = Tweet(UUID.randomUUID().some, s"Hello, $name").asJson

            /**
             * 2) 需要 Json -> Entity 需要 EntityEncoder 或 org.http4s.circe._
             */
            import org.http4s.circe._
            Ok(json/*, Header("Content-Type", "Application/Json")*/)
              .map(_.addCookie(ResponseCookie("foo", "bar")))
        }

        /**
         * Async http
         */
        case GET -> Root / "async"/  name => {
            import io.circe.generic.auto._
            import org.http4s.circe.CirceEntityEncoder._

            /**
             * IO.fromFuture 支持异步 Http
             */
            // 异步控制
            import scala.concurrent.Future
            import monix.execution.Scheduler.Implicits.global
            implicit val cs: ContextShift[IO] = IO.contextShift(global)

            val json = Tweet(UUID.randomUUID().some, s"Hello, $name").asJson
            Ok(IO.fromFuture(IO(Future{
                json
            })))
        }
    }
}
