package com.hibiup.http4s.example4.staticresources

import cats.effect.{Blocker, ContextShift, IO}
import org.http4s.{HttpRoutes, Request}
import org.http4s.dsl.io._
import play.twirl.api.Html

object TwirlResourceController {
    /**
     * 1) 使用 twirl 模版需要在 project 目录下新建 plugins.sbt 文件，加入：
     *   addSbtPlugin("com.typesafe.sbt" % "sbt-twirl" % "1.5.0")
     *
     * 2) 在 build.sbt 中项目设置的后面加上：
     *   lazy val Examples = project.in(file("example1")).settings(
     *     settings,
     *     name:="example1",
     *     ...
     *   ).enablePlugins(SbtTwirl)   <-- 加上这一项
     *
     * 3) 在 sbt 控制台中切换到项目目录下：
     *   > project Examples
     *
     * 4) 然后手工预编译：
     *   > compile
     *
     * 完需要等待以下就可以看到模版被编译成 class 文件，可以载入使用了。
     */
    def routes()(implicit cs:ContextShift[IO]): HttpRoutes[IO] = HttpRoutes.of[IO] {
        case _ @ GET -> Root / "tt" =>
            // 隐式将 Html 转成 String 输出。
            import org.http4s.twirl._
            Ok(toHtml(Map("Haha" -> 5)))
    }

    // "tt" 是 twirl 目录下的 folder
    private def toHtml(kv: Map[String, Double]): Html = tt.html.auth(kv)
}
