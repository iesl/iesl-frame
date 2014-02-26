package status

import play.api.mvc._
import lib.ajax.{EldarionAjaxResponseFragment, EldarionAjax}
import EldarionAjax._
import lib.AjaxResult

// trait Flashing {
//   def message: String
// }

// The Play flashing scope doesn't work for us because it can get lost through multiple redirects (especially e.g. for authentication).
// So, we just use the session scope instead.
// To avoid confusion, we avoid the terms "flash" and "flashing".  These are instead called SessionMessages.

/*
case class FlashingRequest(request: RequestHeader, altMessage: Option[String]) {
  def flashingMessage = {
    val msgKeys = "success info error warning".split(" ").toList
    val msgs = (msgKeys foldLeft List[String]()) { 
      case (acc, e) =>
        val m = request.session.get(e)
        if (m.isDefined)  {
          //List("TODO: FlashingRequest")
          viewHtml(FlashingMessage(m.get), e)() :: acc
        }
        else {
          acc
        }
    }
    (msgs.headOption.getOrElse {
      altMessage.map {m =>
        List("TODO: FlashingRequest")
        // viewHtml(FlashingMessage(m), "info")()
      }
    })
  }

}*/


sealed trait SessionMessageType {
  self =>
  // no need to separate these; they're logically conflated, but so what
  //def sessionKey: String
  //def templateMode: String
  def key: String

  def fragment(msg: String): EldarionAjaxResponseFragment = ".flash-message" >< new SessionMessage(self, msg)
}

case object Success extends SessionMessageType {
  self =>
  val key = "success"
}

case object Info extends SessionMessageType {
  self =>
  val key = "info"
}

case object Warning extends SessionMessageType {
  self =>
  val key = "warning"
}

case object Error extends SessionMessageType {
  self =>
  val key = "error"
}

object SessionMessageType {
  def apply(s: String): SessionMessageType = s match {
    case Success.key => Success
    case Info.key => Info
    case Warning.key => Warning
    case Error.key => Error
  }
}


case class SessionMessageResult(result: SimpleResult) {
  def appendSession(key: String, value: String)(implicit request: Request[_]):SimpleResult = result.withSession(request.session + (key -> value))

  // we store a list of flashing messages in the Session, naming them flash-0, flash-1, etc.
  // the value of each takes the form "type:message"

  def clearSessionMessages(implicit request: Request[_]): SimpleResult = {
    val current = request.session.data.filterKeys(_.startsWith("sessionmessage-"))
    result.withSession(current.keys.foldLeft(request.session)((s, k) => s - k))
  }

  private def withStickySessionMessage(f: SessionMessageType, value: String)(implicit request: Request[_]): SimpleResult = {
    val current = request.session.data.filterKeys(_.startsWith("sessionmessage-"))
    val currentKeyNumbers: Iterable[Int] = current.keys.map(_.substring(15).toInt)
    val maxKey = if(currentKeyNumbers.nonEmpty) currentKeyNumbers.max else 0

    appendSession("sessionmessage-" + (maxKey + 1), f.key + ":" + value)
  }

  def success(value: String)(implicit request: Request[_]): SimpleResult = withStickySessionMessage(Success, value)

  def info(value: String)(implicit request: Request[_]): SimpleResult = withStickySessionMessage(Info, value)

  def warning(value: String)(implicit request: Request[_]): SimpleResult = withStickySessionMessage(Warning, value)

  def error(value: String)(implicit request: Request[_]): SimpleResult = withStickySessionMessage(Error, value)
}


case class AjaxSessionMessageResult(result: AjaxResult) {
  
  def appendSession(key: String, value: String)(implicit request: Request[_]): AjaxResult =  result.withSession(request.session + (key -> value))

  // we store a list of flashing messages in the Session, naming them flash-0, flash-1, etc.
  // the value of each takes the form "type:message"

  def clearSessionMessages(implicit request: Request[_]): SimpleResult  = {
    val current = request.session.data.filterKeys(_.startsWith("sessionmessage-"))
    result.withSession(current.keys.foldLeft(request.session)((s, k) => s - k))
  }

  private def withStickySessionMessage(f: SessionMessageType, value: String)(implicit request: Request[_]): AjaxResult = {
    val current = request.session.data.filterKeys(_.startsWith("sessionmessage-"))
    val currentKeyNumbers: Iterable[Int] = current.keys.map(_.substring(15).toInt)
    val maxKey = if(currentKeyNumbers.nonEmpty) currentKeyNumbers.max else 0

    appendSession("sessionmessage-" + (maxKey + 1), f.key + ":" + value)
  }

  def success(value: String)(implicit request: Request[_]): AjaxResult = withStickySessionMessage(Success, value)

  def info(value: String)(implicit request: Request[_]):AjaxResult= withStickySessionMessage(Info, value)

  def warning(value: String)(implicit request: Request[_]): AjaxResult = withStickySessionMessage(Warning, value)
  
  def error(value: String)(implicit request: Request[_]): AjaxResult  = withStickySessionMessage(Error, value)
}


object SessionMessage {
  import scala.language.implicitConversions
  
  implicit def toRichAjaxResult[T <: AjaxResult
  ](result: T) = AjaxSessionMessageResult(result)
  implicit def toRichResult[T <: SimpleResult](result: T) = SessionMessageResult(result)

  private final val p = "(.*?):(.*)".r

  def get(implicit request: RequestHeader): Seq[SessionMessage] = {
    val current = request.session.data.filterKeys(_.startsWith("flash-"))
    current.values.map(s => {
      val p(key, value) = s
      new SessionMessage(SessionMessageType(key), value)
    }).toSeq
  }
  
  def success(value: String) = Success.fragment(value)
  def info(value: String) = Info.fragment(value)
  def warning(value: String) = Warning.fragment(value)
  def error(value: String) = Error.fragment(value)
}

// These are directly useful for Ajax calls, where the message can be pushed directly into a div.
// They are also used to display sticky flashing messages, at whatever time those are actually resolved from the session.
class SessionMessage(
                      val level: SessionMessageType,
                      val message: String
                      )

/*
object FlashingMessage {
  def flashSuccess(m:String) = ".flash-message" >< FlashingMessage(m, "success") ~ "success"
  def flashInfo(m:String) = ".flash-message" >< FlashingMessage(m, "info") ~ "info"
  def flashError(m:String) = ".flash-message" >< FlashingMessage(m, "error") ~ "error"
  def flashWarning(m:String) = ".flash-message" >< FlashingMessage(m, "warning") ~ "warning"

  //def flashSuccess(m:String) = ".flash-message" -> viewHtml(FlashingMessage(m, "success"), "success")()
  //def flashError(m:String) = ".flash-message" -> viewHtml(FlashingMessage(m, "error"), "error")()
  ////def flashInfo(m:String) = ".flash-message" -> viewHtml(FlashingMessage(m, "info"), "info")()
}
*/
