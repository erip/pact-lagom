package com.github.erip.pact.lagom.verify

import java.io.File

import com.lightbend.lagom.scaladsl.api.Service
import com.lightbend.lagom.scaladsl.server.{LagomApplication, LagomApplicationContext}
import com.lightbend.lagom.scaladsl.testkit.ServiceTest.Setup
import org.scalatest.{FlatSpec, Matchers}
import play.api.libs.json.{JsValue, Json}

import scala.io.Source

/** The specification which will run Lagom pacts.
  *
  * @tparam T the type of [[com.lightbend.lagom.scaladsl.server.LagomApplication]] which will be loaded.
  * @tparam S the type of [[com.lightbend.lagom.scaladsl.api.Service]] under test.
  */
trait LagomPactSpecification[T <: LagomApplication, S <: Service] extends FlatSpec with Matchers { self: Persistence =>

  /**
    * A JSON file containing the contracts to test.
    */
  def pactFile: File

  /**
    * The anonymous function which will create an application when given an application context.
    */
  def appLoader: LagomApplicationContext => T

  /**
    * The pact to be tested. If the Pact is successfully read, the tests proceed.
    * Otherwise, the suite fail.
    */
  private final def pact: Pact = {
    val source: String = Source.fromFile(pactFile).getLines.mkString
    val json: JsValue = Json.parse(source)
    Json.fromJson[Pact](json).getOrElse(fail("Malformed pact file"))
  }

  /**
    * The name of the provider under test.
    */
  private final def providerName: String = pact.provider.name

  /**
    * The name of the consumer under test.
    */
  private final def consumerName: String = pact.consumer.name

  it should "test interactions" in {
    pact.interactions.foreach { inter =>
      new LagomPactTestCase[T, S] {
        override def provider: String = providerName

        override def interaction: Interaction = inter

        override def applicationLoader: LagomApplicationContext => T = appLoader

        override def setupWithPersistence: Setup = self.setupWithPersistence
      }.execute()
    }
  }

}
