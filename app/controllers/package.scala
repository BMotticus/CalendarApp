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
   * AJAX object for asynchronously storing play form/fields 
   */
  object Ajax {
    
    def fieldErrors (errors: (String,String)*) = BadRequest(
      Json.obj("fieldErrors" -> Json.obj(
        errors.map {case (k,v) => k -> (v: Json.JsValueWrapper)} : _*
      ))
    )
    def created (url: String) = Created(Json.obj("url" -> url)).withHeaders("Location" -> url)
    def success (res: JsValue = Json.obj("success" -> true)) = Ok(res)
  }

  /**
    * outputs a given string and the current thread name
    * @param msg
    */
   def log(msg: String) {
     println(s"${Thread.currentThread.getName}: $msg")
   }

}
