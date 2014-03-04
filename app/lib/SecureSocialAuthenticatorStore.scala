package lib

import play.api.Application
import play.api.cache.Cache
import play.api.mvc._
import com.typesafe.scalalogging.slf4j.Logging

import securesocial.core._

import net.sf.ehcache.{CacheManager, Element, Ehcache}
import com.typesafe.scalalogging.slf4j.Logging
import org.joda.time.DateTime


//trait SecuresocialAuthenticator {
//  // def uuid             : UUID
//  def id             : String
//  def identityId     : IdentityId
//  def creationDate   : DateTime
//  def lastUsed       : DateTime
//  def expirationDate : DateTime
//}


//trait SecuresocialAuthenticatorStore {
//  /**
//   * Saves or updates the authenticator in the store
//   *
//   * @param authenticator the authenticator
//   * @return Error if there was a problem saving the authenticator or Unit if all went ok
//   */
//  def save(authenticator: Authenticator): Either[Error, Unit]
// 
//  /**
//   * Finds an authenticator by id in the store
//   *
//   * @param id the authenticator id
//   * @return Error if there was a problem finding the authenticator or an optional authenticator if all went ok
//   */
//  def find(id: String): Either[Error, Option[Authenticator]]
// 
//  /**
//   * Deletes an authenticator from the store
//   *
//   * @param id the authenticator id
//   * @return Error if there was a problem deleting the authenticator or Unit if all went ok
//   */
//  def delete(id: String): Either[Error, Unit]
//}



class SecuresocialAuthenticatorStorePlugin(app: Application) extends AuthenticatorStore(app) with Logging {

  def save(authenticator: Authenticator): Either[Error, Unit] = {
    /// Cache.set(authenticator.id,authenticator)
    // Right(())
    Left(new Error(""))
  }


  def find(id: String): Either[Error, Option[Authenticator]] = {
    // Right(Cache.getAs[Authenticator](id))
    Left(new Error(""))
  }
  def delete(id: String): Either[Error, Unit] = {
    // Cache.set(id, "", 1)
    // Right(())
    Left(new Error(""))
  }
}

//Authenticator(
//  id             = , // : String,
//  identityId     = , // : IdentityId,
//  creationDate   = , // : DateTime,
//  lastUsed       = , // : DateTime,
//  expirationDate =  // : DateTime
//)
