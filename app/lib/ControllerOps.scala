package lib


import _root_.java.lang.Exception
import com.typesafe.scalalogging.slf4j.Logging
import play.api.Play.current
import play.api.mvc._
import securesocial.core._

import lib.scalateor._
import scala.collection.Traversable
import play.api.http.Status._
import play.api.http.Writeable
import play.api.http.HeaderNames._
import play.api.libs.iteratee.Enumerator
import play.api.mvc.Results.Status
import play.api.mvc.Cookie
import play.api.libs.json.JsValue
import lib.ajax._
import edu.umass.cs.iesl.scalacommons.NonemptyString
import play.api.mvc.Cookie

trait IFUser[T] {
  def id: T
}

trait IFUserStore[T] {
  def get(id:T) : Option[IFUser[T]]
  def getByStringId(id:String) : Option[IFUser[T]]
  def findByEmail(s: String): Traversable[IFUser[T]]
  def findBySubstring(s: String): Traversable[IFUser[T]]
}

trait IFLinkedAccount[T] {
  def id: T
  def userId: T
  def providerUserId: String
  def authMethod: String
  def providerKey: String

}

trait IFLinkedAccountStore[T] {
  def findByProviderId(s: String, s1: String) : Option[IFLinkedAccount[T]]

}


/*
Ajax calls must always return "OK"; otherwise the browser shows nothing and the user gets confused.
Any error messages (e.g. "not authorized" etc. must be provided as ajax fragments.

Jump through hoops to enforce this with types.
 */

class AjaxResult(_header: ResponseHeader, _body: Enumerator[EldarionAjaxResponse])(implicit val _writeable: Writeable[EldarionAjaxResponse]) extends SimpleResult[EldarionAjaxResponse](_header,_body)(_writeable) {

  override def withSession(session: Session): AjaxResult = {
    if (session.isEmpty) discardingCookies(Session.discard) else withCookies(Session.encodeAsCookie(session))
  }   
  
  override def withCookies(cookies: Cookie*): AjaxResult = {
    withHeaders(SET_COOKIE -> Cookies.merge(header.headers.get(SET_COOKIE).getOrElse(""), cookies))
  }

  override def discardingCookies(cookies: DiscardingCookie*): AjaxResult = {
    withHeaders(SET_COOKIE -> Cookies.merge(header.headers.get(SET_COOKIE).getOrElse(""), cookies.map(_.toCookie)))
  }

  override def withHeaders(headers: (String, String)*) : AjaxResult = {
    new AjaxResult(header.copy(headers = header.headers ++ headers), body)
  }

  override def toString = {
    "SimpleResult(" + header + ")"
  }

}


// this seems like it should work fine, but somewhere Play swallows an error that I couldn't diagnose.  Backtracking...
/*
case class AjaxResult(header: ResponseHeader, body: Enumerator[EldarionAjaxResponse])(implicit val writeable: Writeable[EldarionAjaxResponse]) extends PlainResult {

  override def withSession(session: Session): AjaxResult = {
    if (session.isEmpty) discardingCookies(Session.discard) else withCookies(Session.encodeAsCookie(session))
  }

  override def withCookies(cookies: Cookie*): AjaxResult = {
    withHeaders(SET_COOKIE -> Cookies.merge(header.headers.get(SET_COOKIE).getOrElse(""), cookies))
  }

  override def discardingCookies(cookies: DiscardingCookie*): AjaxResult = {
    withHeaders(SET_COOKIE -> Cookies.merge(header.headers.get(SET_COOKIE).getOrElse(""), cookies.map(_.toCookie)))
  }
  
  /** The body content type. */
  type BODY_CONTENT = EldarionAjaxResponse

  /**
   * Adds headers to this result.
   *
   * For example:
   * {{{
   * Ok("Hello world").withHeaders(ETAG -> "0")
   * }}}
   *
   * @param headers the headers to add to this result.
   * @return the new result
   */
  def withHeaders(headers: (String, String)*) : AjaxResult = {
    copy(header = header.copy(headers = header.headers ++ headers))
  }

  override def toString = {
    "AjaxResult(" + header + ")"
  }

}
*/

object AjaxOk {
   /*def apply(content: EldarionAjaxResponse)(implicit writeable: Writeable[EldarionAjaxResponse]): AjaxResult = {
    new AjaxResult(
      header = ResponseHeader(OK, writeable.contentType.map(ct => Map(CONTENT_TYPE -> ct)).getOrElse(Map.empty)),
      Enumerator(content))
  }*/

  def apply(fragments: EldarionAjaxResponseFragment*)(implicit writeable: Writeable[EldarionAjaxResponse]): AjaxResult = {
    val content = new EldarionAjaxFragmentsResponse(fragments) //.renderJs()
    new AjaxResult(
      ResponseHeader(OK, writeable.contentType.map(ct => Map(CONTENT_TYPE -> ct)).getOrElse(Map.empty)),
      Enumerator(content))
  }
}


object AjaxRedirect {
  /*def apply(content: EldarionAjaxResponse)(implicit writeable: Writeable[EldarionAjaxResponse]): AjaxResult = {
   new AjaxResult(
     header = ResponseHeader(OK, writeable.contentType.map(ct => Map(CONTENT_TYPE -> ct)).getOrElse(Map.empty)),
     Enumerator(content))
 }*/

