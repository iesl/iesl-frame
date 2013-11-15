package lib.scalateor

import org.fusesource.scalate._

import java.io.File
import play.api._
import play.api.mvc._
import lib.IFUser
import java.util.UUID
import org.fusesource.scalate.ScalateLayout
import org.fusesource.scalate.ViewConfig
import org.fusesource.scalate.ModelConfig
import status.SessionMessage

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

  override def getFile(s: String): java.io.File = Play.getFile(s)

  override def classloader: java.lang.ClassLoader = Play.classloader
}

object engineFactory extends EngineContainer with RunningPlayConfig {
  override val mode: ScalateMode = DevMode

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

  implicit def stringToConfig(uri: String)(
    implicit eng: CustomTemplateEngine
    ) = new {
    def template: ViewConfig =
      ViewConfig(uri).withEngine(eng)
  }

  implicit def anyRefToConfigOps(model: AnyRef)(
    implicit eng: CustomTemplateEngine
    ) = new {
    def template: ModelConfig =
      ModelConfig(model).withEngine(eng)
  }

}

trait ScalateControllerImplicits {

  implicit val defaultLayout = ScalateLayout("/layout/layout.jade")

  implicit def stringToConfigOps(uri: String)(
    implicit eng: CustomTemplateEngine, request: RequestHeader, layout: ScalateLayout, userOpt: Option[IFUser[UUID]] = None
    ) = new {
    def template: ViewConfig = {
      val v = ViewConfig(uri)
        .withEngine(eng)
        .withRequestHeader(request)
        .withLayout(layout.uri)
        .withAttribs("userOpt" -> userOpt)
        .withAttribs("sessionMessages" -> SessionMessage.get)
      if (userOpt.isDefined) v.withAttribs("user" -> userOpt.get) else v
    }
  }
  
  implicit def anyRefToConfigOps(model: AnyRef)(
    implicit eng: CustomTemplateEngine, request: RequestHeader, layout: ScalateLayout, userOpt: Option[IFUser[UUID]] = None
    ) = new {
    def template: ModelConfig = {
      val v = ModelConfig(model)
        .withEngine(eng)
        .withRequestHeader(request)
        .withLayout(layout.uri)
        .withAttribs("userOpt" -> userOpt)
        .withAttribs("sessionMessages" -> SessionMessage.get)
      if (userOpt.isDefined) v.withAttribs("user" -> userOpt.get) else v
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
