package lib.ajax

import play.api.libs.json.JsValue


/**
 * A location on the page where the html will go.  Typically combines a selector and a position.
 */
trait EldarionAjaxTarget {
  def apply(v:EldarionAjaxViewable) = new EldarionAjaxResponseFragment(this, v)
}

/**
 * Just return some html without any information about where it goes; then the client-side javascript must have "data-replace" etc.
 */
object TemplateDecidesTarget extends EldarionAjaxTarget

case class EldarionAjaxExplicitTarget(selector: String,  pos: EldarionAjaxFragmentPosition) extends EldarionAjaxTarget

case class EldarionAjaxExplicitSelector(selector: String, selectClosest: Boolean = false) {

  def closest = EldarionAjaxExplicitSelector(selector, true)

  def replaceWith(v: EldarionAjaxViewable) = new EldarionAjaxResponseFragment(EldarionAjaxExplicitTarget(selector, if (selectClosest) ReplaceClosest else Replace), v)

  def replaceInside(v: EldarionAjaxViewable) = new EldarionAjaxResponseFragment(EldarionAjaxExplicitTarget(selector, if (selectClosest) InnerClosest else Inner), v)

  def prepend(v: EldarionAjaxViewable) = new EldarionAjaxResponseFragment(EldarionAjaxExplicitTarget(selector, if (selectClosest) PrependClosest else Prepend), v)

  def append(v: EldarionAjaxViewable) = new EldarionAjaxResponseFragment(EldarionAjaxExplicitTarget(selector, if (selectClosest) AppendClosest else Append), v)

  def <>(v: EldarionAjaxViewable) = replaceWith(v)

  def ><(v: EldarionAjaxViewable) = replaceInside(v)

  def <<(v: EldarionAjaxViewable) = prepend(v)

  def >>(v: EldarionAjaxViewable) = append(v)
}


sealed class EldarionAjaxFragmentPosition(val key:String)

case object Replace extends EldarionAjaxFragmentPosition("fragments")

case object Inner extends EldarionAjaxFragmentPosition("inner-fragments")

case object Prepend extends EldarionAjaxFragmentPosition("prepend-fragments")

case object Append extends EldarionAjaxFragmentPosition("append-fragments")

case object ReplaceClosest extends EldarionAjaxFragmentPosition("fragments-closest")

case object InnerClosest extends EldarionAjaxFragmentPosition("inner-fragments-closest")

case object PrependClosest extends EldarionAjaxFragmentPosition("prepend-fragments-closest")

case object AppendClosest extends EldarionAjaxFragmentPosition("append-fragments-closest")

