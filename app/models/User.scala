package models

import com.gravitydev.scoop._, query._
import mysql._
import play.api.Play.current
import play.api.db.DB

case class User (
  id: Long,
  username: String,
  email: String
)





