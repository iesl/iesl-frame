package lib.ajax

import play.api.libs.json.Json._
import scala.collection.Traversable
import play.api.libs.json.JsValue
import play.api.mvc.Codec



trait EldarionAjaxResponseEncodings {

  import play.api.http.{ContentTypeOf, ContentTypes, Writeable}
  
  implicit def writeableOf_AjaxResponse(implicit codec: Codec): Writeable[EldarionAjaxResponse] =
    Writeable[EldarionAjaxResponse]((element: EldarionAjaxResponse) => {
      codec.encode(element.renderString())
    })

  implicit def contentTypeOf_AjaxResponse(implicit codec: Codec): ContentTypeOf[EldarionAjaxResponse] =
    ContentTypeOf[EldarionAjaxResponse](Some(ContentTypes.JSON))
}
