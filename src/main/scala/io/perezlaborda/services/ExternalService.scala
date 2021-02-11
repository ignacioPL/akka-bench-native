package io.perezlaborda.services

import com.softwaremill.sttp._
import com.typesafe.config.Config

import scala.concurrent.{ExecutionContext, Future}

class ExternalService(config: Config)(implicit val sttpBackend: SttpBackend[Future, Nothing]) {

  private val req = sttp.get(uri"${config.getString("external.endpoint")}/service")

  def callService(implicit ec: ExecutionContext): Future[String] =
    req.send().map{ r => r.unsafeBody }
}
