package com.hibiup.http4s.example1

import com.hibiup.http4s.example1.common.models.Tweet
import com.typesafe.scalalogging.StrictLogging
import org.scalatest.FlatSpec
import org.http4s.client.dsl.io._
import org.http4s.Method._
import cats.effect._
import com.hibiup.http4s.example1.controllers.TweetController
import org.http4s.client.blaze.BlazeClientBuilder
import fs2.Stream
import monix.eval.Task
import org.http4s.implicits._


class IntegrationTest extends FlatSpec with StrictLogging{
    import monix.execution.Scheduler.Implicits.global
    implicit val cs = IO.contextShift(global)

    private def startServer: Fiber[Task, Nothing] ={
        //implicit val timer: Timer[IO] = IO.timer(global)

        // 引入特定服务，无需全部引入
        import TweetController.tweetRoutes

        import org.http4s.server.blaze._
        val server = BlazeServerBuilder[Task]
          .bindHttp(8080)
          .withHttpApp(tweetRoutes.orNotFound)
          .resource

        // 启动服务( start 将它投送到另一空间 )
        val fiber = server.use(_ => Task.never).start.runSyncUnsafe()
        fiber
    }

    "Integration" should "" in {
        import io.circe.syntax._
        import io.circe.generic.auto._
        import org.http4s.circe._

        def post(msg: String): Stream[IO, Tweet] = {
            // Encode a User request
            val req = POST(
                Tweet(id=None, message=msg).asJson,
                uri"http://localhost:8080/create"
            )

            // Create a client
            BlazeClientBuilder[IO](global).stream.flatMap { httpClient =>
                /**
                 * Decode response，并指定 decode 的格式
                 */
                Stream.eval(httpClient.expect(req)(jsonOf[IO, Tweet]))
            }
        }

        // 在另一任务空间启动服务
        val server = startServer

        // 测试
        val resp = post("hELLO!")
          .compile.last   // 取得 Stream 中的内容(根据之前的 Decode 取得 Tweet)
          .unsafeRunSync()
        resp match{
            case t@Some(Tweet(_, msg)) => {
                println(t)
                assert(msg == "hELLO!")
            }
        }

        // 停止服务
        server.cancel.runSyncUnsafe()
    }
}
