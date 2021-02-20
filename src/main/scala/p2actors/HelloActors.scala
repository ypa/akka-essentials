package p2actors

import akka.actor.{Actor, ActorSystem, Props}

object HelloActors extends App {

  // actor systems
  val actorSystem = ActorSystem("firstActorSystem")
  println(actorSystem.name)

  // create actors
  // word count actor
  class WordCountActor extends Actor {
    var totalWords = 0

    // behavior
    def receive: PartialFunction[Any, Unit] = {
      case message: String =>
        println(s"[Word Counter] I have received: ${message}")
        totalWords += message.split(" ").length
      case msg => println(s"[Word Counter] I cannot understand ${msg.toString}")
    }
  }

  // instantiate actor
  // new WordCountActor // not allowed cannot instantiate like that
  val wordCounter = actorSystem.actorOf(Props[WordCountActor], "wordCounter")
  val anotherWordCounter = actorSystem.actorOf(Props[WordCountActor], "anotherWordCounter")

  // communicate with actor by sending message (this is the only way)
  wordCounter ! "What's up Akka" // sending message is asynchronous
  anotherWordCounter ! "Hi there Akka"

  // Actor with constructor param
  // Eg.:
  class Person(name: String) extends Actor {
    override def receive: Receive = {  // Receive is type alias to PartialFunction[Any, Unit]
      case "hi" => println(s"[Person] Hi, my name is ${name}")
      case _ =>
    }
  }

  val person = actorSystem.actorOf(Props(new Person("Bob")))  // this is legal but not a good practice
  person ! "hi"

  // best practice for instantiating actors with constructor args is to define a companion object
  // with method that returns Props object like so:
  object Person {
    def props(name: String): Props = Props(new Person(name))
  }

  val person2 = actorSystem.actorOf(Person.props("Robert"))
  person2 ! "hi"

}
