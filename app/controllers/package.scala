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
import play.api.db._
import play.api.http._
import session._

trait BaseController extends RequestOps with ControllerOps with StrictLogging with plugins.BMotticusContext {self: Controller =>
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

object AuthAction extends ActionBuilder [AuthRequest] with ControllerOps with plugins.BMotticusContext{
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
  lazy val usersM = plugins.BMotticusContext.bm.usersM
  lazy val googleAuth = plugins.BMotticusContext.bm.googleAuth
  
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
    def clientRedirect = routes.OAuth.clientRedirect().absoluteURL()(r)
  }
}

object RequestOps extends RequestOps
