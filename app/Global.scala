import play.api._
import play.api.mvc._
import play.api.mvc.Results._
import scala.concurrent.Future

object Global extends GlobalSettings{
  //custom error 500
  override def onError(request: RequestHeader, ex: Throwable) = { 
    Future.successful(InternalServerError(
      views.html.errors.internalError(ex, request)
    ))
  }
  //custom error 404
  override def onHandlerNotFound(request: RequestHeader) = {
    Future.successful(NotFound(
      views.html.errors.notFound(request)
    ))
  }
}

