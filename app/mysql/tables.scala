package mysql

import com.gravitydev.scoop._, query._
import org.joda.time._

object `package` {

  implicit object dateTime  extends SqlCustomType [DateTime, java.sql.Timestamp] (
    d => new DateTime(d.getTime), 
    d => new java.sql.Timestamp(d.getMillis)
  )
  implicit object localDate extends SqlCustomType [LocalDate, java.sql.Date] (
    d => LocalDate.fromDateFields(d), 
    d => new java.sql.Date(d.toDate.getTime)
  )
}
