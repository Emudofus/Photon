package org.photon.protocol.photon

import com.twitter.util.Time

case class UserInfos(id: Long, nickname: String, secretAnswer: String, subscriptionEnd: Time)
