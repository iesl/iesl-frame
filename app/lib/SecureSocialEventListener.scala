package lib

import play.api.Application
import play.api.mvc._
import com.typesafe.scalalogging.slf4j.Logging

import securesocial.core.{LogoutEvent, LoginEvent, Event, EventListener, SignUpEvent, PasswordResetEvent, PasswordChangeEvent}


class LoginEventListener(app: Application) extends EventListener with Logging {
    override def id: String = "loginEventListener"

    def onEvent(event: Event, request: RequestHeader, session: Session): Option[Session] = {
      event match {
        case e: LogoutEvent =>
          Some(session - UserControllerOps.ImpersonateUserKey)
        case _ => None
          // case e: LoginEvent => "login"
          // case e: SignUpEvent => "signup"
          // case e: PasswordResetEvent => "password reset"
          // case e: PasswordChangeEvent => "password change"
      }
      // Logger.info("traced %s event for user %s".format(eventName, event.user.fullName))
    }

  //def onEvent(event: Event, request: RequestHeader, session: Session): Option[Session] = {
  //  event match {
  //    case e: LoginEvent => 
  //      None // No change in session
  //    case e: LogoutEvent => 
  //      // logger.info("traced logout event for user %s".format(e.user.fullName))
  //      Some(session - UserControllerOps.ImpersonateUserKey)
  //  }
  //}
}

