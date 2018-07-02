package com.github.erip.pact.lagom

import play.api.libs.json.{Format, JsObject, JsValue, Json}

import scala.collection.immutable._

private[lagom] final case class Provider(name: String)

private[lagom] object Provider {
  implicit val format: Format[Provider] = Json.format
}

private[lagom] final case class Consumer(name: String)

private[lagom] object Consumer {
  implicit val format: Format[Consumer] = Json.format
}

private[lagom] final case class Request(method: String, path: String, body: Option[JsValue])

private[lagom] object Request {
  implicit val format: Format[Request] = Json.format
}

private[lagom] final case class Response(
  status: Int,
  headers: Option[Map[String, String]],
  body: Option[JsValue]
)

private[lagom] object Response {
  implicit val format: Format[Response] = Json.format
}

private[lagom] final case class Interaction(
  description: String,
  request: Request,
  response: Response
)

private[lagom] object Interaction {
  implicit val format: Format[Interaction] = Json.format
}

private[lagom] final case class Pact(
  provider: Provider,
  consumer: Consumer,
  interactions: List[Interaction]
)

private[lagom] object Pact {
  implicit val format: Format[Pact] = Json.format
}