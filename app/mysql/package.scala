import com.gravitydev.scoop._, query._
import org.joda.time._

package object mysql {

  implicit object dateTime  extends SqlCustomType [DateTime, java.sql.Timestamp] (
    d => new DateTime(d.getTime), 
    d => new java.sql.Timestamp(d.getMillis)
  )
  
}
