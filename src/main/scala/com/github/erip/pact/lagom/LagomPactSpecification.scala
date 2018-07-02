package com.github.erip.pact.lagom

import java.io.File

import com.lightbend.lagom.scaladsl.api.Service
import com.lightbend.lagom.scaladsl.server.{LagomApplication, LagomApplicationContext}
import org.scalatest.{FlatSpec, Matchers}
import play.api.libs.json.{JsValue, Json}

import scala.io.Source

trait LagomPactSpecification[T <: LagomApplication, S <: Service]
  extends FlatSpec with Matchers {

  def pactFile: File

  def appLoader: LagomApplicationContext => T

  private def source: String = Source.fromFile(pactFile).getLines.mkString
  private def json: JsValue = Json.parse(source)
  private def pact: Pact = Json.fromJson[Pact](json).getOrElse(fail("Malformed pact file"))
  private def providerName: String = pact.provider.name
  private def consumerName: String = pact.consumer.name

  it should "test interactions" in {
    pact.interactions.foreach { inter =>
      new LagomPactTestCase[T, S] {
        override def provider: String = providerName

        override def interaction: Interaction = inter

        override def applicationLoader: LagomApplicationContext => T = appLoader
      }.execute()
    }
  }

}
