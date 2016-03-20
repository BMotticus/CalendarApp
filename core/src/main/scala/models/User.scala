package models
import play.api.libs.json._
import java.time.Instant

case class User(
  id: Long,
  accountId: Long,
  email: String,
  firstName: Option[String],
  lastName: Option[String],
  role: String,
  storeId: Long,
  createdDate: Instant,
  settings: String
)

case class UserInfo (
  id: Long,
  email: String
)


/*
case class ClientConfig(
  web: JsValue
)

 */