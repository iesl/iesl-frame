package lib

trait IFUser[T] {
  def id: T
}

trait IFUserStore[T] {
  def get(id:T) : Option[IFUser[T]]
  def getByStringId(id:String) : Option[IFUser[T]]
  def findByEmail(s: String): Traversable[IFUser[T]]
  def findBySubstring(s: String): Traversable[IFUser[T]]
}

trait IFLinkedAccount[T] {
  def id: T
  def userId: T
  def providerUserId: String
  def authMethod: String
  def providerKey: String

}

trait IFLinkedAccountStore[T] {
  def findByProviderId(s: String, s1: String) : Option[IFLinkedAccount[T]]
}

//trait IFUserLoginState[T] {
//  def userIsLoggedIn: Boolean 
//  def user: IFUser[T]
//  def userOpt: Option[IFUser[T]]
//}


//trait IFUserRequestState {
//  import play.api.mvc._
// 
//  def request: RequestHeader
//  def requestOpt: Option[RequestHeader]
//}
