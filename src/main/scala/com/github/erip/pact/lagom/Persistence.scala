package com.github.erip.pact.lagom

import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import com.lightbend.lagom.scaladsl.testkit.ServiceTest.Setup

sealed trait Persistence {
  def persistenceEnabled: Boolean = true
  def setupWithPersistence: Setup
}

trait CassandraPersistence extends Persistence {
  override def setupWithPersistence: Setup = ServiceTest.defaultSetup.withCassandra(enabled = persistenceEnabled)
}

trait JdbcPersistence extends Persistence {
  override def setupWithPersistence: Setup = ServiceTest.defaultSetup.withJdbc(enabled = persistenceEnabled)
}