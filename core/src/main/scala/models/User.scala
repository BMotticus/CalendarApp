package models
import play.api.libs.json._
import java.time.Instant

case class User(
  id: UserId,
  accountId: AccountId,
  email: String,
  firstName: Option[String],
  lastName: Option[String],
  role: String,
  storeId: StoreId,
  createdDate: Instant,
  settings: String
)

case class UserInfo (
  id: UserId,
  email: String
)
