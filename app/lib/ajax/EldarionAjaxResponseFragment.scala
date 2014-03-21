package lib.ajax

import play.api.libs.json.JsValue
import play.api.libs.json.Json._

/**
  * Anything that can be rendered as html inside json, and possibly highlighted
  */
/*trait EldarionAjaxResponseElement {
   //extends RenderableTemplateConfig{
   def renderJs(): JsValue
 
   // def renderString() : String = Utils.prettyPrintJson(renderJs())
 }*/
/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 */
case class EldarionAjaxResponseFragment(target:EldarionAjaxTarget, v: AjaxViewable) { //textends EldarionAjaxResponseElement {
  def renderJs(): JsValue = v.renderJs()
}

