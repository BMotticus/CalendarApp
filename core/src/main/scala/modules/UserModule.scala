package modules

import mysql._
import com.gravitydev.scoop._
import query._
import java.time.{Instant, ZoneId}
import play.api.Play.current
import play.api.db.DB
import models._


class UserModule (protected val ctx: Context) {
  
  def byId(userId: UserId) = DB.withConnection{ implicit conn => 
    using(tables.`users`){u =>
      from(u)
        .where(u.id === userId)
        .find(models.Parsers.user(u))
        .headOption
    }
  } 
}
