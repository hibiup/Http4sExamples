package com.hibiup.http4s.example5.client

import org.http4s._
import org.http4s.client.blaze._
import org.http4s.client.oauth1
import org.http4s.implicits._
import cats.effect._
import cats.implicits._
import fs2.Stream
import fs2.io.stdout
import fs2.text.{lines, utf8Encode}
import io.circe.Json
import jawnfs2._
import java.util.concurrent.{Executors, ExecutorService}
import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.global


class TwitterStreamClient[F[_]: ConcurrentEffect : ContextShift] {
    /**
     * 假设服务提供 JSON 格式的数据，我们将使用 jawn 作为 circe 的 JSON 解释器。（通过 jawn-fs2）
     */
    implicit val f = new io.circe.jawn.CirceSupportParser(None, false).facade

    /**
     * client 支持 oauth1。consumerKey, consumerSecret, accessToken, accessSecret 都来自 Twitter 的服务。
     * 通过 http4s.auth1 提供的 API 实现登陆。
     */
    def sign(consumerKey: String,
             consumerSecret: String,
             accessToken: String,
             accessSecret: String)(req: Request[F]): F[Request[F]] = {
        val consumer = oauth1.Consumer(consumerKey, consumerSecret)
        val token    = oauth1.Token(accessToken, accessSecret)

        // 返回 Request
        oauth1.signRequest(req, consumer, callback = None, verifier = None, token = Some(token))
    }

    /**
     *
     */
    def jsonStream(consumerKey: String,
                   consumerSecret: String,
                   accessToken: String,
                   accessSecret: String)(req: Request[F]): Stream[F, Json] =
        for {
            /**
             *  1）新建 BlazeClient。.stream 方法返回一个消费 Stream 数据的 client
             *  */
            client <- BlazeClientBuilder(global).stream
            /**
             *  2）Stream.eval 将 sign 返回的数据封装到 Stream 容器中，以和 client 的数据类型对齐（client 的类型是 Stream[]）
             *  */
            signRequest  <- Stream.eval(sign(consumerKey, consumerSecret, accessToken, accessSecret)(req))
            /**
             *  3) client.stream 方法接受 Request (sign 方法返回 Request)，然后 flatMap 出 response.body
             *  */
            res <- client.stream(signRequest).flatMap(_.body.chunks.parseJsonStream)  // parseJsonStream 由 io.circe.jawn.CirceSupportParser 提供支持
        } yield res  // 将 response 装在 Stream 中返回


    /**
     * 通过 spaces2 格式化输出，然后逐行（through(line）)编码（through(utf8Encoder）打印（through(stdout）。
     */
    def stream(blocker: Blocker): Stream[F, Unit] = {
        val req = Request[F](Method.GET, uri"https://stream.twitter.com/1.1/statuses/sample.json")
        val s   = jsonStream("<consumerKey>", "<consumerSecret>", "<accessToken>", "<accessSecret>")(req)
        s.map(_.spaces2).through(lines).through(utf8Encode).through(stdout(blocker))
    }

    /**
     * Compile our stream down to an effect to make it runnable
     * */
    def run: F[Unit] =
        Stream.resource(Blocker[F]).flatMap { blocker =>
            stream(blocker)
        }.compile.drain
}

object TWStreamApp extends IOApp {
    def run(args: List[String]) =
        (new TwitterStreamClient[IO]).run.as(ExitCode.Success)
}