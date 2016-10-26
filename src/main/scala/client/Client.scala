package client

import akka.actor._
import akka.actor.ExtendedActorSystem
import akka.actor.ActorLogging
import akka.cluster.client.ClusterClient

import scala.concurrent.duration._
import scala.util.Random

import cfabcast.messages._
import cfabcast.serialization.CFABCastSerializer

trait Client extends ActorLogging {
  this: ClientActor =>

  val serializer = new CFABCastSerializer(context.system.asInstanceOf[ExtendedActorSystem])
  val console = context.actorOf(Props[ConsoleClient], "console")

  def clientBehavior(proposers: Set[ActorRef]): Receive = {
    case StartConsole => console ! StartConsole

    case Command(cmd: String) =>
      log.info("Received COMMAND {} ", cmd)
      cmd match {
        case "all" =>
          proposers.zipWithIndex.foreach { case (ref, i) =>
            ref ! Broadcast(serializer.toBinary(cmd ++ "_" ++ i.toString))
          }
        case _ =>
          if(proposers.nonEmpty) {
            val data = serializer.toBinary(cmd)
            proposers.toVector(Random.nextInt(proposers.size)) ! Broadcast(data)
          } else {
            log.warning("Ops!! There are no proposer registered with this client-{}.", id)
          }
      }

    case msg: ClientRegistered =>
      log.info("Client {} registered with {}", id, msg.proposer)
      registerTask.cancel()
      context.become(clientBehavior(proposers + msg.proposer))
  }
}

class ClientActor(val id: String, val clusterClient: ActorRef, registerInterval: FiniteDuration) extends Actor with Client {

  import context.dispatcher
  val registerTask = context.system.scheduler.schedule(0.seconds, registerInterval, clusterClient,
    ClusterClient.Send("/user/node", RegisterClient(self), localAffinity = true))

  override def postStop(): Unit = registerTask.cancel()
  def receive = clientBehavior(Set.empty[ActorRef])
}

object ClientActor {
  def props(id: String, clusterClient: ActorRef, registerInterval: FiniteDuration = 10.seconds) : Props =
    Props(new ClientActor(id, clusterClient, registerInterval))
}
