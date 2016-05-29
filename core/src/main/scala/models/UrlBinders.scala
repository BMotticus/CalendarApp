package models

import java.net.URLEncoder

import play.api.mvc.PathBindable
import play.api.mvc.QueryStringBindable
import java.time._
import java.time.format._

object UrlBinders {
  
  implicit def instantBinder = new QueryStringBindable[Instant] {
    def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, Instant]] = {
      params.get(key).flatMap(_.headOption).map { s:String =>
        try {
          val formatted = DateTimeFormatter.ISO_INSTANT.parse(s)
          Right(Instant.from(formatted))
        } catch {
          case e: NumberFormatException => Left("Failed to parse parameter " + key + " as Instant: " + e.getMessage)
        }
      }
    }

    def unbind(key: String, value: Instant): String = {
      key + "=" + URLEncoder.encode(DateTimeFormatter.ISO_INSTANT.format(value), "utf-8")
    }
  }
  
  implicit def zonedDateTimeBinder = new QueryStringBindable[ZonedDateTime] {
    def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, ZonedDateTime]] = {
      params.get(key).flatMap(_.headOption).map { s:String =>
        try {
          val formatted = DateTimeFormatter.ISO_ZONED_DATE_TIME.parse(s)
          //DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
          //return formatter.format(ZonedDateTime.ofInstant(instant, ZoneId.systemDefault()));
          Right(ZonedDateTime.from(formatted))
        } catch {
          case e: NumberFormatException => Left("Failed to parse parameter " + key + " as Instant: " + e.getMessage)
        }
      }
    }

    def unbind(key: String, value: ZonedDateTime): String = {
      key + "=" + URLEncoder.encode(DateTimeFormatter.ISO_ZONED_DATE_TIME.format(value), "utf-8")
    }
  }
  
  implicit def localDateBinder = new QueryStringBindable[LocalDate] {
    def bind(key: String, params: Map[String, Seq[String]]) = {
      params.get(key).flatMap(_.headOption).map { s: String =>
        try {
          Right(LocalDate.parse(s, DateTimeFormatter.BASIC_ISO_DATE))
        } catch {
          case e: NumberFormatException => Left("Failed to parse parameter " + key + " as LocalDate: " + e.getMessage)
        }
      }
    }
    
    def unbind(key: String, value: LocalDate): String = {
      key + "=" + URLEncoder.encode(DateTimeFormatter.BASIC_ISO_DATE.format(value), "utf-8")
    }
  }

  implicit def stringSeqBinder = new QueryStringBindable[Seq[String]] {
    def bind(key: String, params: Map[String, Seq[String]]) = {
      params.get(key).map(Right.apply)
    }
    def unbind(key: String, value: Seq[String]) = {
      value.map(x => key + "=" + URLEncoder.encode(x, "utf-8")).mkString("&")
    }
  }
  
}

object WebBinders {
  
  class MappedUrlPath[A, B: PathBindable](parse: A => B, serialize: B => Either[String, A]) 
  extends PathBindable[A] { val bindable = implicitly[PathBindable[B]]
    override def bind(key: String, value: String): Either[String, A] = 
    for {
      v <- bindable.bind(key, value).right
      mId <- serialize(v).right
    } yield mId

    override def unbind(key: String, value: A): String = bindable.unbind(key, parse(value))
  }

  class MappedQueryString[A, B: QueryStringBindable](to: A => B, serialize: B => Either[String, A]) 
    extends QueryStringBindable[A] { val bindable = implicitly[QueryStringBindable[B]]
    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, A]] =
      bindable.bind(key, params) map { res =>
        for {
          v <- res.right
          mId <- serialize(v).right
        } yield mId
      }
    override def unbind(key: String, value: A): String = bindable.unbind(key, to(value))
  }

  implicit val accountIdUrlPath = new MappedUrlPath[AccountId,Long](parse = _.underlying, serialize = num => Right(AccountId(num)))
  implicit val accountIdQueryString = new MappedUrlPath[AccountId,Long](parse = _.underlying, serialize = num => Right(AccountId(num)))

  implicit val storeIdUrlPath = new MappedUrlPath[StoreId,Long](parse = _.underlying, serialize = num => Right(StoreId(num)))
  implicit val storeIdQueryString = new MappedUrlPath[StoreId,Long](parse = _.underlying, serialize = num => Right(StoreId(num)))

  implicit val userIdUrlPath = new MappedUrlPath[UserId,Long](parse = _.underlying, serialize = num => Right(UserId(num)))
  implicit val userIdQueryString = new MappedUrlPath[UserId,Long](parse = _.underlying, serialize = num => Right(UserId(num)))
  
}


