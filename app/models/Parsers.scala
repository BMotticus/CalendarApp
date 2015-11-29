package models

import com.gravitydev.scoop._, query._
import mysql._
import session._

object Parsers {
  def user (u: tables.users) = u.id ~ u.user_name ~ u.email  >> User.apply
}
