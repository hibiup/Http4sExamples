package com.hibiup.http4s.example2

import cats.data.OptionT
import cats.effect.IO
import com.typesafe.scalalogging.StrictLogging
import org.http4s.{Method, Request, Response, Status}
import org.scalatest.FlatSpec
import org.http4s.implicits._
import org.http4s.util.CaseInsensitiveString

class OkControllerTest extends FlatSpec with StrictLogging{
    "Middle ware" should "acts like a filter" in {
        // 访问正常的服务
        import com.hibiup.http4s.example2.controllers.OkController.routes
        val getOk = Request[IO](Method.GET, uri"/ok")
        val result = routes.orNotFound.run(getOk).unsafeRunSync()

        assert(result.headers.get(CaseInsensitiveString("Response-time")).isEmpty)

        // 访问加装了 middle ware 的服务
        import com.hibiup.http4s.example2.controllers.OkController.wrappedRoutes
        val wrappedResult = wrappedRoutes.orNotFound.run(getOk).unsafeRunSync()

        val resp = wrappedResult.headers.get(CaseInsensitiveString("Response-time"))
        println(resp)
        assert(resp.isDefined)
    }
}
