package org.photon.login

import org.scalatest.{ShouldMatchers, FreeSpec}
import java.security.MessageDigest
import java.nio.charset.Charset

class UserAuthenticationComponentImpl$Test extends FreeSpec with ShouldMatchers {
	implicit val charset = Charset.forName("UTF-8")
	implicit val digest = MessageDigest.getInstance("SHA-512")

	"UserAuthenticationComponentImpl" - {
		".tohex" - {
			import UserAuthenticationComponentImpl.tohex

			"should convert a string to its hexadecimal form" in {
				tohex("hello".getBytes(charset)) should === ("68656c6c6f")
			}
		}

		".hash" - {
			import UserAuthenticationComponentImpl.hash

			"should hash a string and convert it to hex" in {
				hash("test") should === ("ee26b0dd4af7e749aa1a8ee3c10ae9923f618980772e473f8819a5d4940e0db27ac185f8a0e1d5f84f88bc887fd67b143732c304cc5fa9ad8e6f57f50028a8ff")
			}
		}

		".encrypt" - {
			import UserAuthenticationComponentImpl.encrypt

			"should encrypt password using salt" in {
				encrypt(clear = "test", salt = "test") should === ("667d4473a6229ae96c50e477e1664bdf849223dd2bb847ff12b52de2dbab695abee6318151052ba4eefc19b9a339ad62550e643382ec60f24e510905763eb3ab")
			}
		}
	}
}
