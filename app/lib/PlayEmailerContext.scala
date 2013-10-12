package lib

import play.Logger
import play.api.Play.current
import play.api.mvc.RequestHeader
import edu.umass.cs.iesl.scalacommons.email.EmailerContext

trait PlayEmailerContext extends EmailerContext {
  import play.api.libs.concurrent.Akka
    
  import com.typesafe.plugin._

  lazy val serverHostname = current.configuration.getString("server.hostname").getOrElse("openreview.net")
  lazy val defaultFromAddress = current.configuration.getString("smtp.from").getOrElse("openreview.net <info@openreview.net>")

  // def resolveURL(urlPath: String, secure: Boolean = false)(implicit request: RequestHeader) = {
  def resolvePath(urlPath: String) = resolveURL(urlPath)

  def resolveURL(urlPath: String, secure: Boolean = false) = {
    "http" + (if (secure) "s" else "") + "://" + serverHostname + urlPath
  }

  def sendEmail(subject: String, recipient: String, body: String, from:Option[String]=None, asHtml: Boolean=false) {
    import play.api.libs.concurrent.Akka
    
    import com.typesafe.plugin._

    Logger.debug("sending email to %s".format(recipient))
    Logger.debug("mail = [%s]".format(body))

    val mail = use[MailerPlugin].email
    mail.setSubject(subject)
    mail.addRecipient(recipient)
    mail.addFrom(from.getOrElse(defaultFromAddress))
    if (asHtml)
      mail.sendHtml(body)
    else
      mail.send(body)
  }

}
