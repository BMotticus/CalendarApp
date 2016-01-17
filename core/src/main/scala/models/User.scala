package models

case class User (
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

case class SignInData(email: String, password:String)



