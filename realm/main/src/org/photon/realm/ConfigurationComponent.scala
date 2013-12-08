package org.photon.realm

import com.typesafe.config.Config

trait ConfigurationComponent {
	val config: Config
}
