package com.hibiup.http4s.example5.controllers

import scala.concurrent.duration._

import cats.effect.{ContextShift, IO, Timer}
import fs2.Stream
import org.http4s.HttpRoutes
import org.http4s.dsl.io._

/**
 * Straem 在 http4s 中是一个很重要的概念，http4s 总是将返回的内容作为 stream 看待，因此当我们通过 http4s 的客户端获取 response.body
 * 的时候也总是会得到一个 Stream 对象。
 *
 * 这个例子我们产生一个持续的内容发送给客户端，然后在测试案例中读取并处理内容。
 */
object StreamController {
    def stream(implicit timer:Timer[IO], cs:ContextShift[IO]): HttpRoutes[IO] = HttpRoutes.of[IO] {
        case GET -> Root / "nano" =>
            /**
             * Stream.awakeEvery[IO](1.second) 每间隔一定时间发射出一个 duration time. 这里也就是持续发射出一个
             * nano seconds 数字
             */
            Ok(Stream.awakeEvery[IO](1.second).map((nano: Duration) =>
                s"""{"duration": "${nano._1} ${nano._2.toString}"}""".stripMargin))
    }
}
