package actors

import akka.actor.{ActorRef, Props, ActorLogging, Actor,PoisonPill}
import play.api.libs.concurrent.Akka
import play.api.libs.iteratee.{Enumerator, Iteratee, Concurrent}
import play.api.libs.json.{Json, JsValue}
import play.api.mvc.WebSocket.FrameFormatter
import play.api.mvc._
import play.api.Play.current
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by brandonmott1 on 12/10/15.
  */
class DBActor extends Actor{
  def receive = {
    case st: String => sender ! Json.toJson(st + " now You're inside  DBActor")
  }
}

trait DBResponse {
  def toJson: JsValue
}

/**
  * WebSocketChannel is an actor, which communicates with the DBActor and its companion object
  * @param out 
  */
class WebSocketActor(out: ActorRef) extends Actor with ActorLogging{
  val db = Akka.system.actorOf(Props(classOf[DBActor]))

  /**
    * convertJson translates JsValue to the format that is understood by the DBActor.
    * WebSocket can get any of the following messages:
    * 1. Connection request: It is a message that shows the information required to connect to a database 
    *       such as a host, port, user ID, and password.
    * 2. Query string: It is the query to be executed in the database
    * 3. Disconnect request: It is a message that closes a connection with the database
    * @param j
    * @return Some format that is understood by DBActor
    */
  def convertJson(j: JsValue) = {
    j\\"msg"
  }
  
  def receive: Actor.Receive = {
    case x: String => 
      db ! (x.reverse + " You're inside WebSocketActor")
    case jsRequest: JsValue =>
      out ! Json.stringify(jsRequest)
    case x: DBResponse =>
      out ! x.toJson
  }
  
  override def postStop() = {
    db ! PoisonPill
  }
}

object WebSocketActor{
  def props(out: ActorRef): Props = Props(classOf[WebSocketActor], out)
}

