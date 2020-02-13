package com.hibiup.http4s.example1

import cats.effect.IO
import org.http4s.EntityEncoder
import com.hibiup.http4s.example1.common.models._


package object controllers {
    /**
     * Json -> Entity
     *
     * 对服务返回的数据结构进行编码的编码器.
     *
     * 据结构需要被转换成 Json (Tweet -> Json) 然后在被装入 Response( Json -> Entity ).
     *
     * 我们需要提供一个 EntityEncoder 或通过 import org.http4s.circe._ 实现自动实现 Json -> Entity.
     */
     // implicit def jsonEntityEncoder: EntityEncoder[IO, Json] = ???

    // implicit def tweetEncoder: EntityEncoder[IO, Tweet] = ???
    // implicit def tweetsEncoder: EntityEncoder[IO, Seq[Tweet]] = ???

}
