package lib.ajax

import play.api.libs.json.JsValue
import play.api.libs.json.Json

trait AjaxViewable {
  def renderJs(): JsValue = toJson
  def toJson(): JsValue
}

import scalatags.Node

trait AjaxModule extends EldarionAjaxResponseEncodings {
  import play.api.mvc._
  import lib.AjaxResult

  def renderModelView(model: AnyRef, view: String, args:(String, Any)*): Node

  //def renderJavascript(jsstr: String): String

  case class ViewableModel(it: AnyRef, view: String, javascript: String, args: (String, Any)*) extends AjaxViewable  { 
    def ~(v: String): ViewableModel = ViewableModel(it, v, javascript, args: _*)
  
    def highlight: ViewableModel = ViewableModel(it, view,  s"${javascript}; this.highlight();", args: _*)
  
    def sub(a: (String, Any)*): ViewableModel = ViewableModel(it, view, javascript, a: _*)
  
    def exec(js: String): ViewableModel = ViewableModel(it, view, js, args: _*)
  
    def toJson(): JsValue = Json.toJson(Map("html" -> renderHtml.toString, "js" -> javascript))
  
    //def renderString: String = renderModelView(it, view, args: _*)
    def renderHtml: Node = renderModelView(it, view, args: _*)
  }

  case class HtmlString(s:String) extends AjaxViewable {
    def toJson(): JsValue = Json.toJson(Map("html"->s))
  }

  case class JsOnly(javascript: String) extends AjaxViewable {
    // include the "html" key just in case, to avoid errors on the client side
    def toJson(): JsValue = Json.toJson(Map("html" -> "", "js" -> javascript))
  }

  case class ErrorViewable(message: String, javascript: String, args: (String, Any)*) extends AjaxViewable { 
    // hmm, our eldarion-ajax interface shouldn't assume that the site uses bootstrap.  Alternative is to render a
    //    scalate error template.  Fine, but too much hassle for now.
    // also note we don't know what the calling template will do with returned HTML; it might just be ignored.
    // need to add an "error" type, parallel to "html", to the eldarion-ajax stuff.
    //def render(): JsValue = toJson(Map("html"->s"<div class='alert alert-error'>$message</div>"))
    val it = EldarionAjaxError(message)

    // def toJson(): JsValue = Json.toJson(Map("error" -> ScalateOps.viewHtml(it)(args: _*), "js" -> javascript))
    def toJson(): JsValue = Json.toJson(Map("error" -> renderModelView(it, "indexV", args: _*).toString, "js" -> javascript))
  }

  
  implicit def toSelector(s: String) = EldarionAjaxExplicitSelector(s)

  def htmlString(s:String) = HtmlString(s)
  implicit def view(it: AnyRef) = ViewableModel(it, "indexV", "")
  def exec(js:String) = JsOnly(js)


  import status._

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
    
    implicit def toRichAjaxResult[T <: AjaxResult](result: T) = AjaxSessionMessageResult(result)
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
}

///**
// * A wrapper for a domain object that can be rendered to html with a given view.  The javascript hook can be used for highlighting or whatever else.
// *
// * This can be used as an Ajax fragment or as a complete page.
// */
//case class EldarionAjaxViewableModel(html: Node, javascript: String, args: (String, Any)*) extends EldarionAjaxViewable  { 
//  // def ~(v: String): EldarionAjaxViewableModel = EldarionAjaxViewableModel(it, v, javascript, args: _*)
// 
//  def highlight: EldarionAjaxViewableModel = EldarionAjaxViewableModel(html, javascript + "this.highlight();", args: _*)
// 
//  // TODO should this replace or add to args?
//  def sub(a: (String, Any)*): EldarionAjaxViewableModel = EldarionAjaxViewableModel(html, javascript, (a.toSeq ++ args.toSeq): _*)
// 
//  def exec(js: String): EldarionAjaxViewableModel = EldarionAjaxViewableModel(html, js, args: _*)
// 
//  def renderJs(): JsValue = toJson(Map("html" -> renderString, "js" -> javascript))
// 
//  def renderString: String = html.toString
//}
// 
////case class EldarionAjaxViewableModel(it: AnyRef, view: String, javascript: String, args: (Symbol, Any)*) extends EldarionAjaxViewable  { //extends EldarionAjaxResponseElement {
////  def ~(v: String): EldarionAjaxViewableModel = EldarionAjaxViewableModel(it, v, javascript, args: _*)
//// 
////  def highlight: EldarionAjaxViewableModel = EldarionAjaxViewableModel(it, view, javascript + "this.highlight();", args: _*)
//// 
////  // TODO should this replace or add to args?
////  def sub(a: (Symbol, Any)*): EldarionAjaxViewableModel = EldarionAjaxViewableModel(it, view, javascript, a: _*)
//// 
////  def exec(js: String): EldarionAjaxViewableModel = EldarionAjaxViewableModel(it, view, js, args: _*)
//// 
////  def renderJs(): JsValue = toJson(Map("html" -> renderString, "js" -> javascript))
//// 
////  def renderString: String = ScalateOps.viewHtml(it, view)(args: _*)
////}
// 
//case class EldarionAjaxString(s:String) extends EldarionAjaxViewable {
//  def renderJs(): JsValue = toJson(Map("html"->s))
//}
// 
//case class EldarionAjaxJsOnly(javascript: String) extends EldarionAjaxViewable {
//  // include the "html" key just in case, to avoid errors on the client side
//  def renderJs(): JsValue = toJson(Map("html" -> "", "js" -> javascript))
//}
// 
//case class EldarionAjaxErrorViewable(message: String, javascript: String, args: (Symbol, Any)*) extends EldarionAjaxViewable { //extends EldarionAjaxResponseElement {
// 
//  // hmm, our eldarion-ajax interface shouldn't assume that the site uses bootstrap.  Alternative is to render a
//  //    scalate error template.  Fine, but too much hassle for now.
//  // also note we don't know what the calling template will do with returned HTML; it might just be ignored.
//  // need to add an "error" type, parallel to "html", to the eldarion-ajax stuff.
//  //def render(): JsValue = toJson(Map("html"->s"<div class='alert alert-error'>$message</div>"))
//  val it = EldarionAjaxError(message)
// 
//  def renderJs(): JsValue = toJson(Map("error" -> ScalateOps.viewHtml(it)(args: _*), "js" -> javascript))
//}

