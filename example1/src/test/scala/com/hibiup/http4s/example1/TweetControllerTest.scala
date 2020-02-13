package com.hibiup.http4s.example1

import java.util.UUID

import cats.implicits._
import cats.effect.IO
import com.hibiup.http4s.example1.common.models.Tweet
import org.http4s._
import org.scalatest.FlatSpec
import org.http4s.implicits._

class TweetControllerTest extends FlatSpec{
    "Post with entity" should "" in {
        import io.circe.syntax._
        import io.circe.generic.auto._
        import org.http4s.circe._

        val req = Request[IO](Method.POST,uri"/create")   // uri 语法由 org.http4s.implicits._ 提供
          .withEntity(Tweet(None, "hELLO!").asJson)

        import com.hibiup.http4s.example1.controllers.TweetController.tweetRoutes
        val resp = tweetRoutes.orNotFound.run(req).unsafeRunSync()

        // Response(status=201, headers=Headers(Content-Type: application/json, Content-Length: 58))
        println(resp)

        println(resp.body.compile.last.unsafeRunSync())
    }
}
