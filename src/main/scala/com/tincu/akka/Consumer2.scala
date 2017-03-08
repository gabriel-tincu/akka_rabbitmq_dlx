package com.tincu.akka

import akka.actor.{Actor, Props, ActorSystem}
import com.github.sstone.amqp.Amqp.{QueueParameters, StandardExchanges, Ack, Delivery}
import com.github.sstone.amqp.{Amqp, Consumer, ConnectionOwner}
import com.rabbitmq.client.ConnectionFactory
import scala.concurrent.duration._

/**
  * Created by gabi on 08.03.2017.
  */
object Consumer2 extends App{
  implicit val system = ActorSystem("mySystem")

  // create an AMQP connection
  val connFactory = new ConnectionFactory()
  connFactory.setUri("amqp://guest:guest@localhost/%2F")
  val conn = system.actorOf(ConnectionOwner.props(connFactory, 1 second))

  // create an actor that will receive AMQP deliveries
  val listener = system.actorOf(Props(new Actor {
    def receive = {
      case Delivery(consumerTag, envelope, properties, body) => {
        val text = new String(body)
        println("got a [DEAD] message: " + text)
        sender ! Ack(envelope.getDeliveryTag)
      }
    }
  }))

  // create a consumer that will route incoming AMQP messages to our listener
  // it starts with an empty list of queues to consume from
  val consumer = ConnectionOwner.createChildActor(conn, Consumer.props(listener, channelParams = None, autoack = false))
  ConnectionOwner.createChildActor(conn, Consumer.props(listener, StandardExchanges.amqDirect,
    QueueParameters(
      name="bar",
      passive = false,
      durable = false,
      exclusive = false,
      autodelete = false,
      args=Map()),
    routingKey = "dead",
    channelParams = None,
    autoack = false))
  // wait till everyone is actually connected to the broker
  Amqp.waitForConnection(system, consumer).await()

  // create a queue, bind it to a routing key and consume from it
  // here we don't wrap our requests inside a Record message, so they won't replayed when if the connection to
  // the broker is lost: queue and binding will be gone

  // create a queue
  //  val queueParams = QueueParameters("foo", passive = false, durable = false, exclusive = false, autodelete = true)
  //  consumer ! DeclareQueue(queueParams)
  //  // bind it
  //  consumer ! QueueBind(queue = "foo", exchange = "amq.direct", routing_key = "my_queue")
  //  // tell our consumer to consume from it
  //  consumer ! AddQueue(QueueParameters(name = "foo", passive = false))

  // run the Producer sample now and see what happens
  println("press enter to kill the dead message processor...")

  System.in.read()
  system.shutdown()
}
