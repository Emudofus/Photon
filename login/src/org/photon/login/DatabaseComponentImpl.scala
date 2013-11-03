package org.photon.login

import java.sql.{Connection, DriverManager}
import com.typesafe.scalalogging.slf4j.Logger
import org.slf4j.LoggerFactory

trait DatabaseComponentImpl extends DatabaseComponent { self: ConfigurationComponent =>

  private val logger = Logger(LoggerFactory getLogger classOf[DatabaseComponentImpl])

  val databaseConfiguration = config.getConfig("photon.database")
  val databaseUrl = databaseConfiguration.getString("url")
  val databaseDriver = databaseConfiguration.getString("driver")

  implicit val database: Connection = {
    Class.forName(databaseDriver)
    val connection = DriverManager.getConnection(databaseUrl)
    logger.info(s"successfully connected to database")
    connection
  }

}
