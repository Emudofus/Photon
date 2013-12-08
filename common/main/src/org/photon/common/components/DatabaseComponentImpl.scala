package org.photon.common.components

import java.sql.{Connection, DriverManager}
import com.typesafe.scalalogging.slf4j.Logger
import org.slf4j.LoggerFactory

trait DatabaseComponentImpl extends DatabaseComponent {

	private val logger = Logger(LoggerFactory getLogger classOf[DatabaseComponentImpl])

	val databaseUrl: String
	val databaseDriver: String

	implicit val database: Connection = {
		Class.forName(databaseDriver)
		val connection = DriverManager.getConnection(databaseUrl)
		logger.info(s"successfully connected to database")
		connection
	}

}
