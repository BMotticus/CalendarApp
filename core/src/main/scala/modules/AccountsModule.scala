package modules

import mysql._
import com.gravitydev.scoop._
import query._
import java.time.{Instant, ZoneId}

import play.api.Play.current
import play.api.db.DB
import java.sql.Connection

import models._
import session.SignedInUser

class AccountsModule (protected val ctx: Context) extends ContextOps{
  
  def createAccount(signUpData: SignUpData):SignedInUser = {
    val address = Address("", "", "", "", "")
    val accountId = insertAccount(signUpData.companyName, address)
    val storeId = insertStore(accountId, signUpData.timezone)
    val userId = insertUser(accountId, storeId, signUpData)
//    val (accountId, storeId, userId) = for{
//      accountId <- insertAccount(signUpData.companyName, address)
//      storeId <- insertStore(accountId, signUpData.timezone)
//      userId <- insertUser(accountId, storeId, signUpData)
//    } yield (accountId, storeId, userId)
    SignedInUser(
      UserInfo(userId, signUpData.email),
      StoreInfo(storeId, signUpData.timezone),
      AccountInfo(accountId, signUpData.companyName)
    )
  }
  
  def insertAccount(companyName: String, address: Address) = {
    DB.withConnection{ implicit conn =>
      using(tables.accounts){a =>
        insertInto(a)
          .values(
            a.name    := companyName,
            a.address  := address.address,
            a.city     := address.city,
            a.state     := address.state,
            a.country    := address.country,
            a.postal_code := address.postalCode,
            a.created_date := Instant.now
          )().get
      }
    }
  }

  def insertStore(accountId: Long, timezone: ZoneId):Long = {
    DB.withConnection { implicit conn =>
      using(tables.stores) { s =>
        insertInto(s)
          .values(
            s.name := "Default",
            s.account_id := accountId,
            s.created_date := Instant.now,
            s.timezone := timezone
          )().get
      }
    }
  }
  
  def insertUser(accountId: Long, storeId: Long, signUpData: SignUpData):Long = {
    DB.withConnection { implicit conn =>
      using(tables.users) { u =>
        insertInto(u)
          .values(
            u.account_id := accountId,
            u.email := signUpData.email,
            u.password := signUpData.password,
            u.store_id := storeId,
            u.created_date := Instant.now,
            u.deleted := false,
            u.role := "ADMIN"
          )().get
      }
    }
  }
  
}
