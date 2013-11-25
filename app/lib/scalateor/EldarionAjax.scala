package lib.scalateor

import lib.ScalateOps
import play.api.libs.json.Json._
import scala.Some
import scala.collection.GenTraversable
import play.api.libs.json.JsValue
import play.api.mvc.Codec

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 */

object EldarionAjax {
  implicit def toSelector(s: String) = EldarionAjaxSelector(s)

  //implicit def toAjaxResponseHtml(v: EldarionAjaxViewable) = EldarionAjaxResponseHtml(v)

  implicit def view(it: AnyRef) = EldarionAjaxViewable(it, "index", "")
  
  implicit def errorToViewable(x:EldarionAjaxError) = EldarionAjaxErrorViewable(x.message,"")
}

sealed trait EldarionAjaxFragmentPosition

case object Replace extends EldarionAjaxFragmentPosition

case object Inner extends EldarionAjaxFragmentPosition

case object Prepend extends EldarionAjaxFragmentPosition

case object Append extends EldarionAjaxFragmentPosition

/**
 * Anything that can be rendered as html, and possibly highlighted
 */
trait EldarionAjaxResponseElement {
  //extends RenderableTemplateConfig{
  def render(): JsValue
}

// basically a stub so we can use the standard template-finding mechanism
case class EldarionAjaxError(message:String = "An unexpected error occured, and the developers have been notified.  Please reload the page and try again, or contact us for more information.")

case class EldarionAjaxErrorViewable(message:String, javascript:String, args: (Symbol, Any)*)  extends EldarionAjaxResponseElement {
  // hmm, our eldarion-ajax interface shouldn't assume that the site uses bootstrap.  Alternative is to render a scalate error template.  Fine, but too much hassle for now.
  // also note we don't know what the calling template will do with returned HTML; it might just be ignored.
  // need to add an "error" type, parallel to "html", to the eldarion-ajax stuff.
  //def render(): JsValue = toJson(Map("html"->s"<div class='alert alert-error'>$message</div>"))
  val it = EldarionAjaxError(message)
  def render(): JsValue = toJson(Map("error"->ScalateOps.viewHtml(it)(args: _*),"js"->javascript))
}

/**
 * A wrapper for a domain object that can be rendered to html with a given view.  The javascript hook can be used for highlighting or whatever else.
 */
case class EldarionAjaxViewable(it: AnyRef, view: String, javascript:String, args: (Symbol, Any)*)  extends EldarionAjaxResponseElement {
  def ~(v: String): EldarionAjaxViewable = EldarionAjaxViewable(it, v, javascript, args: _*)

  def highlight:EldarionAjaxViewable = EldarionAjaxViewable(it, view, javascript + "this.highlight();", args: _*)

  def sub(a: (Symbol, Any)*):EldarionAjaxViewable  = EldarionAjaxViewable(it, view, javascript, a: _*)
  
  def exec(js:String):EldarionAjaxViewable = EldarionAjaxViewable(it, view, js, args: _*)

  def render(): JsValue = toJson(Map("html"->ScalateOps.viewHtml(it, view)(args: _*),"js"->javascript))
}

case class EldarionAjaxSelector(selector: String) {

  def replaceWith(v: EldarionAjaxViewable) = EldarionAjaxResponseFragment(selector, Replace, v)

  def replaceInside(v: EldarionAjaxViewable) = EldarionAjaxResponseFragment(selector, Inner, v)

  def prepend(v: EldarionAjaxViewable) = EldarionAjaxResponseFragment(selector, Prepend, v)

  def append(v: EldarionAjaxViewable) = EldarionAjaxResponseFragment(selector, Append, v)

  def <>(v: EldarionAjaxViewable) = EldarionAjaxResponseFragment(selector, Replace, v)

  def ><(v: EldarionAjaxViewable) = EldarionAjaxResponseFragment(selector, Inner, v)

  def <<(v: EldarionAjaxViewable) = EldarionAjaxResponseFragment(selector, Prepend, v)

  def >>(v: EldarionAjaxViewable) = EldarionAjaxResponseFragment(selector, Append, v)
}

// this just means that the template specifies the selector and position, and we can only send one at a time.
/*case class EldarionAjaxResponseHtml(v: EldarionAjaxViewable) extends EldarionAjaxResponseElement {
 def render(): String = v.render

  /* def replace(s:String) = AjaxResponseFragment(s,Replace,v)
   def inside(s:String) = AjaxResponseFragment(s,Inner,v)
   def atBeginning(s:String) = AjaxResponseFragment(s,Prepend,v)
   def atEnd(s:String) = AjaxResponseFragment(s,Append,v)*/
}*/

case class EldarionAjaxResponseFragment(selector: String, pos: EldarionAjaxFragmentPosition, v: EldarionAjaxViewable, highlight:Boolean = false) extends EldarionAjaxResponseElement {
  def render(): JsValue = v.render
}


trait EldarionAjaxResponseEncodings {

  import org.fusesource.scalate.Utils
  import play.api.http.{ContentTypeOf, ContentTypes, Writeable}

  /*
  object BsAjaxResponse {
    def withHtml(t: RenderableTemplateConfig) =
      BsAjaxResponse(html=Some(t))
  }
*/

  def ajaxRedirect(location: String) = EldarionAjaxRedirect(location)

