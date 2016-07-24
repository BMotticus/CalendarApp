package controllers

import play.api.mvc.RequestHeader
import play.api.http.HeaderNames
import play.api.libs.json.{Json, JsValue}
import play.api.mvc.Results._
import scala.concurrent.Future
import play.twirl.api.{Content, Html}
import play.api.libs.json.{Json, JsValue}
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits._
import com.typesafe.scalalogging.StrictLogging
import play.api.mvc._
import play.api.http._
import session._
import javax.inject.{Inject, Singleton}

trait BaseController extends RequestOps with ControllerOps with context.BMotticusContext with StrictLogging {self: Controller =>
  def config = current.configuration

  def bodyParamOpt (name: String)(implicit r: Request[AnyContent]) = r.body.asFormUrlEncoded.get.get(name).flatMap(_.headOption)

  def bodyParam (name: String)(implicit r: Request[AnyContent]) = bodyParamOpt(name).get

  def fragment (x: Seq[xml.Node]) = Ok(Html(xml.Xhtml.toXhtml(x)))

  def wrapRequestHandler[T](f: AuthRequest[T] => Result) = {implicit req: Request[T] =>
    signedInUser(req) fold(
      _ => goToSignIn,
      user => f(new AuthRequest(req, user))
    )
  }

  implicit def toSignedInUser (implicit req: AuthRequest[_]) = req.user

  def signedIn (code: Request[_] => SignedInUser => Result) = Action {r =>
    signedInUser(r) fold (
      _ => goToSignIn(r),
      u => code(r)(u)
    )
  }
}

trait ControllerOps extends Results {
  trait AccessError
  object MustAuthenticate extends AccessError

  def goToSignIn (implicit r: Request[_]) = Redirect(routes.Application.signIn(r.path))

  def signedInUser(implicit r: Request[_]) = SessionUser.parse(r.session.data) map {su => Right(su) } getOrElse Left(MustAuthenticate)

}

object AuthAction extends ActionBuilder [AuthRequest] with ControllerOps with context.BMotticusContext{
  def invokeBlock [T](request: Request[T], block: AuthRequest[T] => Future[Result]) = {
    signedInUser(request) fold(
      _ => Future.successful(goToSignIn(request)),
      user => {
        val authReq = new AuthRequest(request, user)
        
        block(authReq)
      }
    )
  }

  def forwardToInvokeBlock [T](request: Request[T])(block: AuthRequest[T] => Future[Result]): Future[Result] = invokeBlock(request, block)
}

trait AuthRequestHeader extends RequestHeader {
  def signedInUser: SignedInUser
  def user = signedInUser.user
  def store = signedInUser.store
  def account = signedInUser.account
  lazy val usersM = context.BMotticusContext.bm.usersM
  lazy val googleAuth = context.BMotticusContext.bm.googleAuth
  lazy val accountsM = context.BMotticusContext.bm.accountsM
}

class AuthRequest [T](
   request: Request[T],
   val signedInUser: SignedInUser
) extends WrappedRequest(request) with AuthRequestHeader {
  
}

trait RequestOps {

  implicit class RequestHeaderOps (r: RequestHeader) {
    def isSecure = 
      r.headers.get(HeaderNames.X_FORWARDED_PROTO).exists(_.toLowerCase == "https")
    //def clientRedirect = routes.OAuth.clientRedirect().absoluteURL()(r)
  }
}

object RequestOps extends RequestOps


//React.js 

trait ReactJsEngine {self: Controller =>
  import ReactJsEngineOps._

  def render (view: String, title: String)(props: (String,Json.JsValueWrapper)*): Html = {
    jsEngine.loadScript("public/javascripts/components/Application.js")
    jsEngine.loadScript("public/javascripts/components/Elements.js")
    jsEngine.loadScript(s"public/javascripts/views/$view.js")

    
    
    val data = Json.obj(props:_*)
    val rendered = jsEngine.eval("React.renderToString(React.createElement(View, " + Json.stringify(data) + "));").asInstanceOf[String]

    views.html.react.page(title, view, rendered, props = data)
  }
}

object ReactJsEngineOps {
  implicit class ScriptEngineOps (engine: javax.script.ScriptEngine) {
    def loadScript (script: String) = {
      val code = scala.io.Source.fromInputStream(play.api.Play.application.resourceAsStream(script).get).mkString
      
      val _ = engine.put("code", code)
      val x = engine.eval(s"load({name: '${script.replace("\\/", "")}', script: code});")
    }
  }
  lazy val  jsEngine = {
    val engine = new javax.script.ScriptEngineManager(null).getEngineByName("nashorn")

    val polyfill = engine.eval("""
      var global = this; var window = global; window.matchMedia = function (qs) {return {matches: qs !== '(max-width: 700px)', addListener: function (mq) {}}}; var console = {};
      console.debug = print;
      console.warn = print;
      console.log = print; 
    """)

    List(
      "public/javascripts/lodash.min.js",
      "public/lib/react/react.js",
      "public/javascripts/components/Application.js",
      "public/javascripts/components/Elements.js"
    ).foreach (engine.loadScript)

    engine
  }
}