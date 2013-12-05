package lib.ajax

import org.fusesource.scalate.Utils
import play.api.libs.json.Json._
import scala.collection.GenTraversable
import play.api.libs.json.JsValue
import scala.collection

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 */
trait EldarionAjaxResponse {
  def renderString(): String
}


case class EldarionAjaxRedirect(location: String) extends EldarionAjaxResponse {
  def renderString(): String = {
    // toJson(Map("html" -> toJson(html),"fragments" -> toJson(jsonFragments), "inner-fragments" ->toJson(jsonInnerFragments)))
    Utils.prettyPrintJson(toJson(Map(
      "location" -> location
    )))
  }
}


// use GenTraversable instead of GenSet because GenSet is not covariant (!?)
case class EldarionAjaxFragmentsResponse(elements: GenTraversable[EldarionAjaxResponseFragment]) extends EldarionAjaxResponse {
  // constraint that there can be only one html is not enforced by type; if multiple are provided, one is picked.
  val noselector: Option[JsValue] = elements.find(
    e => e.target == TemplateDecidesTarget).headOption.map(e => e.renderJs())

  val elementsRendered = elements.collect({
    case EldarionAjaxResponseFragment(EldarionAjaxExplicitTarget(s, pos), v) => (s, pos, v.renderJs())
  })

  val elementsGrouped = elementsRendered.groupBy(_._2).mapValues(x => x.map(y => (y._1, y._3)).seq.toMap)

  def renderJs(): JsValue = { 
    toJson(elementsGrouped.map({
      case (k, v) => {
        k.key -> toJson(v)
      }
    }).seq.toMap ++ noselector.map("noselector" ->))
  }

  def renderString(): String = {
    val result = Utils.prettyPrintJson(renderJs())
    result
  }

}