package modules

import mysql._
import com.gravitydev.scoop._, query._
import java.time.Instant
import play.api.Play.current
import play.api.db.DB
import java.sql.Connection
import models._


class UsersModule(protected val ctx: Context) extends ContextOps{
  
  def createUser(accountId: Long, storeId: Long, email: String, password: String, role: String): Long = {
    DB.withConnection{ implicit conn =>
      using(tables.users) { u =>
        insertInto(u)
          .values(
            u.account_id := accountId,
            u.email := email,
            u.password := password,
            u.store_id := storeId,
            u.created_date := Instant.now,
            u.deleted := false,
            u.role := role
          )().get
      }
    }
  }
  
  def findUserById(userId: Long): models.UserInfo = {
    DB.withTransaction{implicit conn =>
      using (tables.users) {u => 
         from(u)
          .where(u.id === userId)
          .find(models.Parsers.userInfo(u))
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
            m.sent_date := Instant.now 
          )().get
      }
    }
  }
  
  def checkSignInCredentails(email: String, password: String): Option[models.UserInfo] = {
    DB.withTransaction{ implicit conn => 
      using (tables.users) {u => 
        from(u)
          .where(u.email === email && u.password === password)
          .find(models.Parsers.userInfo(u))
          .headOption
      }
    }
  }
  
  def signInWithCredentials(email: String, password:String): Option[session.SignedInUser] = {
    DB.withTransaction{ implicit conn => 
      using (tables.accounts, tables.stores, tables.users) {(a,s,u) => 
        from(u)
          .innerJoin(s on u.store_id === s.id)
          .innerJoin(a on s.account_id === a.id)
          .where(u.email === email && u.password === password)
          .find(models.Parsers.userInfo(u) ~ models.Parsers.storeInfo(s) ~ models.Parsers.accountInfo(a) >> session.SignedInUser.apply)
          .headOption
      }
    }
  }
}