  case class EldarionAjaxRedirect(location: String) {
    def render(): String = {
      // toJson(Map("html" -> toJson(html),"fragments" -> toJson(jsonFragments), "inner-fragments" ->toJson(jsonInnerFragments)))
      Utils.prettyPrintJson(toJson(Map(
        "location" -> location
      )))
    }
  }


  // use GenTraversable instead of GenSet because GenSet is not covariant (!?)
  case class EldarionAjaxResponse(elements: GenTraversable[EldarionAjaxResponseElement]) {
    // constraint that there can be only one html is not enforced by type; if multiple are provided, one is picked.
    def noselector: Option[JsValue] = elements.find(_.isInstanceOf[EldarionAjaxViewable]).headOption.map(e=>e.render())
    
    val replaceFragments: Map[String, JsValue] = elements.collect({
      case e: EldarionAjaxResponseFragment if e.pos == Replace => (e.selector, e.render())
    }).seq.toMap
    val innerFragments: Map[String, JsValue] = elements.collect({
      case e: EldarionAjaxResponseFragment if e.pos == Inner => (e.selector, e.render())
    }).seq.toMap
    val appendFragments: Map[String, JsValue] = elements.collect({
      case e: EldarionAjaxResponseFragment if e.pos == Append => (e.selector, e.render())
    }).seq.toMap
    val prependFragments: Map[String, JsValue] = elements.collect({
      case e: EldarionAjaxResponseFragment if e.pos == Prepend => (e.selector, e.render())
    }).seq.toMap

    // def +(e:EldarionAjaxResponseElement) = copy(elements = elements :+ e)

    def render(): String = {
      // toJson(Map("html" -> toJson(html),"fragments" -> toJson(jsonFragments), "inner-fragments" ->toJson(jsonInnerFragments)))
      val result = Utils.prettyPrintJson(toJson(List(
        noselector.map("noselector" ->),
        if (replaceFragments.nonEmpty) Some("fragments" -> toJson(replaceFragments)) else None,
        if (innerFragments.nonEmpty) Some("inner-fragments" -> toJson(innerFragments)) else None,
        if (appendFragments.nonEmpty) Some("append-fragments" -> toJson(appendFragments)) else None,
        if (prependFragments.nonEmpty) Some("prepend-fragments" -> toJson(prependFragments)) else None
      ).filter(_.isDefined).map(_.get).toMap)) //.filter.toMap
      result
    }
  }

  /*
  implicit def writeableOf_AjaxResponse(implicit codec: Codec): Writeable[EldarionAjaxResponse] =
    Writeable[EldarionAjaxResponse]((vconf:EldarionAjaxResponse) => codec.encode(vconf.render()))

  implicit def contentTypeOf_BsAjaxResponse(implicit codec: Codec): ContentTypeOf[EldarionAjaxResponse] =
    ContentTypeOf[EldarionAjaxResponse](Some(ContentTypes.JSON))
  */

  //confused about inheritance with Writeables...
/*
  implicit def writeableOf_AjaxViewable(implicit codec: Codec): Writeable[EldarionAjaxViewable] =
    Writeable[EldarionAjaxViewable]((element: EldarionAjaxViewable) => codec.encode(EldarionAjaxResponse(Seq(element)).render()))

  implicit def contentTypeOf_AjaxViewable(implicit codec: Codec): ContentTypeOf[EldarionAjaxViewable] =
    ContentTypeOf[EldarionAjaxViewable](Some(ContentTypes.JSON))
*/
  implicit def writeableOf_AjaxRedirect(implicit codec: Codec): Writeable[EldarionAjaxRedirect] =
    Writeable[EldarionAjaxRedirect]((element: EldarionAjaxRedirect) => codec.encode(element.render()))

  implicit def contentTypeOf_AjaxRedirect(implicit codec: Codec): ContentTypeOf[EldarionAjaxRedirect] =
    ContentTypeOf[EldarionAjaxRedirect](Some(ContentTypes.JSON))

  implicit def writeableOf_AjaxResponseElement(implicit codec: Codec): Writeable[EldarionAjaxResponseElement] =
    Writeable[EldarionAjaxResponseElement]((element: EldarionAjaxResponseElement) => codec.encode(EldarionAjaxResponse(Seq(element)).render()))

  implicit def contentTypeOf_AjaxResponseElement(implicit codec: Codec): ContentTypeOf[EldarionAjaxResponseElement] =
    ContentTypeOf[EldarionAjaxResponseElement](Some(ContentTypes.JSON))

  implicit def writeableOf_AjaxResponseElementSet(implicit codec: Codec): Writeable[GenTraversable[EldarionAjaxResponseElement]] =
    Writeable[GenTraversable[EldarionAjaxResponseElement]]((elements: GenTraversable[EldarionAjaxResponseElement]) => codec.encode(EldarionAjaxResponse(elements).render()))

  implicit def contentTypeOf_AjaxResponseElementSet(implicit codec: Codec): ContentTypeOf[GenTraversable[EldarionAjaxResponseElement]] =
    ContentTypeOf[GenTraversable[EldarionAjaxResponseElement]](Some(ContentTypes.JSON))


  //implicit def toAjaxResponse(elements : GenSet[EldarionAjaxResponseElement]) : EldarionAjaxResponse = EldarionAjaxResponse(elements)
  //implicit def toAjaxResponse(element : EldarionAjaxResponseElement) : EldarionAjaxResponse = EldarionAjaxResponse(Set(element))
}
