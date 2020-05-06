package com.hibiup.http4s.example1.services

import com.hibiup.http4s.example1.common.models._
import monix.eval.Task

object TweetServices {
    def getTweet(tweetId: Int): Task[Tweet] = ???
    def getPopularTweets: Task[Seq[Tweet]] = ???
}
