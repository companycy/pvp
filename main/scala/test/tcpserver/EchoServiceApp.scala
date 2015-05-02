package test.tcpserver

import akka.actor.ActorSystem
import java.net.InetSocketAddress

/**
 * Created by bjcheny on 6/23/14.
 */
object EchoServiceApp {
  val system = ActorSystem("echo-service-system")
  val endpoint = new InetSocketAddress("localhost", 11111)
  system.actorOf(EchoService.props(endpoint), "echo-service")

  readLine(s"Hit ENTER to exit ...${System.getProperty("line.separator")}")
  system.shutdown()
}
