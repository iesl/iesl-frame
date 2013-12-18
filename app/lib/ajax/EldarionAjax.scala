package lib.ajax

import lib.ScalateOps
import play.api.libs.json.Json._
import scala.Some
import scala.collection.GenTraversable
import play.api.libs.json.JsValue
import play.api.mvc.Codec
import org.fusesource.scalate.Utils

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 */

object EldarionAjax {
  import scala.language.implicitConversions
  
  implicit def toSelector(s: String) = EldarionAjaxExplicitSelector(s)
  
  //implicit def toAjaxResponseHtml(v: EldarionAjaxViewable) = EldarionAjaxResponseHtml(v)

  def html(s:String) = EldarionAjaxString(s)
  implicit def view(it: AnyRef) = EldarionAjaxScalateViewable(it, "index", "")
  def exec(js:String) = EldarionAjaxJsOnly(js)

  //implicit def errorToViewable(x: EldarionAjaxError) = EldarionAjaxErrorViewable(x.message, "")

  //implicit def fragmentsToSet(elements: GenTraversable[EldarionAjaxResponseElement]) = new EldarionAjaxFragmentsResponse(elements)
}









// this just means that the template specifies the selector and position, and we can only send one at a time.
/*case class EldarionAjaxResponseHtml(v: EldarionAjaxViewable) extends EldarionAjaxResponseElement {
 def render(): String = v.render

  /* def replace(s:String) = AjaxResponseFragment(s,Replace,v)
   def inside(s:String) = AjaxResponseFragment(s,Inner,v)
   def atBeginning(s:String) = AjaxResponseFragment(s,Prepend,v)
   def atEnd(s:String) = AjaxResponseFragment(s,Append,v)*/
}*/



trait EldarionAjaxResponseEncodings {

  import play.api.http.{ContentTypeOf, ContentTypes, Writeable}

  /*
  object BsAjaxResponse {
    def withHtml(t: RenderableTemplateConfig) =
      BsAjaxResponse(html=Some(t))
  }
*/
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

  /*implicit def writeableOf_AjaxRedirect(implicit codec: Codec): Writeable[EldarionAjaxRedirect] =
    Writeable[EldarionAjaxRedirect]((element: EldarionAjaxRedirect) => codec.encode(element.render()))

  implicit def contentTypeOf_AjaxRedirect(implicit codec: Codec): ContentTypeOf[EldarionAjaxRedirect] =
    ContentTypeOf[EldarionAjaxRedirect](Some(ContentTypes.JSON))

  implicit def writeableOf_AjaxResponseElement(implicit codec: Codec): Writeable[EldarionAjaxResponseElement] =
    Writeable[EldarionAjaxResponseElement]((element: EldarionAjaxResponseElement) => codec.encode(EldarionAjaxResponse(Seq(element)).render()))

  implicit def contentTypeOf_AjaxResponseElement(implicit codec: Codec): ContentTypeOf[EldarionAjaxResponseElement] =
    ContentTypeOf[EldarionAjaxResponseElement](Some(ContentTypes.JSON))

  */

  // convenience since we can't use chained implicits

  /*
  implicit def writeableOf_AjaxResponseElementSet(implicit codec: Codec): Writeable[GenTraversable[EldarionAjaxResponseElement]] =
    Writeable[GenTraversable[EldarionAjaxResponseElement]]((elements: GenTraversable[EldarionAjaxResponseElement]) => {
      codec.encode(EldarionAjaxFragmentsResponse(elements).renderString())
    })

  implicit def contentTypeOf_AjaxResponseElementSet(implicit codec: Codec): ContentTypeOf[GenTraversable[EldarionAjaxResponseElement]] =
    ContentTypeOf[GenTraversable[EldarionAjaxResponseElement]](Some(ContentTypes.JSON))
*/
  
  implicit def writeableOf_AjaxResponse(implicit codec: Codec): Writeable[EldarionAjaxResponse] =
    Writeable[EldarionAjaxResponse]((element: EldarionAjaxResponse) => {
      codec.encode(element.renderString())
    })

  implicit def contentTypeOf_AjaxResponse(implicit codec: Codec): ContentTypeOf[EldarionAjaxResponse] =
    ContentTypeOf[EldarionAjaxResponse](Some(ContentTypes.JSON))


  
  //implicit def toAjaxResponse(elements : GenSet[EldarionAjaxResponseElement]) : EldarionAjaxResponse = EldarionAjaxResponse(elements)
  //implicit def toAjaxResponse(element : EldarionAjaxResponseElement) : EldarionAjaxResponse = EldarionAjaxResponse(Set(element))
}
