package test

/**
 * Created by bjcheny on 6/12/14.
 */
import akka.actor._
//import akka.camel.{ Consumer, CamelMessage }
//
//class Ser extends Consumer {
//  def endpointUri = "mina2:tcp://localhost:9002"
//  def receive = {
//    case message: CamelMessage => {
//      //log
//      println("looging, question:" + message)
//      sender ! "server response to request: " + message.bodyAs[String] + ", is NO"
//    }
//    case _ => println("I got something else!??!!")
//  }
//}

object Ser extends App {

  val system = ActorSystem("some")
//  val spust = system.actorOf(Props[Ser])
}
