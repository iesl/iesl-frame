//package lib
// 
//import play.api._
//import play.api.mvc._
//import org.fusesource.scalate._
// 
//import libs.json.JsValue
//import play.api.libs.json.Json._
// 
//object ScalateOps extends Scalate 
// 
//// import lib.template.scalate.TemplateSupport
// 
//trait Scalate  { 
//  type PlayRequest = play.api.mvc.Request[AnyContent]
// 
//  def global = Play.current.global.asInstanceOf[ScalateGlobal]
// 
//  def scalateEngine = global.engineContainer.engine.asInstanceOf[CustomTemplateEngine]
// 
//  def symsToStrings(ss: Seq[(Symbol, Any)]): Seq[(String, Any)] = ss.map{case (k, v) => k.name->v}
// 
//  def viewPage(it:AnyRef, view:String="index")(args: (Symbol, Any)*): StringAsHtml = {
//    StringAsHtml(scalateEngine.model(it, view, symsToStrings(args):_*))
//  }
// 
//  def viewHtml(it:AnyRef, view:String="index")(args: (Symbol, Any)*) : String = {
//    scalateEngine.model(it, view, symsToStrings(args):_*)
//  }
// 
//}
 
 
 
