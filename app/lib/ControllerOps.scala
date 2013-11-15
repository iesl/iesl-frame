package lib


import com.typesafe.scalalogging.slf4j.Logging
import play.api.Play.current
import play.api.mvc._
import securesocial.core._

import lib.scalateor.ScalateControllerSupport
import scala.collection.GenTraversable

trait IFUser[T] {
  def id: T
}

trait IFUserStore[T] {
  def get(id:T) : Option[IFUser[T]]
  def getByStringId(id:String) : Option[IFUser[T]]
  def findByEmail(s: String): GenTraversable[IFUser[T]]
  def findBySubstring(s: String): GenTraversable[IFUser[T]]
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
    (for {
      mainUser <- ouser
      otherUser <- req.session.get(ImpersonateUserKey)
      if authorizedToImpersonate(mainUser)
      impUser <- userStore.getByStringId(otherUser)
    } yield {
      f(req)(Some(impUser))
    }).getOrElse {
      f(req)(ouser)
    }
  }

  /*
    def maybeImpersonateUploadAction[A](ouser: Option[User])(f: Request[A] => Option[User] => Result)(implicit req:
    SecuredRequest[A]): Result = {
      (for {
        mainUser <- ouser
        otherUser <- req.session.get(ImpersonateUserKey)
        if authorizedToImpersonate(mainUser)
        impUser <- userStore.get(UUID.fromString(otherUser))
      } yield {
        f(req)(Some(impUser))
      }).getOrElse {
        f(req)(ouser)
      }
    }
  */

  /*
  def UploadUserAction(f: Request[MultipartFormData[TemporaryFile]] => Option[User] => Result): Action[MultipartFormData[TemporaryFile]] =
    SecuredAction(true, None, parse.multipartFormData, None) {
      implicit req: SecuredRequest[MultipartFormData[TemporaryFile]] => {
        try {
          logger.trace("Executing secured Ajax action")
          logger.trace(req.toString())
          val result = maybeImpersonateAction[MultipartFormData[TemporaryFile]](userFromSSocialUser)(f)(req)
          result
        }
        catch {
          case e: Throwable => {
            e.printStackTrace()
            throw e
          }
        }
      }
    }
    */

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
          case e: Throwable => {
            e.printStackTrace()
            throw e
          }
        }
      }
    }

  def UserAction[A](
                     bparser: BodyParser[A]
                     )(f: Request[A] => Option[IFUser[T]] => Result) = SecuredAction(
    ajaxCall = false,
    authorize = None,
    p = bparser
  ) {
    implicit req => {
      logger.trace(req.toString())
      val result = maybeImpersonateAction(userFromSSocialUser)(f)(req)
      result
    }
  }

  def OptUserAction(f: Request[AnyContent] => Option[IFUser[T]] => Result) = UserAwareAction {
    implicit req =>
      logger.trace(req.toString())
      val result = maybeImpersonateAction(ouserFromSSocialUser)(f)(req)
      result
  }


  def userFromSSocialUser(implicit req: SecuredRequest[_]): Option[IFUser[T]] = {
    val ssuser = req.user
    for {
      la :IFLinkedAccount[T] <- linkedAccountStore.findByProviderId(ssuser.userIdFromProvider.providerId, ssuser.userIdFromProvider.authId)
      u <- userStore.get(la.userId)
    } yield u
  }

  def ouserFromSSocialUser(implicit req: RequestWithUser[_]): Option[IFUser[T]] = {
    for {
      ssuser <- req.user
      la:IFLinkedAccount[T]  <- linkedAccountStore.findByProviderId(ssuser.userIdFromProvider.providerId, ssuser.userIdFromProvider.authId)
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

  def sendEmail(subject: String, recipient: String, body: String, fromAddress: Option[String]) {

    import com.typesafe.plugin._

    logger.debug("sending email to %s".format(recipient))
    logger.debug("mail = [%s]".format(body))

    val mail = use[MailerPlugin].email
    mail.setSubject(subject)
    mail.addRecipient(recipient)
    mail.addFrom(fromAddress.getOrElse(defaultFromAddress))
    mail.send(body)
  }

}


object ControllerOps extends ControllerOps {
}

