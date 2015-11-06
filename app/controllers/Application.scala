package controllers

import models._
import play.api.data.validation.Constraints._
import com.typesafe.scalalogging.StrictLogging
import scala.language.postfixOps
import play.api._
import play.api.mvc._
import org.joda.time.{LocalDate, DateTime, DateTimeZone}
import play.api.data.Form
import play.api.data.Forms._
import app.mysql._
import com.gravitydev.scoop._, query._
import play.api.Play.current
import play.api.db.DB
import java.sql.Connection

object Application extends Controller with StrictLogging {

  val userForm = Form(
    mapping(
      "firstName" -> text,
      "lastName" -> text,
      "username" -> nonEmptyText(minLength = 6),
      "email" -> email,
      "password" -> text(minLength = 8),
      "password2" -> nonEmptyText
    )(UserData.apply)(UserData.unapply).verifying("Passwords must match", fields => fields.password == fields.password2 )
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
    Ok(views.html.signUp(userForm.fill(UserData("","","","","",""))))
  }
  
  def doSignUp = Action {implicit r =>
    userForm.bindFromRequest().fold(
      hasErrors => {
        BadRequest(views.html.signUp(hasErrors))
      }, 
      success => {
        //TODO: Save to DB
        val userId = DB.withConnection(implicit conn =>
          using(tables.users){u => 
            .insertInto(u)
            .values(
              u.firstName := success.firstName
              u.lastName  := success.lastName
              u.userName  := success.userName
              u.email     := success.email
            )
          }().get
        )
        Redirect(routes.Application.index)
      }
    )
  }
}