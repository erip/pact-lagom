package com.github.erip.pact.lagom

import com.lightbend.lagom.scaladsl.api.Service
import com.lightbend.lagom.scaladsl.server.{LagomApplication, LagomApplicationContext}
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import org.scalatest.{Assertion, AsyncWordSpec, Matchers}
import org.scalatest.Inspectors._
import play.api.libs.ws.WSClient
import play.api.test.WsTestClient

import scala.concurrent.Future

private[lagom] trait LagomPactTestCase[T <: LagomApplication, S <: Service]
  extends AsyncWordSpec with Matchers {

  protected def provider: String
  protected def interaction: Interaction

  private def description: String = interaction.description

  private def invoke(ws: WSClient, host: String, port: Int, interaction: Interaction): Future[Assertion] = {

    val baseRequest =
      ws.url(s"$host:$port${interaction.request.path}")
        .withMethod(interaction.request.method)

    val request = interaction.request.body.map(b => baseRequest.withBody(b)).getOrElse(baseRequest)

    for {
      resp <- request.get()
    } yield {
      // The status code should be what is expected.
      interaction.response.status should===(resp.status)
      // If there are headers, compare them. Otherwise, succeed.
      checkHeaders(resp.headers, interaction.response.headers)
      // If there is a response body, compare it with the pact. Otherwise, succeed.
      interaction.response.body.map(b => b should===(resp.body)).getOrElse(succeed)
    }
  }

  private def checkHeaders(
    responseHeaders: Map[String, Seq[String]],
    expectedHeaders: Option[Map[String, String]]
  ): Future[Assertion] = expectedHeaders match {
    case Some(h) =>
      forAll(h) { case (k, v) =>
        responseHeaders should contain key k
        responseHeaders(k).toSet should contain(v)
      }
    case None => succeed
  }

  def applicationLoader: LagomApplicationContext => T

  def withService(block: (WSClient, String, Int) => Future[Assertion]): Future[Assertion] = {
    ServiceTest.withServer(ServiceTest.defaultSetup.withCassandra())(applicationLoader) { server =>
      WsTestClient.withClient { wsClient =>
        block(wsClient, "http://127.0.0.1", server.playServer.httpPort.getOrElse(fail("No HTTP port specified")))
      }
    }
  }

  provider should {
    description in withService { case (ws, host, port) => invoke(ws, host, port, interaction) }
  }

}
