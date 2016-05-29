package utils

import java.time.ZoneId

import play.api.libs.json._
import models._
import play.api.libs.functional.syntax._

trait JsonFormats {
  
  val longF = implicitly[Format[Long]]

  implicit lazy val accountIdF = longF.inmap(AccountId.apply, (_:AccountId).underlying)
  implicit lazy val storeIdF = longF.inmap(StoreId.apply, (_:StoreId).underlying)
  implicit lazy val userIdF = longF.inmap(UserId.apply, (_:UserId).underlying)
  
  implicit val timeZoneF = new Format[ZoneId]{
    def reads(j: JsValue) = j.validate[String].map(ZoneId.of)
    def writes(tz: ZoneId) = Json.toJson(tz.getId)
  }
  
  implicit val addressF = Json.format[Address]
  implicit val accountInfoF = Json.format[AccountInfo]
  implicit val storeInfoF = Json.format[StoreInfo]
  implicit val userInfoF = Json.format[UserInfo]
}

object JsonFormats extends JsonFormats