//
//
//import akka.http.scaladsl.model.{ ContentTypes, HttpEntity }
//import akka.http.scaladsl.server.HttpApp
//import akka.http.scaladsl.server.Route
//
//// Server definition
//object WebServer extends HttpApp {
//  override def routes: Route = path("order" / IntNumber) { id ⇒
//    get {
//      complete {
//        "Received GET request for order " + id
//      }
//    } ~
//      put {
//        complete {
//          "Received PUT request for order " + id
//        }
//      }
//  }
//}