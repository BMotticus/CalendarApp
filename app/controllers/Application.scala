package controllers

import play.api._
import models._
import play.api.data.validation.Constraints._
import scala.language.postfixOps
import play.twirl.api.Html
import play.api.Play.current
import play.api.mvc._
import play.api.data._, format.Formats._, Forms._
import play.api.db.DB
import play.api.libs.concurrent.Execution.Implicits._
import data._


object Application extends Controller {

  val signUpForm = Form(
    tuple(
      "email" -> email,
      "password" -> text(minLength = 8),
      "password2" -> nonEmptyText
    ) verifying("Passwords must match", fields => fields match {
      case (_, pass1, pass2) => pass1 == pass2
    })
  )
  
  case class AccountData(
    firstName: String,
    lastName: String,
    email: String,
    street: String,
    city: String,
    state: String,
    zip: String,
    phone: String
  )  
  
  val accountForm = Form(
    mapping(
      "firstName" -> nonEmptyText,
      "lastName" -> nonEmptyText,
      "email" -> email,
      "street" -> text,
      "city" -> text,
      "state" -> text,
      "zip" -> text,
      "phone" -> text
    )(AccountData.apply)(AccountData.unapply)
  )

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

}