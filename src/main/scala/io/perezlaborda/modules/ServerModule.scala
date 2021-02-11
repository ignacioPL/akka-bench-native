package io.perezlaborda.modules

import java.util.concurrent.Executors

import akka.actor.ActorSystem
import akka.dispatch.MessageDispatcher
import akka.stream.ActorMaterializer
import com.softwaremill.macwire._
import com.softwaremill.sttp.okhttp.OkHttpFutureBackend
import com.softwaremill.tagging._
import com.typesafe.config.{Config, ConfigFactory}
import io.perezlaborda.controllers.MainController
import io.perezlaborda.services.{CpuHeavyLoad, ExternalService, SlowService}

import scala.concurrent.ExecutionContext

class ServerModule {

  implicit val actorSystem = ActorSystem("akka-bench")
  implicit val executor = actorSystem.dispatcher
  implicit val materializer = ActorMaterializer()

  import akka.http.scaladsl.server.Directives._

  implicit lazy val sttpBackend = OkHttpFutureBackend()(ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(Runtime.getRuntime.availableProcessors*8)))//AkkaHttpBackend()//AsyncHttpClientFutureBackend()

  val config: Config = ConfigFactory.load()

  lazy val heavyLoad: @@[MessageDispatcher, CpuHeavyLoad] = actorSystem.dispatchers.lookup("cpu-pool-dispatcher").taggedWith[CpuHeavyLoad]

  private lazy val externalService: ExternalService = wire[ExternalService]
  private lazy val slowService: SlowService = wire[SlowService]
  private lazy val endpoints: MainController = wire[MainController]

  val routes =
      endpoints.healthCheck ~
      endpoints.slow ~
      endpoints.external ~
      endpoints.slowSafe
}
