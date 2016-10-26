package client

import akka.actor._
import akka.cluster.client.{ ClusterClient, ClusterClientSettings }

import com.typesafe.config.ConfigFactory

import cfabcast.messages._

object ClientMain {
  //TODO: pass registerInterval as optional parameter
  def main(args: Array[String]): Unit = {
    val clientId = args(0).toInt
    val clientName = s"client-${clientId}"
    val defaultConfig = ConfigFactory.load()

    val appConfig = defaultConfig.getConfig("app")
    val hostname = appConfig.getString("host")
    val port = appConfig.getString("port")

    val config = ConfigFactory.parseString(s"""
      akka.remote.netty.tcp {
        hostname = ${hostname}
        port = ${port}
      }
    """).withFallback(defaultConfig)

    val system = ActorSystem("ClientSystem", config)

    println(s"Client ${clientId} running on ${hostname}:${port}")

    val contactList = config.getStringList("contact-points")
    val chosenContact = clientId % contactList.size
    val initialContacts = Set(ActorPath.fromString(contactList.get(chosenContact) + "/system/receptionist"))
    println(s"CONTACTS: MyId: ${clientId}, Chosen: ${chosenContact}, Size: ${contactList.size} addr: ${contactList.get(chosenContact)}")

    val clusterClient = system.actorOf(ClusterClient.props(
      ClusterClientSettings(system).withInitialContacts(initialContacts)), "clusterClient")

    val client = system.actorOf(ClientActor.props(clientName, clusterClient), clientName)

    client ! StartConsole
  }
}
