package lib.ajax

import play.api.libs.json.Json._
import scala.collection.Traversable
import play.api.libs.json.JsValue
import scala.collection

object Utils {

  import org.json4s._
  import org.json4s.native.JsonMethods
  import play.api.libs.json.{JsValue, Json}
  
  def playJsonToLiftJson(jsval: JsValue): JValue = {
    JsonMethods.parse(
      Json.stringify(jsval),
      useBigDecimalForDouble = false)
  }

  def prettyPrintJson(jsval: JsValue): String = {
    org.json4s.native.Printer.pretty(
      JsonMethods.render(playJsonToLiftJson(jsval))
    )
  }

}

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


// use Traversable instead of Set because Set is not covariant (!?)
case class EldarionAjaxFragmentsResponse(elements: Traversable[EldarionAjaxResponseFragment]) extends EldarionAjaxResponse {
  // constraint that there can be only one html is not enforced by type; if multiple are provided, one is picked.
  val noselector: Option[JsValue] = elements.find(
    e => e.target == TemplateDecidesTarget).headOption.map(e => e.renderJs())

  val elementsRendered = elements.collect({
    case EldarionAjaxResponseFragment(EldarionAjaxExplicitTarget(s, pos), v) => (s, pos, v.renderJs())
  })

  val elementsGrouped = elementsRendered.groupBy(_._2).mapValues(x => x.map(y => (y._1, y._3)).toMap)

  def renderJs(): JsValue = { 
    toJson(elementsGrouped.map({
      case (k, v) => {
        k.key -> toJson(v)
      }
    }).toMap ++ noselector.map(s => "noselector" -> s))
  }

  def renderString(): String = {
    val result = Utils.prettyPrintJson(renderJs())
    result
  }

}
