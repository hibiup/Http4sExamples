package com.hibiup.http4s.example2.controllers

import java.time.LocalDateTime

import cats.effect._
import org.http4s.dsl.io._
import org.http4s.{Header, HttpRoutes}

object OkController {
    // HttpRoutes[IO] 就是 Kleisli[IO, Request[IO], Response[IO]] 的别名
    val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
        case GET -> Root / "ok" =>
            Ok()
    }

    /**
     * 2) 给服务加装一个 middle ware
     */
    import com.hibiup.http4s.example2.middles.MyMiddleFilters.myMiddle
    val wrappedRoutes: HttpRoutes[IO] = myMiddle(routes, Header("Response-time", LocalDateTime.now().toString))
}
