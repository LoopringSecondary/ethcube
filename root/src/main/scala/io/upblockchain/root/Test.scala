//package io.upblockchain.root
//
//import org.json4s._
//import org.json4s.native.JsonMethods._
//import io.upblockchain.common.model.JsonRPCResponseWrapped
//import org.json4s.ext.JodaTimeSerializers
//import org.json4s.jackson.Serialization._
//
//object Test extends App {
//
//  class StringOptionFormats extends DefaultFormats {
//    override val strictOptionParsing: Boolean = true
//  }
//
//  implicit val formats = new StringOptionFormats() ++ JodaTimeSerializers.all
//
//  val json = """
//{"jsonrpc":"2.0","id":70, "error":{"code":-32700,"message":"missing request id"}}
//"""
//
//  val d = read[JsonRPCResponseWrapped](json)
//
//  println(write(d))
//
//  //  val d = parse(json).extract[JsonRPCResponseWrapped]
//  //
//  //  println(d)
//
//}