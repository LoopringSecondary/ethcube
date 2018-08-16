package io.upblockchain.common.json

import org.json4s._
import org.json4s.native.Serialization

import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import org.json4s.ext.JodaTimeSerializers
import io.upblockchain.common.json._

trait JsonSupport extends Json4sSupport {

  implicit val serialization = Serialization //.formats(NoTypeHints)
  implicit def json4sFormats: Formats = new StringOptionFormats() ++ JodaTimeSerializers.all

}