package models

import com.gravitydev.scoop._
import mysql._
import scala.language.reflectiveCalls
import java.time.ZoneId

object Parsers {

  def account(a: tables.accounts) = a.id ~ a.name ~ a.phone ~ accountAddress(a) ~ a.created_date ~ a.settings ~ a.location >> Account.apply

  def accountInfo(a: tables.accounts) = a.id ~ a.name >> AccountInfo.apply

  def store(s: tables.stores) = s.id ~ s.name ~ s.account_id ~ storeAddress(s) ~ s.created_date ~ s.timezone >> Store.apply

  def storeInfo(s: tables.stores) = s.id ~ s.timezone >> StoreInfo.apply
  
  def user (u: tables.users) = u.id ~ u.account_id ~ u.email ~ u.first_name ~ u.last_name ~ u.role ~ u.store_id ~ u.created_date ~ u.settings >> User.apply
  
  def userInfo (u: tables.users) = u.id ~ u.email  >> UserInfo.apply
  
  def accountAddress(s: tables.accounts) = s.address ~ s.city ~ s.state ~ s.postal_code ~ s.country >> Address.apply
  def storeAddress(s: tables.stores) = s.address ~ s.city ~ s.state ~ s.postal_code ~ s.country >> Address.apply
}
