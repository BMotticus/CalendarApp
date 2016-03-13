package mysql

import com.gravitydev.scoop._, query._
import java.time._

object `package` {

  implicit object dateTime  extends SqlCustomType [Instant, java.sql.Timestamp] (
    d => d.toInstant, 
    d => new java.sql.Timestamp(d.toEpochMilli)
  )
  implicit object localDate extends SqlCustomType [LocalDate, java.sql.Date] (
    d => d.toLocalDate, 
    d => java.sql.Date.valueOf(d)
  )
  
}
