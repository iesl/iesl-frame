package lib.ajax

import play.api.libs.json.Json._
import scala.collection.Traversable
import play.api.libs.json.JsValue
import play.api.mvc.Codec


/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 */

//object EldarionAjax {
//  import scala.language.implicitConversions
//  
//  implicit def toSelector(s: String) = EldarionAjaxExplicitSelector(s)
//  
// 
//  def html(s:String) = HtmlString(s)
//  implicit def view(it: AnyRef) = EldarionAjaxViewableModel(it, "index", "")
//  def exec(js:String) = EldarionAjaxJsOnly(js)
// 
//}


trait EldarionAjaxResponseEncodings {

  import play.api.http.{ContentTypeOf, ContentTypes, Writeable}

  
  implicit def writeableOf_AjaxResponse(implicit codec: Codec): Writeable[EldarionAjaxResponse] =
    Writeable[EldarionAjaxResponse]((element: EldarionAjaxResponse) => {
      codec.encode(element.renderString())
    })

  implicit def contentTypeOf_AjaxResponse(implicit codec: Codec): ContentTypeOf[EldarionAjaxResponse] =
    ContentTypeOf[EldarionAjaxResponse](Some(ContentTypes.JSON))


}
