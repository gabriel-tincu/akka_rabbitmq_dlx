package com.tincu.akka

import akka.actor.ActorSystem
import com.github.sstone.amqp.{ChannelOwner, ConnectionOwner, Amqp}
import com.github.sstone.amqp.Amqp._
import com.rabbitmq.client.ConnectionFactory
import com.github.sstone.amqp.Amqp.Publish
import java.util.concurrent.TimeUnit
import scala.concurrent.duration._

/**
  * Created by gabi on 08.03.2017.
  */
object Producer {
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("mySystem")

    val connFactory = new ConnectionFactory()
    connFactory.setUri("amqp://guest:guest@localhost/%2F")
    val conn = system.actorOf(ConnectionOwner.props(connFactory, 1 second))
    val producer = ConnectionOwner.createChildActor(conn, ChannelOwner.props())

    // wait till everyone is actually connected to the broker
    waitForConnection(system, conn, producer).await(5, TimeUnit.SECONDS)

    // send a message
    producer ! Publish("amq.direct", "alive", "yo!!".getBytes, properties = None, mandatory = true, immediate = false)

    Thread.sleep(500)
    system.shutdown()
  }

}
