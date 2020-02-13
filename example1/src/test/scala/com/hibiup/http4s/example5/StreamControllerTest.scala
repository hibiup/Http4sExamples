package com.hibiup.http4s.example5

import cats.effect.{ConcurrentEffect, ContextShift, Fiber, IO, IOApp, Timer}
import com.typesafe.scalalogging.StrictLogging
import fs2.Stream
import io.circe.Json
import org.http4s._
import org.http4s.client.blaze.BlazeClientBuilder
import org.scalatest.FlatSpec
import org.http4s.implicits._

import scala.concurrent.ExecutionContext.global

class StreamControllerTest extends FlatSpec with StrictLogging{

    private def startServer: Fiber[IO, Nothing] ={
        import monix.execution.Scheduler.Implicits.global
        implicit val cs = IO.contextShift(global)
        implicit val timer: Timer[IO] = IO.timer(global)

        // 引入特定服务，无需全部引入
        import com.hibiup.http4s.example5.controllers.StreamController.{stream => service}

        import org.http4s.server.blaze._
        val server = BlazeServerBuilder[IO]
          .bindHttp(8080)
          .withHttpApp(service.orNotFound)
          .resource

        // 启动服务( start 将它投送到另一空间 )
        val fiber = server.use(_ => IO.never).start.unsafeRunSync()
        fiber
    }

    "Stream service" should "" in {
        class StreamClient[F[_]: ConcurrentEffect : ContextShift] {
            implicit val ec = scala.concurrent.ExecutionContext.global
            implicit val cs = IO.contextShift(ec)
            import cats.effect.Blocker
            import IO.timer
            implicit val t = timer(ec)

            /**
             * 1）定义流处理引擎（client）并赋予它处理内容的解码器
             */
            def jsonStream(request: Request[F]): Stream[F, Json] = {
                import jawnfs2._
                // 新建一个隐式 Parser 解码器
                implicit val f = new io.circe.jawn.CirceSupportParser(None, false).facade

                for{
                    /**
                     * 获得处理 Stream 的 client
                     */
                    client <- BlazeClientBuilder(global).stream

                    /**
                     * 用 client 端来处理 request。并将结果映射到 parseJsonStream 解码器。(并没有立刻开始处理，只是将处理封装到一个 Stream 对象中)
                     * parseJsonStream 由 jawnfs2._ 提供，它需要一个隐式的 Stream Parser.
                     *
                     * 和 as[Json] （或 as[Tweet]，参见 example1）直接返回结果不同，因为我们需要在流中处理，而不是将结果返回
                     * 因此我们需要用到 parseJsonStream，它将始终保持在 Stream 中处理结果。
                     */
                    s <- client.stream(request).flatMap(_.body.chunks.parseJsonStream)
                } yield s
            }

            /**
             * 2）定义处理上下文。（源和输出）.将输出简单打印到标准输出上的时候需要一个处理阻塞 IO 的专门容器（Blocker）
             */
            def stream(blocker: Blocker): Stream[F, Unit] = {
                import fs2.text.{lines, utf8Encode}
                import fs2.io.stdout

                /**
                 * 定义 request
                 */
                val req = Request[F](Method.GET, uri"http://localhost:8080/nano")

                /**
                 * 得到处理这个 request 的 stream
                 */
                val s = jsonStream(req)

                /**
                 * 定义如何处理接受到的结果
                 */
                s.map(_.spaces2)/*.through(lines)*/.through(utf8Encode).through(stdout(blocker))
            }

            /**
             * 3）定义处理线程容器（Blocker）并触发处理。
             */
            def run: F[Unit] =
                /**
                 * Stream.resource 将一个资源嵌入流处理中。
                 */
                Stream.resource(Blocker[F]).flatMap { blocker =>
                    stream(blocker)
                }.compile.drain  // 开始流处理
        }

        /**
         * 4）为整个处理提供 IO 容器
         */
        object StreamConsumer extends IOApp {
            import cats.effect._
            import cats.implicits._

            def run(args: List[String]) =
                new StreamClient[IO].run.as(ExitCode.Success)
        }

        // 启动服务并测试
        val server = startServer
        StreamConsumer.run(List.empty[String]).unsafeRunSync()
        server.join.unsafeRunSync()  // hold 住服务别退出。
    }
}
