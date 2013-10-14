package lib

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 */


object IeslFrameInfo {
  var _s : IeslFrameInfo = null
  def set(s:IeslFrameInfo) { _s = s }
  def assetsVersion = _s.assetsVersion
}

trait IeslFrameInfo {
  def assetsVersion : String
}
