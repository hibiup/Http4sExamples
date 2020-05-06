package com.hibiup.http4s.example3.middles

import java.util.UUID

// import akka.http.scaladsl.server.{Directive0, Directive1}
import cats.Monad
import cats.implicits._
import cats.data.{EitherT, Kleisli, OptionT}
import cats.effect.IO
import com.hibiup.http4s.example3.commons.models.User
import com.typesafe.scalalogging.StrictLogging
import org.http4s.headers.{Authorization, Location}
import org.http4s.{AuthedRoutes, Header, Message, Request, Response, Uri}
import org.http4s.server.{AuthMiddleware, HttpMiddleware, ServerRequestKeys}
import org.http4s.util.CaseInsensitiveString

object AuthFilter extends StrictLogging{
    /**
     * A-1) 实现用户认证方法：
     *
     * 该方法只需返回用户的认证接口数据类型：Kleisli[OptionT[IO, ?], Request[IO], T]， T 为自定义的用户实体（参见 models.User）
     * */
    type M[T] = OptionT[IO, T]
    def authUser: Kleisli[M, Request[IO], User] = Kleisli((request: Message[IO]) => OptionT(IO {
        request.headers.get(CaseInsensitiveString("Authentication")) match {
            case Some(Header(_, token)) => User(UUID.randomUUID(), "JohnSmith").some
            case _ => None
        }
    }))

    /**
     * A-2) 将该认证实体作为参数传递给 AuthMiddleware。同时可以给一个可选的错误处理方案。例如重定向，或返回 Forbidden("...")
     */
    val authMiddleware: AuthMiddleware[IO, User] = AuthMiddleware/*.withFallThrough*/(authUser)  // .withFallThrough 给所有失败的地址放回 404

    /**
     * B-1) 如果我们不满意于缺省的认证失败，比如我们希望重定向到登陆地址，那么可以将返回类型定义为 Either，并给予一个错误状态，
     * 比如 TemporaryRedirect。然后给 AuthMiddleware 函数第二个参数失败的处理具柄
     */
    def authUserFallThroughToLogin: Kleisli[IO, Request[IO], Either[Throwable, User]] = Kleisli(request => {
        // 2.2.1) 定义获取用户的方法
        def retrieveUser: Kleisli[IO, Long, User] = Kleisli(token => IO(User(UUID.randomUUID(), "JohnSmith")))
        // 2。2。2）定义检验 token 的方法
        def validateSignedToken(token:String): Option[String] = token.some

        val message = for {
            header <- request.headers.get(Authorization).toRight(new RuntimeException("Couldn't find an Authorization header"))
            token <- validateSignedToken(header.value).toRight(new RuntimeException("Invalid token"))
            message <- Either.catchOnly[NumberFormatException]{
                token.toLong       // 验证 token 格式. 必须是 Long，否则抛出 NumberFormatException 错误
            }  // 否则返回 Exception 类型的左值
        } yield message

        //
        message.traverse(retrieveUser.run)
    } )

    // 定义一个错误处理具柄
    import org.http4s.dsl.io._
    val failureHandler: AuthedRoutes[Throwable, IO] = Kleisli(req =>
        OptionT.liftF(Forbidden(req.context.getMessage))               // req.context 返回左值
        //OptionT.liftF(TemporaryRedirect(Location(uri"/login.html"))) // 或者给客户端发送重定向
    )

    /**
     * B-2) 将该认证实体作为参数传递给 AuthMiddleware。
     */
    val authOrLoginMiddleware: AuthMiddleware[IO, User] = AuthMiddleware(authUserFallThroughToLogin, failureHandler)

    /**
     * C-1) 利用 Session 验证（未完成）
     */
    object SessionHttpMiddleware{
        def apply(): Kleisli[OptionT[IO, ?], Request[IO], Response[IO]] = Kleisli{ req =>
            OptionT.liftF({
                val output = req.attributes.lookup(ServerRequestKeys.SecureSession).flatten.map{ session =>
                    assert(session.sslSessionId != "")
                    assert(session.cipherSuite != "")
                    assert(session.keySize != 0)
                    session.X509Certificate.head.getSubjectX500Principal.getName
                }.getOrElse("Invalid")

                Ok(output)
            })
        }
    }
}
