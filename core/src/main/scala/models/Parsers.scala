package models

import mysql._

object Parsers {
  def user (u: tables.users) = u.id ~ u.email  >> UserInfo.apply
}
