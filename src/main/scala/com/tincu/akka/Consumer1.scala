package com.github.sstone.amqp.samples

import akka.actor.{Props, Actor, ActorSystem}
import com.github.sstone.amqp._
import com.github.sstone.amqp.Amqp._
import com.rabbitmq.client.ConnectionFactory
import scala.concurrent.duration._

/**
  * simple Consumer sample
  * to run the sample:
  * mvn exec:java -Dexec.classpathScope="test" -Dexec.mainClass=com.github.sstone.amqp.samples.Consumer1
  */
object Consumer1 extends App {
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
        println("got a message: " + text)
        // if we only ack on clean treating, restarting this will kill it again
        // if we ack anyway (which is what happens in our case i think) then the message gets dequeued,
        // but code that should generate the report no longer gets executed
        // solution is to basic.reject on failure i think, then treat it on app restart
        
//        sender ! Ack(envelope.getDeliveryTag)
        if (!text.contains("fail")){
          sender ! Ack(envelope.getDeliveryTag)
        } else {
          throw new OutOfMemoryError("failure")
        }
      }
    }
  }))

  // create a consumer that will route incoming AMQP messages to our listener
  // it starts with an empty list of queues to consume from
  val a = Map("x-dead-letter-routing-key" -> "dead", "x-dead-letter-exchange" -> "amq.direct")
  val consumer = ConnectionOwner.createChildActor(conn, Consumer.props(listener, channelParams = None, autoack = false))
  ConnectionOwner.createChildActor(conn, Consumer.props(listener, StandardExchanges.amqDirect,
    QueueParameters(
      name="foo2",
      passive = false,
      durable = false,
      exclusive = false,
      autodelete = false,
      args=a),
    routingKey = "alive",
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
  println("press enter...")

  System.in.read()
  system.shutdown()
}