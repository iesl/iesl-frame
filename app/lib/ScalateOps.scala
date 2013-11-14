package lib

import play.api._
import play.api.mvc._
import org.fusesource.scalate._

import libs.json.JsValue
import play.api.libs.json.Json._

object ScalateOps extends Scalate 

// import lib.template.scalate.TemplateSupport

trait Scalate  { 
  type PlayRequest = play.api.mvc.Request[AnyContent]

  def global = Play.current.global.asInstanceOf[ScalateGlobal]

  def scalateEngine = global.engineContainer.engine.asInstanceOf[CustomTemplateEngine]

  def symsToStrings(ss: Seq[(Symbol, Any)]): Seq[(String, Any)] = ss.map{case (k, v) => k.name->v}

  def viewPage(it:AnyRef, view:String="index")(args: (Symbol, Any)*): StringAsHtml = {
    StringAsHtml(scalateEngine.model(it, view, symsToStrings(args):_*))
  }

  def viewHtml(it:AnyRef, view:String="index")(args: (Symbol, Any)*) : String = {
    scalateEngine.model(it, view, symsToStrings(args):_*)
  }

  /*
  def viewJson(it:AnyRef, view:String="index")(args: (Symbol, Any)*): JsValue = {
    val html = viewHtml(it,view)(args:_*)
    toJson(Map("html" -> html))
  }
  
  // use viewHtml to generate the fragments
  def viewJsonAndFragments(it:AnyRef, view:String="index")(args: (Symbol, Any)*)(fragments : Map[String,String],innerFragments : Map[String,String]): JsValue = {
    val html : String = viewHtml(it,view)(args:_*)
    val jsonFragments : Map[String, JsValue] = fragments.mapValues(s=>toJson(s))
    val jsonInnerFragments : Map[String, JsValue] = innerFragments.mapValues(s=>toJson(s))
    toJson(Map("html" -> toJson(html),"fragments" -> toJson(jsonFragments), "inner-fragments" ->toJson(jsonInnerFragments)))
  }

  def viewFragments(fragments : Map[String,String],innerFragments : Map[String,String]) = {
    val jsonFragments : Map[String, JsValue] = fragments.mapValues(s=>toJson(s))
    val jsonInnerFragments : Map[String, JsValue] = innerFragments.mapValues(s=>toJson(s))
    toJson(Map("fragments" -> toJson(jsonFragments), "inner-fragments" ->toJson(jsonInnerFragments)))
  }
  
  def redirJson(url:String) = toJson(Map("location" -> url))
*/
}



