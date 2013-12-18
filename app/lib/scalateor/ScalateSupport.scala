package lib.scalateor

import org.fusesource.scalate._

import java.io.File
import play.api._
import play.api.mvc._
import lib.IFUser
import java.util.UUID
import status.SessionMessage
import play.api.libs.json.JsValue
import play.api.libs.json.Json._

import org.fusesource.scalate.ScalateLayout
import org.fusesource.scalate.ViewConfig
import org.fusesource.scalate.ModelConfig
import lib.ajax.EldarionAjaxResponseEncodings



trait NotRunningPlayConfig extends ScalateConfig {
  override def templateRootPaths: Seq[File] = Seq(getFile("/app/templates"))
}

trait RunningPlayConfig extends NotRunningPlayConfig {

  import play.api.Configuration
  import play.api.Play.current

  def conf: Configuration = Play.configuration

  override def mode: ScalateMode = Play.mode match {
    case Mode.Dev => DevMode
    case Mode.Prod => ProductionMode
  }

  override def getFile(s: String): java.io.File = Play.getFile(s)

  override def classloader: java.lang.ClassLoader = Play.classloader
}

object engineFactory extends EngineContainer with RunningPlayConfig {
  override def mode: ScalateMode = _mode
  var _mode: ScalateMode = DevMode

  // play.mode match ...
  def configurator: EngineLike => Unit = {
    e => ()
  }

  val templateEngine = configureEngine {
    eng =>
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


trait ScalateTemplateImplicits {
  import scala.language.implicitConversions
  
  implicit def stringToConfig(uri: String)(
    implicit eng: CustomTemplateEngine
    ) = ViewConfig(uri).withEngine(eng)


  implicit def anyRefToConfigOps(model: AnyRef)(
    implicit eng: CustomTemplateEngine
    ) = ModelConfig(model).withEngine(eng)


}

trait ScalateControllerImplicits {
  import scala.language.implicitConversions

  implicit val defaultLayout = ScalateLayout("/layout/layout.jade")

  // no need for ".template", except to avoid ambiguous implicits vs. the Ajax versions.
  // OK, but gall it ".page" instead to highlight the fact that these are not ajax.

  implicit def stringToConfigOps(uri: String)(
    implicit eng: CustomTemplateEngine, request: RequestHeader, layout: ScalateLayout, userOpt: Option[IFUser[UUID]] = None
    ) = new {
    def page: ViewConfig = {

      val baseAttrs: Seq[(String, Any)] = Seq(
        "userOpt" -> userOpt,
        "sessionMessages" -> SessionMessage.get)

      val attrs = if (userOpt.isDefined) baseAttrs :+ ("user" -> userOpt.get) else baseAttrs

      val v = ViewConfig(uri)
        .withEngine(eng)
        .withRequestHeader(request)
        .withLayout(layout.uri)
        .addAttribs(attrs: _*)
      v
    }
  }

  implicit def anyRefToConfigOps(model: AnyRef)(
    implicit eng: CustomTemplateEngine, request: RequestHeader, layout: ScalateLayout, userOpt: Option[IFUser[UUID]] = None
    ) = new {
    def page: ModelConfig = {
      val baseAttrs: Seq[(String, Any)] = Seq(
        "userOpt" -> userOpt,
        "sessionMessages" -> SessionMessage.get)

      val attrs = if (userOpt.isDefined) baseAttrs :+ ("user" -> userOpt.get) else baseAttrs

      val v = ModelConfig(model)
        .withEngine(eng)
        .withRequestHeader(request)
        .withLayout(layout.uri)
        .addAttribs(attrs: _*)
      v
    }
  }

}


trait TemplateSupport extends ConfiguredScalateEngine with ScalateTemplateImplicits

trait ScalateControllerSupport extends ConfiguredScalateEngine with ScalateResponseEncodings with EldarionAjaxResponseEncodings with ScalateControllerImplicits

object ScalateApp {

  import java.io.File

  object factory extends ScalateEngineFactory with NotRunningPlayConfig {
    override def getFile(s: String): File = new File(new File("./prj-openreview-front/"), s)

    override def classloader: java.lang.ClassLoader = Thread.currentThread.getContextClassLoader()

    override val mode: ScalateMode = PrecompileMode

    val templateEngine = configureEngine {
      eng =>
    }
  }

  val engine = factory.templateEngine

  def main(args: Array[String]) {
    engine.precompileAll()
  }

}
