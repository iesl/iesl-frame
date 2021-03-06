package lib

import _root_.java.lang.Exception
import com.typesafe.scalalogging.{StrictLogging => Logging}
import play.api.Play.current
import play.api.mvc._
import securesocial.core._

//import lib.scalateor._
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

/*
Ajax calls must always return "OK"; otherwise the browser shows nothing and the user gets confused.
Any error messages (e.g. "not authorized" etc. must be provided as ajax fragments.

Jump through hoops to enforce this with types.
 */

// class AjaxResult(_header: ResponseHeader, _body: Enumerator[EldarionAjaxResponse])(implicit val _writeable: Writeable[EldarionAjaxResponse]) extends Result(_header,_body)(_writeable) {

class AjaxResult(_header: ResponseHeader, _body: Enumerator[Array[Byte]]) extends Result(_header,_body) {

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
    "Result(" + header + ")"
  }

}


object AjaxOk {
   /*def apply(content: EldarionAjaxResponse)(implicit writeable: Writeable[EldarionAjaxResponse]): AjaxResult = {
    new AjaxResult(
      header = ResponseHeader(OK, writeable.contentType.map(ct => Map(CONTENT_TYPE -> ct)).getOrElse(Map.empty)),
      Enumerator(content))
  }*/

  def apply(fragments: EldarionAjaxResponseFragment*)(implicit writeable: Writeable[EldarionAjaxResponse]): AjaxResult = {
    val content = new EldarionAjaxFragmentsResponse(fragments).renderString.getBytes
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
    val content = new EldarionAjaxRedirect(location).renderString.getBytes
    new AjaxResult(
      ResponseHeader(OK, writeable.contentType.map(ct => Map(CONTENT_TYPE -> ct)).getOrElse(Map.empty)),
      Enumerator(content))
  }
}


object UserControllerOps {
  val ImpersonateUserKey = "impersonate.user.id"
}

////trait UserControllerOps[T] extends ControllerOps with SecureSocial with ScalateControllerSupport with Logging {
trait UserControllerOps[T] extends ControllerOps with securesocial.core.SecureSocial[IFUser[T]] with IFScalaTags with AjaxModule with Logging {
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
 
  //import EldarionAjax._
  def AjaxUserAction[C](f: Request[AnyContent] => Option[IFUser[T]] => AjaxResult): Action[AnyContent] =
    SecuredAction {
      implicit req => {
        try {
          logger.trace("Executing secured AJAX action")
          logger.trace(req.toString())
          maybeImpersonateAction(userFromSSocialUser)(f)(req)
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
    // TODO
    logger.trace(req.toString())
    None
    maybeImpersonateAction(ouserFromSSocialUser)(f)(req)
  }
  
  def userFromSSocialUser(implicit req: SecuredRequest[_]): Option[IFUser[T]] = {
    // TODO
    None
    //val ssuser = req.user
    //for {
    //  la :IFLinkedAccount[T] <- linkedAccountStore.findByProviderId(ssuser.identityId.providerId, ssuser.identityId.userId)
    //  u <- userStore.get(la.userId)
    //} yield u
  }
 
  def ouserFromSSocialUser(implicit req: RequestWithUser[_]): Option[IFUser[T]] = {
    // FIXME
    None
    //for {
    //  ssuser <- req.user
    //  la:IFLinkedAccount[T]  <- linkedAccountStore.findByProviderId(ssuser.identityId.providerId, ssuser.identityId.userId)
    //  u <- userStore.get(la.userId)
    //} yield u
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
 
  //def sendEmail(subject: String, recipient: NonemptyString, body: String, fromAddress: Option[String]) {
  // 
  //  import com.typesafe.plugin._
  // 
  //  logger.info("sending email to %s".format(recipient))
  //  logger.info("mail = [%s]".format(body))
  // 
  //  val mail = use[MailerPlugin].email
  //  mail.setSubject(subject)
  //  mail.addRecipient(recipient.s)
  //  mail.addFrom(fromAddress.getOrElse(defaultFromAddress))
  //  mail.send(body)
  //}
 
}
 
 
object ControllerOps extends ControllerOps


