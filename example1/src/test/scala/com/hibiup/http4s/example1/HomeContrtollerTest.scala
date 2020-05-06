package com.hibiup.http4s.example1

import cats.data.OptionT
import monix.eval.Task
//import cats.effect._
import com.hibiup.http4s.example1.common.models.Tweet
import org.http4s._
import org.scalatest.FlatSpec
import org.http4s.implicits._
import monix.execution.Scheduler.Implicits.global

class HomeContrtollerTest extends FlatSpec{

    "Ok" should "be always Ok!" in {
        /**
         * Ok 代表了一个总是成功的服务，但是某个具体的服务就未必如此幸运，至少我们有可能访问不到它（404）
         */
        //import org.http4s.dsl.io._
        import org.http4s.dsl.Http4sDsl
        val dsl = new Http4sDsl[Task]{}
        import dsl._

        // Response(status=200, headers=Headers(Content-Type: text/plain; charset=UTF-8, Content-Length: 4))
        println(Ok().runSyncUnsafe())

        // Headers(Content-Type: text/plain; charset=UTF-8, Content-Length: 4)
        println(Ok().runSyncUnsafe().headers)
    }

    "Query Home route unsafe" should "" in {
        /**
         * 引入要测试的目标服务
         */
        import com.hibiup.http4s.example1.controllers.HomeController.homeRoutes

        /**
         * 定义一个客户端请求不存在的服务（Request）
         */
        val getRoot = Request[Task](Method.GET, uri"/")
        val notFound: OptionT[Task, Response[Task]] = homeRoutes.run(getRoot)

        // None
        assert(notFound.value.runSyncUnsafe().isEmpty)

        /**
         * 尝试用正确的路径去请求服务.
         */
        val getHello = Request[Task](Method.GET, uri"/hello/john")
        val resp: OptionT[Task, Response[Task]] = homeRoutes.run(getHello)

        // Response(status=200, headers=Headers(Content-Type: text/plain; charset=UTF-8, Content-Length: 12))
        println(resp.value.runSyncUnsafe().get)

        // Stream(..)
        val body: EntityBody[Task] = resp.value.runSyncUnsafe().get.body
        println(body)
    }

    "Query Home route with NotFound" should "" in {
        /**
         * 如果要测试如果路径不存在的情况：
         */
        import com.hibiup.http4s.example1.controllers.HomeController.homeRoutes
        val getRoot = Request[Task](Method.GET, uri"/")

        /**
         * 附加上 orNotFound 将它补充完整并转换成 Kleisli（参见 MainWithIOApp）
         */
        val notFound = homeRoutes.orNotFound.run(getRoot)

        // Response(status=404, headers=Headers(Content-Type: text/plain; charset=UTF-8, Content-Length: 12))
        assert(notFound.runSyncUnsafe().status == Status(404))

        /**
         * 正确的访问路径
         */
        val getHello = Request[Task](Method.GET, uri"/hello/john")
        val resp = homeRoutes.orNotFound.run(getHello)

        val actualResp = resp.runSyncUnsafe()

        // Response(status=200, headers=Headers(Content-Type: text/plain; charset=UTF-8, Content-Length: 12))
        println(actualResp)
        // Stream(..)
        println(actualResp.body)

        /**
         * org.http4s.circe.CirceEntityDecoder._ 提供了从 Entity -> Json 的支持
         * io.circe.generic.auto._ 提供了 Json -> Tweet 的支持
         */
        import io.circe.generic.auto._
        import org.http4s.circe.CirceEntityDecoder._
        // Tweet(Some(xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx),Hello, john)
        println(actualResp.as[Tweet].runSyncUnsafe())
    }

    "Query Async service" should "" in {
        import com.hibiup.http4s.example1.controllers.HomeController.homeRoutes
        val getRoot = Request[Task](Method.GET, uri"/async/john")

        val resp = homeRoutes.orNotFound.run(getRoot)

        // Response(status=200, headers=Headers(Content-Type: text/plain; charset=UTF-8, Content-Length: 12))
        println(resp.runSyncUnsafe())

        // Stream(..)
        val stream = resp.runSyncUnsafe().body

        // Some(125)
        println(stream.compile.last.runSyncUnsafe())
    }
}
