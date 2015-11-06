package app.mysql

import com.gravitydev.scoop._, query._
import org.joda.time._

object `package` {

  implicit object dateTime  extends SqlCustomType [DateTime, java.sql.Timestamp] (
    d => new DateTime(d.getTime), 
    d => new java.sql.Timestamp(d.getMillis)
  )
  
}
