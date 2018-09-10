package org.loopring.ethcube.common.json

import org.json4s._
import org.json4s.native.Serialization

import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import org.json4s.ext.JodaTimeSerializers

trait JsonSupport extends Json4sSupport {

  implicit val serialization = Serialization //.formats(NoTypeHints)
  implicit def json4sFormats: Formats = new StringOptionFormats() ++ JodaTimeSerializers.all // + IntegerSerializer

}

private[json] class StringOptionFormats extends DefaultFormats {
  override val strictOptionParsing: Boolean = true
}
// TODO(Toan) 添加 JString => int 的 支持
//case object IntegerSerializer extends CustomSerializer[Int](format ⇒ (
//  {
//    case JInt(i) ⇒ i.toInt
//    case JString(i) ⇒ i.toInt
//    case JNull ⇒ Int.MinValue
//  },
//  {
//    case i: Int ⇒ JInt(i)
//  }))