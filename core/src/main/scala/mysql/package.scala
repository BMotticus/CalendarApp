package mysql

import com.gravitydev.scoop._, query._

import java.time._
import scala.language.postfixOps
import models._

object `package` {

  implicit object instantF extends SqlCustomType [Instant, java.sql.Timestamp] (
    d => d.toInstant, 
    d => new java.sql.Timestamp(d.toEpochMilli)
  )

  implicit object localDateTime extends SqlCustomType [LocalDateTime, java.sql.Timestamp] (
    d => d.toLocalDateTime,
    d => java.sql.Timestamp.valueOf(d)
  )

  implicit object localDateF extends SqlCustomType [LocalDate, java.sql.Date] (
    d => d.toLocalDate, 
    d => java.sql.Date.valueOf(d)
  )
  
  implicit  object zoneIdF extends  SqlCustomType [ZoneId, String] (
    d => ZoneId.of(d),
    d => d.toString
  )
  
  implicit object accountIdF extends SqlCustomType [AccountId, Long](
    new AccountId(_),
    _.underlying
  )

  implicit object storeIdF extends SqlCustomType [StoreId, Long](
    new StoreId(_),
    _.underlying
  )

  implicit object userIdF extends SqlCustomType [UserId, Long](
    new UserId(_),
    _.underlying
  )
}
