package controllers

import play.api.Play.current
import com.typesafe.scalalogging.StrictLogging
import scala.concurrent.Future
import play.api.mvc._
import play.api._
import play.api.mvc.WebSocket.FrameFormatter
import scala.concurrent.ExecutionContext.Implicits.global
import akka.actor._
import play.api.libs.json._
import actors.WebSocketActor
/**
  * Created by brandonmott1 on 12/10/15.
  */
object Messanger extends Controller with BaseController with StrictLogging {
  
  /**
    * This class is equivalent to incoming JSON for every request to `def webSocket` and FrameFormatter used to translate 
    * JSON message automatically. The Request type can be following messages:
    * 1. `Connection request`: It is a message that shows the information required to connect to a database 
    *       such as a host, port, user ID, and password.
    * 2. `Query string`: It is the query to be executed in the database
    * 3. `Disconnect request`: It is a message that closes a connection with the database
    * @param requestType Request Type of the following: Connection request, Query string, or Disconnect request
    * @param message message sent to the DB Actor
    */
  case class WebSocketRequest(requestType:String, message:String)

  /**
    * The WebSocket methods do not validate the format of data received automatically in the same manner 
    * as Action parsers. We will need to do this additionally, if required.
    */
  implicit val requestFormat = Json.format[WebSocketRequest]
  /**
    * FrameFormatter is a helper and can convert org.jboss.netty.handler.codec.http.websocketx.WebSocketFrame 
    * to play.core.server.websocket.Frames
    */
  implicit val requestFrameFormatter = FrameFormatter.jsonFrame[WebSocketRequest]

  /**
    *  WebSocket to translate the JSON message to a WebSocketRequest automatically. This is possible by 
    *  specifying the data type for the acceptWithActor method
    * @return The response received from the DBActor can be one of the following:
    *     1. A successful connection
    *     2. Connection failure
    *     3. Query result
    *     4. Invalid query
    *     5. Disconnected
    */                           
  def webSocket = WebSocket.acceptWithActor[String, String] {//TODO: Add WebSocketRequest and create a Framed Response
    request => out =>
      WebSocketActor.props(out)
      /** use this to reject a WebSocket request 
      Future.successful(request.session.get("user") match {
          case None => Left(Forbidden)
          case Some(_) => Right(WebSocketActor.props)
        })
      */
  }
  
}
