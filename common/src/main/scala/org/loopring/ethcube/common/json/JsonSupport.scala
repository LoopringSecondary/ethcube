package org.loopring.ethcube.common.json

import org.json4s._
import org.json4s.native.Serialization

import de.heikoseeberger.akkahttpjson4s.Json4sSupport

trait JsonSupport extends Json4sSupport {

  implicit val serialization = Serialization
  implicit def json4sFormats: Formats = DefaultFormats

}