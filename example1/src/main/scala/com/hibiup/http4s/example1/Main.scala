package com.hibiup.http4s.example1

import cats.data.Kleisli
import com.hibiup.http4s.example1.controllers.{HomeController, TweetController}
import org.http4s.server.blaze.BlazeServerBuilder
import cats.implicits._
import org.http4s.{Request, Response}
import org.http4s.implicits._
import org.http4s.server.Router

object Main extends App{
    /**
     * 在非 Cats IOApp 中，需要重建所有隐式变量
     */
    import cats.effect._
    implicit val ec = scala.concurrent.ExecutionContext.global
    implicit val cs = IO.contextShift(ec)
    import IO.timer
    implicit val t = timer(ec)

    import HomeController.homeRoutes
    import TweetController.tweetRoutes

    // （可选）通过 <+> 将部分路由合并
    val tweetAndHomeRoutes = homeRoutes <+> tweetRoutes

    /**
     * 合并所有的 controller。此时可以给它们附加前缀路径. (tweetAndHomeRoutes 中重复了 homeRoute)
     */
    val routes: Kleisli[IO, Request[IO], Response[IO]] =
        Router(
            "/" -> homeRoutes,
            "/api" -> tweetAndHomeRoutes)
          .orNotFound  // 总的错误控制

    /**
     * BlazeServerBuilder 生成服务入口，然后显式启动服务
     */
    val fiber = BlazeServerBuilder[IO]
      .bindHttp(8080, "0.0.0.0")
      .withHttpApp(routes)
      .resource
      .use(_ => IO.never)  // 使用的任务容器（never 容器会阻塞任务返回）
      .start               // start 将 Fiber 启动在另一个任务空间（也就是说 IO.never 会阻塞在另一个任务空间中，不阻塞当前任务）
      .unsafeRunSync()     // 显式启动

    // 或以服务(serve)方式（效果和 IO.never 相同）
    /*
    val fiber = BlazeServerBuilder[IO]
      .bindHttp(8080, "0.0.0.0")  // 可选
      .withHttpApp(routes)
      .serve              // 阻塞(服务)模式
      .compile
      .drain
      .start
      .unsafeRunSync()   // 显式启动
    */

    // 将任务(io.never)取回当前空间，阻塞主任务结束。实际上也可以不必这么麻烦，注释掉 start 就可以直接在当前任务空间启动（阻塞）fiber
    val io = fiber.join.unsafeRunSync()
}
