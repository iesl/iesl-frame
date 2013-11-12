package lib.scalateor

import org.fusesource.scalate._

import java.io.File
import play.api._
import play.api.http.Status._
import play.api.libs.json.JsValue
import play.api.libs.json.Json._
import play.api.mvc._
import lib.IFUser
import java.util.UUID

//import org.fusesource.scalate.ScalateSupportApp.engineFactory


trait NotRunningPlayConfig extends ScalateConfig {
  override def templateRootPaths: Seq[File] = Seq(getFile("/app/templates"))
  // import play.api.Configuration
  // import play.api.Play.current
  // def conf: Configuration = Play.configuration
  // override val mode: ScalateMode =
  // override def getFile(s:String):java.io.File = Play.getFile(s)
  // override def classloader: java.lang.ClassLoader = Play.classloader
}

trait RunningPlayConfig extends NotRunningPlayConfig {
  import play.api.Configuration
  import play.api.Play.current

  def conf: Configuration = Play.configuration

  override val mode: ScalateMode = Play.mode match {
    case Mode.Dev => DevMode
    case Mode.Prod => ProductionMode
  }

  override def getFile(s:String):java.io.File = Play.getFile(s)
  override def classloader: java.lang.ClassLoader = Play.classloader
}

object engineFactory extends EngineContainer with RunningPlayConfig {
  override val layoutMode = false
  override val mode: ScalateMode = DevMode // play.mode match ...
  def configurator: EngineLike => Unit = {e => ()}

  val templateEngine = configureEngine { eng =>
  }
}

trait ConfiguredScalateEngine {
  implicit def templateEngine = {
    engineFactory.templateEngine.reportConfig()
    engineFactory.templateEngine
  }

  // TODO Fix this!!!!
  implicit def customEngine = templateEngine.asInstanceOf[CustomTemplateEngine]

}

/*
class EngineFactory(_paths : Seq[File]) extends EngineContainer with RunningPlayConfig {
  override val layoutMode = false
  override val mode: ScalateMode = DevMode // todo play.mode match ...
  def configurator: EngineLike => Unit = {e => ()}

  val templateEngine : EngineLike = configureEngine { eng =>
  }

  override def templateRootPaths = _paths
}

object GlobalEngine {
  private var _engine : EngineLike = null
  def setEngine(e:EngineLike) { _engine = e}
  def apply() = _engine 
}

trait ConfiguredScalateEngine {
  
  
  implicit def templateEngine = {
    //engineFactory.templateEngine.reportConfig()
    //engineFactory.templateEngine

    GlobalEngine().reportConfig()
    GlobalEngine()
  }
/*
  // TODO Fix this!!!!
  implicit def customEngine = templateEngine.asInstanceOf[CustomTemplateEngine]
  */
  //implicit def customEngine : CustomTemplateEngine

}
*/
trait ScalateTemplateImplicits {

  implicit def stringToConfig(uri:String)(
    implicit eng:CustomTemplateEngine
  ) = new {
    def template: ViewConfig = 
      ViewConfig(uri).withEngine(eng)
  }

  implicit def anyRefToConfigOps(model:AnyRef)(
    implicit eng:CustomTemplateEngine
  ) = new {
    def template: ModelConfig = 
      ModelConfig(model).withEngine(eng)
  }

}

trait ScalateControllerImplicits {

  implicit val defaultLayout = ScalateLayout("/layout/layout.jade")

  implicit def stringToConfigOps(uri:String)(
    implicit eng:CustomTemplateEngine, request: RequestHeader, layout:ScalateLayout
  ) = new {
    def template: ViewConfig = 
      ViewConfig(uri)
        .withEngine(eng)
        .withRequestHeader(request)
        .withLayout(layout.uri)
  }

  implicit def anyRefToConfigOps(model:AnyRef)(
    implicit eng:CustomTemplateEngine, request: RequestHeader, layout:ScalateLayout
  ) = new {
    def template: ModelConfig = 
      ModelConfig(model)
        .withEngine(eng)
        .withRequestHeader(request)
        .withLayout(layout.uri)
  }

}




trait BsAjaxResponseEncodings {
  import org.fusesource.scalate.{RenderableTemplateConfig, Utils}
  import play.api.http.{ContentTypeOf, ContentTypes, Writeable}

  object BsAjaxResponse {
    def withHtml(t: RenderableTemplateConfig) =
      BsAjaxResponse(html=Some(t))
  }

  case class BsAjaxResponse(
    val html: Option[RenderableTemplateConfig] = None,
    val fragments: Option[String] = None
  ) {
    def withHtml(t: RenderableTemplateConfig) =
      this.copy(html=Some(t))

    def render():String = {
      // toJson(Map("html" -> toJson(html),"fragments" -> toJson(jsonFragments), "inner-fragments" ->toJson(jsonInnerFragments)))
      Utils.prettyPrintJson(toJson(List(
        html.map("html" -> _.render())
      ).filter(_.isDefined).map(_.get).toMap))
    }
  }
  
  implicit def writeableOf_BsAjaxResponse(implicit codec: Codec): Writeable[BsAjaxResponse] =
    Writeable[BsAjaxResponse]((vconf:BsAjaxResponse) => codec.encode(vconf.render()))

  implicit def contentTypeOf_BsAjaxResponse(implicit codec: Codec): ContentTypeOf[BsAjaxResponse] =
    ContentTypeOf[BsAjaxResponse](Some(ContentTypes.JSON))
}

trait TemplateSupport extends ConfiguredScalateEngine  with ScalateTemplateImplicits

trait ScalateControllerSupport extends ConfiguredScalateEngine with ScalateResponseEncodings with BsAjaxResponseEncodings with ScalateControllerImplicits

object ScalateApp {
  import java.io.File
  object factory extends ScalateEngineFactory with NotRunningPlayConfig {
    override def getFile(s:String): File = new File(new File("./prj-openreview-front/"), s)
    override def classloader: java.lang.ClassLoader = Thread.currentThread.getContextClassLoader()

    override val layoutMode = false
    override val mode: ScalateMode = PrecompileMode

    val templateEngine = configureEngine { eng =>
    }
  }

  val engine = factory.templateEngine

  def main(args: Array[String]) {
    engine.precompileAll()
  }

}

/*
object ScalateApp {
  import java.io.File
  private class Factory(_paths: Seq[File]) extends ScalateEngineFactory with NotRunningPlayConfig {
    // todo abstract app directory.  For now, assume working directory is the app.
    // override def getFile(s:String): File = new File(new File("."), s)
    override def classloader: java.lang.ClassLoader = Thread.currentThread.getContextClassLoader()

    override val layoutMode = false
    override val mode: ScalateMode = PrecompileMode

    override def templateRootPaths = _paths
    
    val templateEngine = configureEngine { eng =>
    }
  }


  def main(args: Array[String]) {

    val engine = new Factory(args.map(new File(_))).templateEngine
    engine.precompileAll()
  }

}
*/
