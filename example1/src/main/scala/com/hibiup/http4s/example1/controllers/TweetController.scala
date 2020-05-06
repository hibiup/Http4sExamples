package com.hibiup.http4s.example1.controllers

import java.util.UUID

import cats.implicits._
import cats.effect._
import com.hibiup.http4s.example1.common.models._
import org.http4s.HttpRoutes
import io.circe.syntax._
import io.circe.generic.auto._
import monix.eval.Task
import org.http4s.circe._

object TweetController{
    import org.http4s.dsl.Http4sDsl
    // import org.http4s.dsl.io._
    val dsl = new Http4sDsl[Task]{}
    import dsl._

    import com.hibiup.http4s.example1.services.TweetServices._

    val tweetRoutes: HttpRoutes[Task]= HttpRoutes.of[Task] {
        /**
         * 1) 通常这里需要将用户输入的数据结构转换为后端服务能够接受的数据结构。例如(IntVar(tweetId))：Query parameter -> Int
         *
         * 可选的有 IntVar, IntLong, String 等。缺省是 String。可以定制自己的 Extractor object,实现 unapply(param:String) 接口即可：
         *
         *
         * object MyType{
         *   def unapply(param:String):Option[...] {...}
         * }
         *
         * 关于路径参数，参考：https://http4s.org/v0.21/dsl/
         */
        case GET -> Root / "tweets" / IntVar(tweetId)=> {
            getTweet(tweetId).flatMap((t: Tweet) => Ok(t.asJson))
        }

        case GET -> Root / "tweets" / "popular" => {
            getPopularTweets.flatMap(tweets => Ok(tweets.asJson))
        }


        /**
         * request@_ 取得 Request
         */
        case request@POST -> Root / "create" => {
            // 提供异步支持
            import scala.concurrent.Future
            import monix.execution.Scheduler.Implicits.global
            //implicit val cs: ContextShift[IO] = IO.contextShift(global)

            /**
             * 从请求中读取 Json 并转换成 case class
             */
            import io.circe.generic.auto._
            // org.http4s.circe 不仅提供的 EntityEncoder，同样提供了 DecoderEncoder
            import org.http4s.circe.CirceEntityDecoder._

            for{
                /**
                 * 从 Request 中读取 Tweet：
                 *
                 * 与下行将 Case class -> Json 和 Json -> Entity 分为两个分开的步骤不同，接受上行数据时 as[T] 同时调用了
                 * EntityDecoder 和 Decoder
                 *
                 * org.http4s.circe.CirceEntityDecoder._ 提供了从 Entity -> Json 的支持
                 * io.circe.generic.auto._ 提供了 Json -> Tweet 的支持
                 */
                t <- request.as[Tweet]  // 这里同时用到 EntityDecoder 和 Decoder

                // 立刻生成回复信息（需要 EntityEncoder）
                resp <- Created(Task.fromFuture(Future{
                    Tweet(UUID.randomUUID().some, s"${t.message}").asJson
                }))
            } yield resp
        }
    }
}
