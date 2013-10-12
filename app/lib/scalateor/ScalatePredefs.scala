package lib.template

import org.fusesource.scalate._

import play.api.http.{ContentTypeOf, ContentTypes, Writeable}
import play.api.mvc._


case class StringAsHtml(val cont: String)

object ScalateResponseEncodings extends ScalateResponseEncodings

trait ScalateResponseEncodings {

  implicit def writeableOf_ViewConfig(implicit codec: Codec): Writeable[ViewConfig] = 
    Writeable[ViewConfig]((vconf: ViewConfig) => codec.encode(vconf.render))

  implicit def contentTypeOf_ViewConfig(implicit codec: Codec): ContentTypeOf[ViewConfig] =
    ContentTypeOf[ViewConfig](Some(ContentTypes.HTML))


  implicit def writeableOf_ModelConfig(implicit codec: Codec): Writeable[ModelConfig] = 
    Writeable[ModelConfig]((conf:ModelConfig) => codec.encode(conf.render))

  implicit def contentTypeOf_ModelConfig(implicit codec: Codec): ContentTypeOf[ModelConfig] =
    ContentTypeOf[ModelConfig](Some(ContentTypes.HTML))


  implicit def writeableOf_ScalateContent(implicit codec: Codec): Writeable[StringAsHtml] = 
    Writeable[StringAsHtml]((s: StringAsHtml) => codec.encode(s.cont))

  implicit def contentTypeOf_ScalateContent(implicit codec: Codec): ContentTypeOf[StringAsHtml] =
    ContentTypeOf[StringAsHtml](Some(ContentTypes.HTML))

}
