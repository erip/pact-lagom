package com.github.erip.pact.lagom.verify

import com.lightbend.lagom.scaladsl.api.Service
import com.lightbend.lagom.scaladsl.server.{LagomApplication, LagomApplicationContext}
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import com.lightbend.lagom.scaladsl.testkit.ServiceTest.Setup
import org.scalatest.Inspectors._
import org.scalatest.{Assertion, AsyncWordSpec, Matchers}
import play.api.libs.ws.WSClient
import play.api.test.WsTestClient

import scala.concurrent.Future

/**
  * A testcase for a single interaction.
  *
  * @tparam T the type of [[com.lightbend.lagom.scaladsl.server.LagomApplication]] which will be loaded.
  * @tparam S the type of [[com.lightbend.lagom.scaladsl.api.Service]] under test.
  */
private[lagom] trait LagomPactTestCase[T <: LagomApplication, S <: Service]
  extends AsyncWordSpec with Matchers {

  /**
    * The ame of the provider under test.
    */
  protected def provider: String

  /**
    * The interaction being tested.
    */
  protected def interaction: Interaction

  /**
    * The service-specific setup with persistence.
    */
  protected def setupWithPersistence: Setup

  /**
    * The description of the interaction, which will be used as the test name.
    */
  private def description: String = interaction.description

  /**
    * The invocation of an interaction against a running test service and associated assertions against
    * the response thereof.
    *
    * @param ws the ws client used to submit HTTP requests.
    * @param host the host of the test service
    * @param port the port of the test service
    * @param interaction the interaction to be tested.
    * @return a future of assertions describing the success or failure of the test.
    */
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

  /**
    * Checks whether all expected headers exist in the response headers, but
    * not vice versa. If there are no expected headers, the check succeeds indiscriminately.
    * @param responseHeaders the headers returned from the HTTP request
    * @param expectedHeaders the optional headers to check.
    * @return a future containing the success or failure of the assertions made against headers.
    */
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

  /**
    * The anonymous function which will create an application when given an application context.
    */
  def applicationLoader: LagomApplicationContext => T

  /**
    * A fixture which will provide a test instance of the provider and a web service client.
    * It will run arbitrary tests or fail of an HTTP port is not provided by Play.
    *
    * @param block the tests to be run when given a client and the ingredients of a service.
    */
  def withService(block: (WSClient, String, Int) => Future[Assertion]): Future[Assertion] = {
    ServiceTest.withServer(setupWithPersistence)(applicationLoader) { server =>
      WsTestClient.withClient { wsClient =>
        block(wsClient, "http://127.0.0.1", server.playServer.httpPort.getOrElse(fail("No HTTP port specified")))
      }
    }
  }

  provider should {
    description in withService { case (ws, host, port) => invoke(ws, host, port, interaction) }
  }

}
