package com.hibiup.http4s.example1

import cats.data.Kleisli
import cats.effect.ExitCode
import com.hibiup.http4s.example1.controllers.{HomeController, TweetController}
import org.http4s.server.blaze.BlazeServerBuilder
import cats.implicits._
import monix.eval.{Task, TaskApp}
import org.http4s.{Request, Response}
import org.http4s.implicits._
import org.http4s.server.Router

object MainWithIOApp extends TaskApp{
    import HomeController.homeRoutes
    import TweetController.tweetRoutes

    // （可选）通过 <+> 将部分路由合并
    val tweetAndHomeRoutes = homeRoutes <+> tweetRoutes

    /**
     * 合并所有的 controller。此时可以给它们附加前缀路径. (tweetAndHomeRoutes 中重复了 homeRoute)
     */
    val routes: Kleisli[Task, Request[Task], Response[Task]] =
        Router(
            "/" -> homeRoutes,
            "/api" -> tweetAndHomeRoutes)
          .orNotFound  // 总的错误控制

    /**
     *
     * run 是 Cats IOApp 的入口函数。不需要显式执行 unsafeRunSync
     *
     * BlazeServerBuilder 生成服务入口
     */
    def run(args:List[String]): Task[ExitCode] = {
        BlazeServerBuilder[Task]
          .bindHttp(8080, "0.0.0.0")  // 可选
          .enableHttp2(true)
          .withHttpApp(routes)                     // 将合并的 routes 载入
          .resource                                // 得到 Resource[F] 具柄
              .use(_ => Task.never)                  // 通过 Resource 加载 IO.never 作为异步执行容器，IO.never 不会退出。
              .as(ExitCode.Success)
    }
}
