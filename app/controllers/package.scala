import play.api.libs.json.{Json, JsValue}
import play.api.mvc.Results._
import scala.concurrent.Future
import play.twirl.api.{Content, Html}
import play.api.libs.json.{Json, JsValue}
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits._

/**
 * Created by brandon Mott
 */
package object controllers{

  /**
    * outputs a given string and the current thread name
    * @param msg
    */
   def log(msg: String) {
     println(s"${Thread.currentThread.getName}: $msg")
   }

}
