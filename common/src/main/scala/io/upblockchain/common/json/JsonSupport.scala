//package io.upblockchain.common.json
//
//trait JsonSupport extends Json4sSupport {
//
//  implicit val serialization = native.Serialization
//
//  implicit def json4sFormats: Formats = customDateFormat ++ JodaTimeSerializers.all // ++ EnumSerializers.all
//
//  val customDateFormat = new DefaultFormats {
//    override def dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
//  }
//
//}