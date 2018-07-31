package io.upblockchain.worker.json

import org.json4s.ext.JodaTimeSerializers
import org.json4s.{ DefaultFormats, Formats, native }
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import java.text.SimpleDateFormat

trait JsonSupport extends Json4sSupport {

  implicit val serialization = native.Serialization

  implicit def json4sFormats: Formats = customDateFormat ++ JodaTimeSerializers.all

  val customDateFormat = new DefaultFormats {
    override def dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
  }

}