package io.perezlaborda.controllers

import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.{Directives, Route}
import com.softwaremill.tagging.@@
import io.perezlaborda.services.{CpuHeavyLoad, ExternalService, SlowService}
import scala.concurrent.{ExecutionContext, Future}


class MainController(slowService: SlowService,
                     heavyLoad: ExecutionContext @@ CpuHeavyLoad,
                     externalService: ExternalService)
                    (implicit val ec: ExecutionContext) extends Directives {

  def external: Route = pathPrefix("external"){
    (pathEnd & get){
      val res = externalService.callService
      onSuccess(res){ r =>
        complete(StatusCodes.OK, HttpEntity(ContentTypes.`application/json`,r))
      }
    }
  }

  def healthCheck: Route = pathPrefix("health-check"){
    (pathEnd & get){
      complete(StatusCodes.OK, "hello world")
    }
  }

  def slowSafe: Route = pathPrefix("slow-safe"){
    (pathSuffix(IntNumber) & get){ iterations =>
      val res = Future{slowService.loopIterator(iterations)}(heavyLoad)
      onSuccess(res) {
        complete(StatusCodes.OK, "Done")
      }
    }
  }

  def slow: Route = pathPrefix("slow"){
    (pathSuffix(IntNumber) & get){ iterations =>
      slowService.loopIterator(iterations)
      complete(StatusCodes.OK, "Done")
    }
  }

}
