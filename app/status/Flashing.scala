package status

import play.api.mvc._

// trait Flashing {
//   def message: String
// }

case class FlashingRequest(request: RequestHeader, altMessage: Option[String]) {
  def flashingMessage = {
    val msgKeys = "success info error warning".split(" ").toList
    val msgs = (msgKeys foldLeft List[String]()) { 
      case (acc, e) =>
        val m = request.flash.get(e)
        if (m.isDefined)  {
          List("TODO: FlashingRequest")
          // viewHtml(FlashingMessage(m.get), e)() :: acc
        }
        else {
          acc
        }
    }
    (msgs.headOption.getOrElse {
      altMessage.map {m =>
        List("TODO: FlashingRequest")
        // viewHtml(FlashingMessage(m), "info")()
      }
    })
  }

}

case class FlashingMessage(
  val message:String,
  val level:String
)
