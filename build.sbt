name := "second"

version := "1.0"

scalaVersion := "2.11.8"

mainClass in Compile := Some("com.tincu.akka.Main")

lazy val akkaVersion = "2.4.4"

libraryDependencies ++= Seq(
  "com.github.sstone" % "amqp-client_2.11" % "1.5",
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "org.scalatest" %% "scalatest" % "2.2.6" % "test",
  "junit" % "junit" % "4.12" % "test",
  "com.novocode" % "junit-interface" % "0.11" % "test"
)