  def apply(location:String)(implicit writeable: Writeable[EldarionAjaxResponse]): AjaxResult = {
    val content = new EldarionAjaxRedirect(location) //.renderJs()
    new AjaxResult(
      ResponseHeader(OK, writeable.contentType.map(ct => Map(CONTENT_TYPE -> ct)).getOrElse(Map.empty)),
      Enumerator(content))
  }
}


object UserControllerOps {
  val ImpersonateUserKey = "impersonate.user.id"
}

trait UserControllerOps[T] extends ControllerOps with SecureSocial with ScalateControllerSupport with Logging {
  import UserControllerOps._
  
  def userStore : IFUserStore[T]
  def linkedAccountStore : IFLinkedAccountStore[T]
  def authorizedToImpersonate(user: IFUser[T]): Boolean 

  def impersonate(user: IFUser[T]): (String, String) =
    (ImpersonateUserKey -> user.id.toString)


  def maybeImpersonateAction[A](ouser: Option[IFUser[T]])(f: Request[A] => Option[IFUser[T]] => Result)(implicit req: Request[A]): Result = {
    try {
      (for {
        mainUser <- ouser
        otherUser <- req.session.get(ImpersonateUserKey)
        if authorizedToImpersonate(mainUser)
        impUser <- userStore.getByStringId(otherUser)
      } yield {
        val result = f(req)(Some(impUser))
        result
      }).getOrElse {
        val result =  f(req)(ouser)
        result
      }
    }
    catch {
      case e: Throwable => {
        logger.error("error",e)
        throw e
      }
    }
  }

  def UserAction(f: Request[AnyContent] => Option[IFUser[T]] => Result): Action[AnyContent] =
    SecuredAction {
      implicit req => {
        try {
          logger.trace("Executing secured action")
          logger.trace(req.toString())
          val result = maybeImpersonateAction(userFromSSocialUser)(f)(req)
          result
        }
        catch {
          case e: NotAuthorizedException => Unauthorized
          case e: Throwable => {
            logger.error("error", e)
            throw e
          }
        }
      }
    }


  case class NotAuthorizedException(s: String = "") extends Exception(s)
  case class NotFoundException(s: String = "") extends Exception(s)
  /*
  object AjaxOk {
    def apply[C](content: C)(implicit writeable: Writeable[C]) : AjaxOk = new AjaxOk(content)(writeable)
  }
  */
  def AjaxUserAction[C](f: Request[AnyContent] => Option[IFUser[T]] => AjaxResult): Action[AnyContent] =
    SecuredAction {
      import EldarionAjax._
      implicit req => {
        try {
          logger.trace("Executing secured AJAX action")
          logger.trace(req.toString())
          val result = maybeImpersonateAction(userFromSSocialUser)(f)(req)
          result
        }
        catch {
          case e: NotAuthorizedException => AjaxOk(".ajaxerror" >< EldarionAjaxError("Not authorized.") ~ "error") //Unauthorized
          case e: NotFoundException => AjaxOk(".ajaxerror" >< EldarionAjaxError("Not found.") ~ "error") //Unauthorized
          case e: Throwable => {
            logger.error("error", e)
            AjaxOk(".ajaxerror" >< (EldarionAjaxError() ~ "error"))
            //throw e
          }
        }
      }
    }


  def OptUserAction(f: Request[AnyContent] => Option[IFUser[T]] => Result) = UserAwareAction {
    implicit req =>
      logger.trace(req.toString())
      val result = maybeImpersonateAction(ouserFromSSocialUser)(f)(req)
      result
  }

  def AjaxOptUserAction(f: Request[AnyContent] => Option[IFUser[T]] => AjaxResult) = UserAwareAction {
    implicit req =>
      logger.trace(req.toString())
      val result = maybeImpersonateAction(ouserFromSSocialUser)(f)(req)
      result
  }


  def userFromSSocialUser(implicit req: SecuredRequest[_]): Option[IFUser[T]] = {
    val ssuser = req.user
    for {
      la :IFLinkedAccount[T] <- linkedAccountStore.findByProviderId(ssuser.identityId.providerId, ssuser.identityId.userId)
      u <- userStore.get(la.userId)
    } yield u
  }

  def ouserFromSSocialUser(implicit req: RequestWithUser[_]): Option[IFUser[T]] = {
    for {
      ssuser <- req.user
      la:IFLinkedAccount[T]  <- linkedAccountStore.findByProviderId(ssuser.identityId.providerId, ssuser.identityId.userId)
      u <- userStore.get(la.userId)
    } yield u
  }

}

trait ControllerOps extends Controller with Logging {
  type PlayRequest = play.api.mvc.Request[AnyContent]

 
  // TODO: use the PlayEmailerContext 
  lazy val serverHostname = current.configuration.getString("server.hostname").getOrElse("openreview.net")
  lazy val defaultFromAddress = current.configuration.getString("smtp.from").getOrElse("openreview.net " +
                                                                                       "<info@openreview.net>")

  def resolveURL(urlPath: String, secure: Boolean = false)(implicit request: RequestHeader) = {
    "http" + (if (secure) "s" else "") + "://" + serverHostname + urlPath
  }

  def sendEmail(subject: String, recipient: NonemptyString, body: String, fromAddress: Option[String]) {

    import com.typesafe.plugin._

    logger.info("sending email to %s".format(recipient))
    logger.info("mail = [%s]".format(body))

    val mail = use[MailerPlugin].email
    mail.setSubject(subject)
    mail.addRecipient(recipient.s)
    mail.addFrom(fromAddress.getOrElse(defaultFromAddress))
    mail.send(body)
  }

}


object ControllerOps extends ControllerOps {
}

