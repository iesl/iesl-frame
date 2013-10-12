package lib

import play.api.Application
import play.api.mvc._
import com.typesafe.scalalogging.slf4j.Logging

import securesocial.core.{LogoutEvent, LoginEvent, Event, EventListener}


class LoginEventListener(app: Application) extends EventListener with Logging {
  override def ssId: String = "loginEventListener"

  def onEvent(event: Event, request: RequestHeader, session: Session): Option[Session] = {
    event match {
      case e: LoginEvent => 
        None // No change in session
      case e: LogoutEvent => 
        // logger.info("traced logout event for user %s".format(e.user.fullName))
        Some(session - UserControllerOps.ImpersonateUserKey)
    }
  }
}
