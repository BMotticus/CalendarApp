package controllers

import models._
import play.api.data.validation.Constraints._
import com.typesafe.scalalogging.StrictLogging
import scala.language.postfixOps
import play.api.Play.current
import play.api._
import play.api.mvc._
import play.api.db.DB
import play.api.libs.json
import play.api.libs.concurrent.Akka
import org.joda.time.{LocalDate, DateTime, DateTimeZone}
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.data._
import play.api.data.validation._
import scala.concurrent.Future
import play.twirl.api.Html
import org.apache.commons.codec.binary.Hex

object Application extends Controller with StrictLogging {

  val userForm = Form(
    tuple(
      "firstName" -> text,
      "lastName" -> text,
      "username" -> nonEmptyText(minLength = 6),
      "email" -> email,
      "password" -> text(minLength = 8),
      "password2" -> nonEmptyText
    ).verifying("Passwords must match", fields => fields match { case (_, _, _, _, pass1, pass2) => pass1 == pass2 })
  )
  
  case class UserData(
    firstName: String,
    lastName: String,
    userName: String,
    email: String,
    password: String,
    password2: String
  )  
  
  

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }
  
  def signUp = Action {
    Ok(views.html.signUp(userForm.fill("","","","","","")))
  }
}