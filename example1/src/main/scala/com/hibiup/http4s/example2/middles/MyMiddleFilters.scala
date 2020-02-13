package com.hibiup.http4s.example2.middles

import cats.data.Kleisli
import cats.effect.IO
import org.http4s.{Header, HttpRoutes, Request, Status}

object MyMiddleFilters {
    /**
     * 定义一个 middle ware 很简单，第一个参数是一个 Route，返回一个相同的 HttpRoutes[IO] 即可.
     *
     * 这个接口的关键在于 HttpRoutes 是一个 Kleisli，因此当我们暴露这个 middle ware 作为原服务的代理给用户端时，我们可以
     * 截获它的 request, 然后传递给被代理的 route，并且也可以截获它的 response，修改后再传回客户端。
     *
     * 假设我们在这里为返回值添加一个 header:
     *
     * 1) 定义一个 middle ware。然后到 Route (LuckyDrawController) object 中使用它就可以了。
     */
    def myMiddle(service: HttpRoutes[IO], header: Header): HttpRoutes[IO] = Kleisli { req: Request[IO] =>
        service(req).map {
            case Status.Successful(resp) =>
                resp.putHeaders(header)
            case resp =>
                resp
        }
    }
}
