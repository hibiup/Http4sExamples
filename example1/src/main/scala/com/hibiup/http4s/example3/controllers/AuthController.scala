package com.hibiup.http4s.example3.controllers

import cats.implicits._
import cats.effect._
import com.hibiup.http4s.example3.commons.models.User
import org.http4s.{AuthedRoutes, HttpRoutes}
import org.http4s.dsl.io._
import org.http4s.server.Router

object AuthController {
    import com.hibiup.http4s.example3.middles.AuthFilter.authMiddleware
    import com.hibiup.http4s.example3.middles.AuthFilter.authOrLoginMiddleware
    /**
     * 4）被过虑的 route 返回 AuthRoutes[T, IO] 数据类型。
     */
    private val enginshRoutes: AuthedRoutes[User, IO] =
        AuthedRoutes.of {
            /**
             * 4.1）可以在 route 中通过 `as` 获得认证实体。
             */
            case GET -> Root / "welcome" as user => Ok(s"Welcome, ${user.username}")
        }

    /**
     * 3) 将 authentication middleware 应用于需过虑的 route
     */
    val authedRoutes: HttpRoutes[IO] = authMiddleware(enginshRoutes)

    /**
     * 一个非认证的普通 route
     */
    val frenchRoutes: HttpRoutes[IO] =
        HttpRoutes.of {
            case GET -> Root / "bonjour" => Ok(s"Bonjour")
        }

    /**
     * 5) AuthedRoutes 和 HttpRoutes 可以合并在一起
     */
    val partialSecuredRoutes: HttpRoutes[IO] = {
        Router (
            "/english" -> authedRoutes,   // 需要认证
            "/french" -> frenchRoutes,    // 不需要认证
            "/try" -> authOrLoginMiddleware(enginshRoutes)  // 如果没有认证那么从定向到登陆页面
        )
    }

    // 5.1) 但是如果通过 <+> 合并，会导致所有 API 都需要认证：
    val allSecuredRoutes = frenchRoutes <+> authedRoutes
}
