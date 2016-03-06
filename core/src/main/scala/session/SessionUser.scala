package session

import models._
import scala.language.postfixOps

trait SessionUser {
  val signedIn: Boolean
}

object SessionUser {
  def parse(data: Map[String, String]): Option[SignedInUser] = for {
    userId            <- data.get("user-id").map(_.toLong)
    email             <- data.get("user-email")
  } yield SignedInUser(
    User(userId, email)
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
    "user-email" -> user.email
  )
}