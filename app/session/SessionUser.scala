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
    userId            <- data.get("user-id")
    email             <- data.get("user-email")
    username          <- data.get("user-username")
  } yield SignedInUser(
    User(userId.toLong, username, email)
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
    "user-id" -> s"$user.id",
    "user-email" -> user.email,
    "user-username" -> user.username
  )
}