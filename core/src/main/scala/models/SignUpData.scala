package models

import java.time.ZoneId

case class SignUpData (
  companyName: String,
  email: String,
  password: String,
  timezone: ZoneId
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