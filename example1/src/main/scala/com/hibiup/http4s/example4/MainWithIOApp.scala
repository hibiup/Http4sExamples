package com.hibiup.http4s.example4

import cats.implicits._
import cats.effect.{Blocker, ExitCode, IO, IOApp, Resource}
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.{Router, Server}
import org.http4s.implicits._

object MainWithIOApp extends IOApp{
    import org.http4s.server.staticcontent._

    override def run(args: List[String]): IO[ExitCode] =
        app.use(_ => IO.never).as(ExitCode.Success)

    val app: Resource[IO, Server[IO]] =
        for {
            blocker <- Blocker[IO]
            /**
             * 静态资源需要用到一个 cats.effect.Blocker 参数，它是 Cats 缺省提供的用于处理阻塞任务的线程池（https://typelevel.org/cats-effect/api/cats/effect/Blocker.html）
             *
             * 它装在一个IO容器中提供，因此可以和 BlazeServerBuilder[IO] 用于同一个 for-comprehension。否则也可以用以下方法获得：
             *
             *   import java.util.concurrent._
             *
             *   val blockingPool = Executors.newFixedThreadPool(4)
             *   val blocker = Blocker.liftExecutorService(blockingPool)
             *
             */

            server <- BlazeServerBuilder[IO]
              .bindHttp(8080)
              .withHttpApp(
                  Router(
                      /**
                       * 从相对(或绝对)目录下取得静态资源： http://localhost:8080/images/image.html
                       * */
                      "/images" -> fileService[IO](FileService.Config("./example1/src/main/resources/images", blocker)),       // 将 Blocker 分配给静态资源服务程序

                      /**
                       * 从 classpath 中取得资源: http://localhost:8080/static/static.html
                       */
                      "/static" -> resourceService[IO](ResourceService.Config("/assets", blocker)),

                      /**
                       * http://localhost:8080/public/res/test.html
                       * http://localhost:8080/public/res/test.js
                       * http://localhost:8080/public/res/test.css  (NotFound)
                       */
                      "/public" -> com.hibiup.http4s.example4.staticresources.CustomStaticResourceController.routes(blocker),

                      /**
                       *
                       * http://localhost:8080/twirl/tt
                       */
                      "/twirl" -> com.hibiup.http4s.example4.staticresources.TwirlResourceController.routes()
                      //
                      // TODO: Add more route
                  ).orNotFound)
              .resource
        } yield server
}
