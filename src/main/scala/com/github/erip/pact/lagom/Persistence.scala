package com.github.erip.pact.lagom

import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import com.lightbend.lagom.scaladsl.testkit.ServiceTest.Setup

/**
  * A trait which will provide potentially-enabled in-memory persistence.
  */
sealed trait Persistence {

  /**
    * Whether persistence should be enabled in tests.
    */
  def persistenceEnabled: Boolean = true

  /**
    * The service-specific setup for the test service.
    * @return
    */
  def setupWithPersistence: Setup
}

trait CassandraPersistence extends Persistence {

  /**
    * Setup for Cassandra in-memory persistence.
    */
  override final def setupWithPersistence: Setup = ServiceTest.defaultSetup.withCassandra(enabled = persistenceEnabled)
}

trait JdbcPersistence extends Persistence {

  /**
    * Setup for JDBC in-memory persistence.
    */
  override final def setupWithPersistence: Setup = ServiceTest.defaultSetup.withJdbc(enabled = persistenceEnabled)
}