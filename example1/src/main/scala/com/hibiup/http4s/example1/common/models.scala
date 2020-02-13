package com.hibiup.http4s.example1.common

import java.util.UUID

object models {
    /**
     * 用户请求的数据结构（来自服务返回）
     */
    case class Tweet(id: Option[UUID], message: String)

    /**
     * Case class -> Json 但是 io.circe.generic.auto._ 可以提供自动转换。因此显式实现并非必须
     */
    /*
    implicit def jsonEncoder: Encoder[Tweet] = Encoder.instance { t: Tweet => json"""{"hello": ${t.message}}""" }
    */

    /**
     * io.circe.generic.auto._ 同样提供了逆向（Json -> Case class ）的支持。
     * 同时 org.http4s.circe.CirceEntityDecoder 提供 Entity -> Json 的支持，因此以下显式实现也并非必须了。
     *
     * （参考:HomeController 的 POST 方法）
     */
    //implicit val jsonDecoder: EntityDecoder[IO, Tweet] = jsonOf[IO, Tweet]
}
