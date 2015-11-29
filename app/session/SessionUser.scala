package session

import models._
import scala.language.postfixOps

/**
  * Created by brandonmott1 on 11/24/15.
  */
trait SessionUser {
  val signedIn: Boolean
}

object SessionUser {
  def parse(data: Map[String, String]): Option[SignedInUser] = for {
    userId            <- data.get("user-id").map(_.toLong)
    email             <- data.get("user-email")
    username          <- data.get("user-username")
  } yield SignedInUser(
    User(userId, username, email)
  )
}

case class AnonymousUser () extends SessionUser {
  val signedIn = false
}

case class SignedInUser (
  user: User
) extends SessionUser {
  val signedIn = true
  
  val data: Map[String,String] = Map(
    "user-id" -> user.id.toString,
    "user-email" -> user.email,
    "user-username" -> user.username
  )
}