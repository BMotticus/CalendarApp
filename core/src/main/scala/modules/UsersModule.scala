package modules

import mysql._
import com.gravitydev.scoop._, query._
import org.joda.time.DateTime
import play.api.Play.current
import play.api.db.DB
import java.sql.Connection
import models._


class UsersModule(protected val ctx: Context) extends ContextOps{
  
  def createUser(user: UserData): Long = {
    println("Creating user")
    DB.withConnection{ implicit conn =>
      using(tables.users){u => 
      insertInto(u)
        .values(
          u.email     := user.email,
          u.password  := user.password1,
          u.created_date := DateTime.now
        )().get
      }
    }
  }
  
  def findUserById(userId: Long): models.User = {
    DB.withTransaction{implicit conn =>
      using (tables.users) {u => 
         from(u)
          .where(u.id === userId)
          .find(models.Parsers.user(u))
          .headOption getOrElse sys.error("No User found for id: " + userId)
      }
    }
  }
  
  def createMessage(data: ContactData): Long = {
    DB.withConnection{ implicit conn =>
      using(tables.messages) {m =>
        insertInto(m)
          .values(
            m.sender_info := data.sender,
            m.description := Option(data.about).filter(_.nonEmpty),
            m.message := data.message,
            m.respond_info := Option(data.respond).filter(_.nonEmpty),
            m.sent_date := DateTime.now 
          )().get
      }
    }
  }
  
  def checkSignInCredentails(email: String,password: String): Option[models.User] = {
    DB.withTransaction{ implicit conn => 
      using (tables.users) {u => 
        from(u)
          .where(u.email === email && u.password === password)
          .find(models.Parsers.user(u))
          .headOption
      }
    }
  }
  
  def signInWithCredentials(email: String, password:String): Option[session.SignedInUser] = {
    DB.withTransaction{ implicit conn => 
      using (tables.users) {u => 
        from(u)
          .where(u.email === email && u.password === password)
          .find(models.Parsers.user(u) >> session.SignedInUser.apply)
          .headOption
      }
    }
  }
}
