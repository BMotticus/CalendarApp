package modules

import mysql._
import com.gravitydev.scoop._
import query._
import java.time.{Instant, ZoneId}
import play.api.Play.current
import play.api.db.DB
import java.sql.Connection

import models._

class StoreModule (protected val ctx: Context) extends ContextOps{

  def createStore(accountId: Long, timezone: ZoneId):Long = {
    DB.withConnection{ implicit conn =>
      using(tables.stores){s =>
        insertInto(s)
          .values(
            s.name         := "Default",
            s.account_id   := accountId,
            s.created_date := Instant.now,
            s.timezone     := timezone
          )().get
      }
    }
  }
  
  def byUserId(userId: Long) = DB.withConnection{ implicit conn =>
    using(tables.`users`, tables.`stores`){(u,s) =>
      from(s)
        .innerJoin(u on u.store_id === s.id)
        .where(u.id === userId)
        .find(Parsers.store(s))
        .headOption
    }
  }
  
}
