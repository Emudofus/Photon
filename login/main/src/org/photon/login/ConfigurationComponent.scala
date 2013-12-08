package org.photon.login

import com.typesafe.config.Config

trait ConfigurationComponent {
	val config: Config
}
