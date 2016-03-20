package session

import models._
import scala.language.postfixOps
import java.time.ZoneId

trait SessionUser {
  val signedIn: Boolean
}

object SessionUser {
  def parse(data: Map[String, String]): Option[SignedInUser] = for {
    userId            <- data.get("user-id").map(_.toLong)
    email             <- data.get("user-email")
    storeId           <- data.get("store-id").map(_.toLong)
    timezone          <- data.get("timezone").map(ZoneId.of)
    accountId         <- data.get("account-id").map(_.toLong)
    companyName       <- data.get("company-name")
  } yield SignedInUser(
    UserInfo(userId, email),
    StoreInfo(storeId, timezone),
    AccountInfo(accountId, companyName)
  )
}

case class AnonymousUser () extends SessionUser {
  val signedIn = false
}

case class SignedInUser (
  user: UserInfo,
  store: StoreInfo,
  account: AccountInfo
) extends SessionUser {
  val signedIn = true
  
  val data: Map[String,String] = Map(
    "user-id" -> user.id.toString,
    "user-email" -> user.email,
    "store-id" -> store.id.toString,
    "timezone" -> store.timezone.toString,
    "account-id" -> account.id.toString,
    "company-name" -> account.name
  )
}