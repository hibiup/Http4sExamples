package com.hibiup.http4s.example3

import org.http4s.{Method, Request}
import cats.effect.IO
import com.typesafe.scalalogging.StrictLogging
import org.http4s._
import org.scalatest.FlatSpec
import org.http4s.implicits._
import org.http4s.util.CaseInsensitiveString

class AuthControllerTest extends FlatSpec with StrictLogging{
    "Auth controller " should "secure" in {
        import com.hibiup.http4s.example3.controllers.AuthController.partialSecuredRoutes

        // 不带认证信息访问加密地址
        val trySecure = Request[IO](Method.GET, uri"/english/welcome")
        val rejected = partialSecuredRoutes.orNotFound.run(trySecure).unsafeRunSync()
        assert(rejected.status == Status(401))

        // 不带认证信息访问加密地址
        val tryLogin = Request[IO](Method.GET, uri"/try/welcome")
        val redirected = partialSecuredRoutes.orNotFound.run(tryLogin).unsafeRunSync()
        assert(redirected.status == Status(403))
        //assert(redirected.headers.get(CaseInsensitiveString("Location")).isDefined)

        // 带上认证信息访问加密地址
        val getSecure = Request[IO](Method.GET, uri"/english/welcome").withHeaders(Header("Authentication", "Basic token.."))
        val eng = partialSecuredRoutes.orNotFound.run(getSecure).unsafeRunSync()
        assert(eng.status == Status(200))

        // 访问非加密地址
        val getInsecure = Request[IO](Method.GET, uri"/french/bonjour")
        val fre = partialSecuredRoutes.orNotFound.run(getInsecure).unsafeRunSync()
        assert(fre.status == Status(200))

        // 访问被错误地加密了的地址
        import com.hibiup.http4s.example3.controllers.AuthController.allSecuredRoutes
        val rejectedAgain = allSecuredRoutes.orNotFound.run(getInsecure).unsafeRunSync()
        assert(rejectedAgain.status == Status(401))
    }
}
