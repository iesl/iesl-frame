package lib

import _root_.java.util.UUID
import play.api.Application
import com.typesafe.scalalogging.slf4j.Logging

import play.i18n.Messages
import securesocial.core.providers.UsernamePasswordProvider
import securesocial.controllers.Registration
import securesocial.core.providers.utils.RoutesHelper
import play.api.templates.{Html, Txt}


import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import securesocial.controllers.Registration.RegistrationInfo
import securesocial.controllers.PasswordChange.ChangeInfo
import securesocial.controllers.TemplatesPlugin
import securesocial.core._

import ControllerOps._

import lib.scalateor.ScalateControllerSupport
import status.{SessionMessage, Warning}

class SecureSocialViews(application: Application) extends TemplatesPlugin with ScalateControllerSupport {
 def renderHtml(viewname:String, args:(Symbol, Any)*)(implicit request: RequestHeader, user:Option[IFUser[UUID]]=None): Html = {
   Html(
     (s"${viewname}.jade").template.withSymAttribs(
       args:_*
     ).render
   )
 }

  def forms: List[(Symbol, Any)] = List(
    'loginForm -> UsernamePasswordProvider.loginForm,
    'signupForm -> Registration.startSignUp,
    'resetPasswordForm -> Registration.startResetPassword
  )

//  override def onStart() {
//    Logger.info("[securesocial] loaded templates plugin: %s".format(getClass.getName))
//  }

  //  def getLoginPage[A](implicit request: Request[A], form: Form[(String, String)], msg: Option[String] = None): Html
  override def getLoginPage[A](implicit request: Request[A], form: Form[(String, String)], msg: Option[String] = None): Html = {
    renderHtml("auth/login",
      'request -> request,
      'authProviders -> Registry.providers.all().values, 
      //'flashMessage -> (msg.map {Messages.get(_)}),  FLASH
      'sessionMessages -> msg.map((m:String)=>Seq(new SessionMessage(Warning, m))).getOrElse(Seq.empty),
      'loginForm -> form,
      'signupForm -> Registration.startForm,
      'forgotPasswordForm -> Registration.startForm
    )
  }

  //  def getSignUpPage[A](implicit request: Request[A], form: Form[RegistrationInfo], token: String): Html
  override def getSignUpPage[A](implicit request: Request[A], form: Form[RegistrationInfo], token: String): Html = {
    renderHtml("auth/signup",
      'request -> request,
      'token -> token,
      'signupForm -> form,
      'errors -> form.errors.map(e => e.key + ":" + e.message)
    )
  }

  //  def getStartSignUpPage[A](implicit request: Request[A], form: Form[String]): Html
  override def getStartSignUpPage[A](implicit request: Request[A], form: Form[String]): Html = {
    renderHtml("auth/login",
      'request -> request,
      'authProviders -> Registry.providers.all().values, 
      'loginForm -> UsernamePasswordProvider.loginForm,
      'forgotPasswordForm -> Registration.startForm,
      'signupForm -> form
    )
  }

  //  def getStartResetPasswordPage[A](implicit request: Request[A], form: Form[String]): Html
  override def getStartResetPasswordPage[A](implicit request: Request[A], form: Form[String]): Html = {
    renderHtml("auth/login",
      'request -> request,
      'authProviders -> Registry.providers.all().values, 
      'loginForm -> UsernamePasswordProvider.loginForm,
      'signupForm -> Registration.startForm,
      'forgotPasswordForm -> form
    )
  }

  /**
   * Returns the html for the start reset page
   *
   * @param request
   * @tparam A
   * @return
   */
  //  def getResetPasswordPage[A](implicit request: Request[A], form: Form[(String, String)], token: String): Html
  def getResetPasswordPage[A](implicit request: Request[A], form: Form[(String, String)], token: String): Html = {
    renderHtml("auth/resetPassword",
      'request -> request,
      'resetForm -> form,
      'token -> token
    )
  }

