package com.hibiup.http4s.example1.services

import cats.effect.IO
import com.hibiup.http4s.example1.common.models._

object TweetServices {
    def getTweet(tweetId: Int): IO[Tweet] = ???
    def getPopularTweets: IO[Seq[Tweet]] = ???
}
