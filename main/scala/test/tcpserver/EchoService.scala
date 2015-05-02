package test.tcpserver

import java.net.InetSocketAddress
import akka.actor.{ActorLogging, Actor, Props}
import akka.io.{Tcp, IO}
import akka.actor.Actor.Receive

/**
 * Created by bjcheny on 6/23/14.
 */
object EchoService {
  def props(endpoint: InetSocketAddress): Props =
    Props(new EchoService(endpoint))
}



class EchoService(endpoint: InetSocketAddress) extends Actor with ActorLogging {

  import context.system

  IO(Tcp) ! Tcp.Bind(self, endpoint)

  override def receive: Receive = {
    case Tcp.Connected(remote, _) =>
      log.debug("Remote address {} connected", remote)
      sender ! Tcp.Register(context.actorOf(EchoConnectionHandler.props(remote, sender)))
  }
}

/*
class EchoService(endpoint: InetSocketAddress) extends Actor with ActorLogging {
  IO(Tcp) ! Tcp.Bind(self, endpoint)

  override def receive: Receive = {
    case Tcp.Connected(remote, _) =>
      log.debug("Remote address {} connected", remote)
      sender ! Tcp.Register(context.actorOf(EchoConnectionHandler.props(remote, sender)))
  }
}
*/