   /**
   * Returns the html for the change password page
   *
   * @param request
   * @param form
   * @tparam A
   * @return
   */
//  def getPasswordChangePage[A](implicit request: SecuredRequest[A], form: Form[ChangeInfo]): Html
  def getPasswordChangePage[A](implicit request: SecuredRequest[A], form: Form[ChangeInfo]): Html = {
    renderHtml("auth/passwordChanged",
      'request -> request,
      'passwordChangeForm -> form
    )
  }


  /**
   * Returns the email sent when a user starts the sign up process
   *
   * @param token the token used to identify the request
   * @param request the current http request
   * @return a String with the html code for the email
   */
  def getSignUpEmail(token: String)(implicit request: RequestHeader): (Option[Txt], Option[Html]) = {
    val html = renderHtml("email/signupStart",
      'request -> request,
      'token -> token,
      'href -> resolveURL(RoutesHelper.signUp(token).url, IdentityProvider.sslEnabled)
    )
    None -> Some(html)
  }

  /**
   * Returns the email sent when the user is already registered
   *
   * @param user the user
   * @param request the current request
   * @return a String with the html code for the email
   */
  def getAlreadyRegisteredEmail(user: Identity)(implicit request: RequestHeader): (Option[Txt], Option[Html]) = {
    val html = renderHtml("email/alreadyRegistered",
      'request -> request,
      'user -> user,
      'href -> resolveURL(RoutesHelper.startResetPassword().url, IdentityProvider.sslEnabled)
    )
    None -> Some(html)
  }

  /**
   * Returns the welcome email sent when the user finished the sign up process
   *
   * @param user the user
   * @param request the current request
   * @return a String with the html code for the email
   */
  def getWelcomeEmail(user: Identity)(implicit request: RequestHeader): (Option[Txt], Option[Html]) = {
    val html = renderHtml("email/welcome",
      'request -> request, 
      'user -> user,
      'href -> resolveURL(RoutesHelper.login.url, IdentityProvider.sslEnabled)
    )
    None -> Some(html)
  }

  /**
   * Returns the email sent when a user tries to reset the password but there is no account for
   * that email address in the system
   *
   * @param request the current request
   * @return a String with the html code for the email
   */
  def getUnknownEmailNotice()(implicit request: RequestHeader): (Option[Txt], Option[Html]) = {
    val html = renderHtml("email/unknownEmail",
      'request -> request
    )
    None -> Some(html)
  }

  /**
   * Returns the email sent to the user to reset the password
   *
   * @param user the user
   * @param token the token used to identify the request
   * @param request the current http request
   * @return a String with the html code for the email
   */
  def getSendPasswordResetEmail(user: Identity, token: String)(implicit request: RequestHeader): (Option[Txt], Option[Html]) = {
    val html =  renderHtml("email/passwordReset",
      'request -> request, 
      'user -> user, 
      'token -> token, 
      'href -> resolveURL(RoutesHelper.resetPassword(token).url, IdentityProvider.sslEnabled)
    )
    None -> Some(html)
  }

  /**
   * Returns the email sent as a confirmation of a password change
   *
   * @param user the user
   * @param request the current http request
   * @return a String with the html code for the email
   */
  def getPasswordChangedNoticeEmail(user: Identity)(implicit request: RequestHeader): (Option[Txt], Option[Html]) = {
    val html = renderHtml("email/passwordChanged",
      'request -> request,
      'user -> user,
      'href -> resolveURL(RoutesHelper.login.url, IdentityProvider.sslEnabled)
    )
    None -> Some(html)
  }

   /**
    * Returns the html for the not authorized page
    *
    * @param request
    * @tparam A
    * @return
    */

  //  def getNotAuthorizedPage[A](implicit request: Request[A]): Html
  def getNotAuthorizedPage[A](implicit request: Request[A]): Html = {
    securesocial.views.html.notAuthorized()
  }
 
}

