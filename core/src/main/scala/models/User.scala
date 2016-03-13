package models
import play.api.libs.json._
import java.time.Instant

case class User(
  id: Long,
  accountId: Long,
  email: String,
  firstName: String,
  lastName: String,
  role: String,
  storeId: Long,
  createdDate: Instant,
  settings: String,
  deleted: Boolean,
  address: Address
)

case class UserInfo (
  id: Long,
  email: String
)

case class UserData(
  email: String,
  password1: String,
  password2: String
)

case class ContactData(
  sender: String,
  about: String,
  message: String,
  respond: String
)

case class SignInData(
 email: String,
 password:String
)

/*
case class ClientConfig(
  web: JsValue
)

case class `users`() extends Table[`users`](`users`) {
    val `id`                        = col[Long]                  (Symbol("id"))                
    val `account_id`                = col[Long]                  (Symbol("account_id"))        
    val `email`                     = col[String]                (Symbol("email"))             
    val `first_name`                = col[String]                (Symbol("first_name"))        nullable
    val `last_name`                 = col[String]                (Symbol("last_name"))         nullable
    val `password`                  = col[String]                (Symbol("password"))          
    val `role`                      = col[String]                (Symbol("role"))              
    val `store_id`                  = col[Long]                  (Symbol("store_id"))          
    val `created_date`              = col[org.joda.time.DateTime] (Symbol("created_date"))      
    val `settings`                  = col[String]                (Symbol("settings"))          
    val `deleted`                   = col[Int]                   (Symbol("deleted"))           
    val `confirmed_date`            = col[org.joda.time.DateTime] (Symbol("confirmed_date"))    nullable
    val `email_verification_token`  = col[String]                (Symbol("email_verification_token")) nullable
  }
 */