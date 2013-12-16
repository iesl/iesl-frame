package lib.ajax

import play.api.libs.json.JsValue
import play.api.libs.json.Json._
import lib.ScalateOps


trait EldarionAjaxViewable {
  def renderJs(): JsValue
}
/**
 * A wrapper for a domain object that can be rendered to html with a given view.  The javascript hook can be used for highlighting or whatever else.
 *
 * This can be used as an Ajax fragment or as a complete page.
 */
case class EldarionAjaxScalateViewable(it: AnyRef, view: String, javascript: String, args: (Symbol, Any)*) extends EldarionAjaxViewable  { //extends EldarionAjaxResponseElement {
  def ~(v: String): EldarionAjaxScalateViewable = EldarionAjaxScalateViewable(it, v, javascript, args: _*)

  def highlight: EldarionAjaxScalateViewable = EldarionAjaxScalateViewable(it, view, javascript + "this.highlight();", args: _*)

  def sub(a: (Symbol, Any)*): EldarionAjaxScalateViewable = EldarionAjaxScalateViewable(it, view, javascript, a: _*)

  def exec(js: String): EldarionAjaxScalateViewable = EldarionAjaxScalateViewable(it, view, js, args: _*)

  def renderJs(): JsValue = toJson(Map("html" -> renderString, "js" -> javascript))

  def renderString: String = ScalateOps.viewHtml(it, view)(args: _*)
}

case class EldarionAjaxString(s:String) extends EldarionAjaxViewable {
  def renderJs(): JsValue = toJson(Map("html"->s))
}

case class EldarionAjaxJsOnly(javascript: String) extends EldarionAjaxViewable {
  // include the "html" key just in case, to avoid errors on the client side
  def renderJs(): JsValue = toJson(Map("html" -> "", "js" -> javascript))
}

case class EldarionAjaxErrorViewable(message: String, javascript: String, args: (Symbol, Any)*) extends EldarionAjaxViewable { //extends EldarionAjaxResponseElement {
// hmm, our eldarion-ajax interface shouldn't assume that the site uses bootstrap.  Alternative is to render a scalate error template.  Fine, but too much hassle for now.
// also note we don't know what the calling template will do with returned HTML; it might just be ignored.
// need to add an "error" type, parallel to "html", to the eldarion-ajax stuff.
//def render(): JsValue = toJson(Map("html"->s"<div class='alert alert-error'>$message</div>"))
val it = EldarionAjaxError(message)

  def renderJs(): JsValue = toJson(Map("error" -> ScalateOps.viewHtml(it)(args: _*), "js" -> javascript))
}


