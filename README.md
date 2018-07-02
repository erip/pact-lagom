# Pact-Lagom

Pact-Lagom is a consumer-driven contract testing library.

## Building

Since this is a prototype, it will need to be published locally:

```
sbt publishLocal
```

## Example

Derived from the [online-scala-auction](https://github.com/lagom/online-auction-scala) repository, this test should be dropped in `online-auction-scala/item-impl/src/test/scala/com/example/auction/item/impl/ItemServicePactSpec.scala`:

```scala
package com.example.auction.item.impl

import java.io.File

import com.example.auction.item.api.ItemService
import com.github.erip.pact.lagom.LagomPactSpecification
import com.lightbend.lagom.scaladsl.server.{LagomApplicationContext, LocalServiceLocator}

class ItemServicePactSpec extends LagomPactSpecification[ItemApplication, ItemService] {

  override def pactFile: File = new File(getClass.getResource("/pacts/pact.json").toURI)

  override def appLoader: LagomApplicationContext => ItemApplication =
    context => new ItemApplication(context) with LocalServiceLocator
}
```

No contract-driven tests are complete without their contracts. A `resources/pacts` directory must be created in the `itemImpl` test directory with a single file: `pact.json`. An example pact can be seen below:

```json
{
  "provider": {
    "name": "Item Service"
  },
  "consumer": {
    "name": "Unused consumer"
  },
  "interactions": [
    {
      "description": "Not find an item before items have been added",
      "request": {
        "method": "get",
        "path": "/api/item/babbcbc0-522c-4fbf-84d6-0cd5a8233208"
      },
      "response": {
        "status": 404,
        "headers": {
          "Content-Type": "application/json"
        }
      }
    }
  ]
}
```
