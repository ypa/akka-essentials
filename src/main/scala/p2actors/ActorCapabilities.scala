package p2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ActorCapabilities extends App {

  class SimpleActor extends Actor {
    override def receive: Receive = {
      case "Hi!" => context.sender() ! "Hello, there! back to you"  // Replying  Hi! message back to the sender
      case message: String => println(s"[${self}] I have received $message")
      case number: Int => println(s"[simple actor] I have received a NUMBER: $number")
      case SpecialMessage(contents) => println(s"[simple actor] I have received something SPECIAL: $contents")
      case SendMessageToYourself(content) =>
        self ! s"Note to self: $content"
      case SayHiTo(ref) => ref ! "Hi!"
      case RelayMessage(content, ref) => ref forward (content + "sss") // keep the original sender of RelayMessage
    }
  }

  val system = ActorSystem("actorCapabilitiesDemo")
  val simpleActor = system.actorOf(Props[SimpleActor], "simpleActor")

  simpleActor ! "Hi there, actor"

  // messages can be of any type
  // messages must be IMMUTABLE
  // messages must be Serializable (Java interface)
  simpleActor ! 42

  // in practice use case classes and case objects
  case class SpecialMessage(contents: String)
  simpleActor ! SpecialMessage("some special content")

  // actors have information about their context and about themselves
  // context.self (or just self) === `this` in oop

  case class SendMessageToYourself(content: String)
  simpleActor ! SendMessageToYourself("I am an actor and I am proud of it")

  // actors can reply to messages
  val alice = system.actorOf(Props[SimpleActor], "alice")
  val bob = system.actorOf(Props[SimpleActor], "bob")

  case class SayHiTo(ref: ActorRef)
  alice ! SayHiTo(bob)

  // dead letters
  alice ! "Hi!" // Alice will attempt to reply back to noSender resulting in "dead letters"

  // forwarding messages - sending a message with original sender
  case class RelayMessage(content: String, ref: ActorRef)
  alice ! RelayMessage("Hi", bob)
}
