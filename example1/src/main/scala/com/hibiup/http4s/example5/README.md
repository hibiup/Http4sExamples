Straem 在 http4s 中是一个很重要的概念，http4s 总是将 Request 和 response 的 body 的内容作为 stream 看待(不含 header)，因此当我们通过 http4s 的客户端获取 response.body
的时候也总是会得到一个 Stream 对象。并且也因此它允许我们不需要获得全部的 request/response 的内容后才开始后续处理。关于 body 内容的解码，参考：https://http4s.org/v0.21/entity/


