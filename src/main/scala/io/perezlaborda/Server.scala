package io.perezlaborda

import akka.http.scaladsl.Http
import io.perezlaborda.modules.ServerModule
import scala.concurrent.duration._
import scala.concurrent.Await

object Server extends App {

  val module = new ServerModule

  import module._

  Http().bindAndHandle(routes, "0.0.0.0", 9000).foreach{ http =>
    println(s"Server up running in port ${http.localAddress.getPort}")
  }

  sys.addShutdownHook{
    sttpBackend.close()
    actorSystem.terminate()
    Await.result(actorSystem.whenTerminated, 30.seconds)
  }

}
