package modules

import mysql._
import com.gravitydev.scoop._
import query._
import java.time.{Instant, ZoneId}
import play.api.Play.current
import play.api.db.DB
import models._
/**
  * Created by brandonmott1 on 4/23/16.
  */
class UserModule (protected val ctx: Context) extends ContextOps{
  
  def byId(userId: UserId) = DB.withConnection{ implicit conn => 
    using(tables.`users`){u =>
      from(u)
        .where(u.id === userId)
        .find(models.Parsers.user(u))
        .headOption
    }
  } 
}
