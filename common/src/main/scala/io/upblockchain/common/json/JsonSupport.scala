package io.upblockchain.common.json

import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import org.json4s.{ Formats, native }
import org.json4s.ext.JodaTimeSerializers
import org.json4s.DefaultFormats
import java.text.SimpleDateFormat

trait JsonSupport extends Json4sSupport {

  implicit val serialization = native.Serialization
  implicit def json4sFormats: Formats = DefaultFormats // customDateFormat ++ JodaTimeSerializers.all

  //  val customDateFormat = new DefaultFormats {
  //    override def dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
  //  }

}