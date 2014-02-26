package lib.ajax

import lib.ScalateOps
import play.api.libs.json.Json._
import scala.Some
import scala.collection.Traversable
import play.api.libs.json.JsValue
import play.api.mvc.Codec
import org.fusesource.scalate.Utils

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 */

object EldarionAjax {
  import scala.language.implicitConversions
  
  implicit def toSelector(s: String) = EldarionAjaxExplicitSelector(s)
  

  def html(s:String) = EldarionAjaxString(s)
  implicit def view(it: AnyRef) = EldarionAjaxScalateViewable(it, "index", "")
  def exec(js:String) = EldarionAjaxJsOnly(js)

}


trait EldarionAjaxResponseEncodings {

  import play.api.http.{ContentTypeOf, ContentTypes, Writeable}

  
  implicit def writeableOf_AjaxResponse(implicit codec: Codec): Writeable[EldarionAjaxResponse] =
    Writeable[EldarionAjaxResponse]((element: EldarionAjaxResponse) => {
      codec.encode(element.renderString())
    })

  implicit def contentTypeOf_AjaxResponse(implicit codec: Codec): ContentTypeOf[EldarionAjaxResponse] =
    ContentTypeOf[EldarionAjaxResponse](Some(ContentTypes.JSON))


}
