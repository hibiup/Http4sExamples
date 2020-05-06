package com.hibiup.http4s.example4.staticresources

import cats.effect.{Blocker, ContextShift, IO}
import org.http4s.{HttpRoutes, Request, Response, StaticFile}
import org.http4s.dsl.io._

import play.twirl.api.Html

object CustomStaticResourceController {
    def static(path: String, blocker: Blocker, request: Request[IO])(implicit cs:ContextShift[IO]): IO[Response[IO]] =
        StaticFile.fromResource("/mixed/" + path, blocker, Some(request)).getOrElseF(NotFound())

    def routes(blocker:Blocker)(implicit cs:ContextShift[IO]): HttpRoutes[IO] = HttpRoutes.of[IO] {
        /**
         * 访问静态资源
         */
        case request @ GET -> Root / "res" / url if List(".js", ".html").exists(ext => url.endsWith(ext) ) =>
            static(url, blocker, request)
    }
}
