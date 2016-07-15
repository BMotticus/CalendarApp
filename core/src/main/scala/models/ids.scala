package models

import scalaz.Equal

 sealed abstract class TableType (val name: String)
 object TableType {
  case object Account extends TableType("account")
  case object Store extends TableType("store")
  case object User extends TableType("user")
 }

sealed abstract class TableId[T <: TableType](val text: String, tableType: T){
 override def toString = text
}
object TableId {
 implicit def idInstance [T <: TableId[_]] = new Equal[T] {
  def equal (a: T, b: T) = a.text == b.text
 }
}

case class AccountId (underlying: Long) extends TableId(underlying.toString, TableType.Account)
case class StoreId (underlying: Long) extends TableId(underlying.toString, TableType.Store)
case class UserId (underlying: Long) extends TableId(underlying.toString, TableType.User)