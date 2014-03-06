import sbt._
import sbt.Keys._
import edu.umass.cs.iesl.sbtbase.Dependencies
import edu.umass.cs.iesl.sbtbase.IeslProject._

import play.Project._

object IeslFrameBuild extends Build {

  val vers = "0.1-SNAPSHOT"
  val organization = "edu.umass.cs.iesl"

  implicit val allDeps: Dependencies = new Dependencies()

  import allDeps._
  
  val deps = Seq(
    "eu.teamon" %% "play-navigator" % "0.5.0",
    "org.apache.commons" % "commons-email" % "1.2",
    "org.mindrot" % "jbcrypt" % "0.3m",
    "org.ccil.cowan.tagsoup" % "tagsoup" % "latest.release",
    
    "ws.securesocial" %% "securesocial" % "2.1.3",

    "net.openreview" %% "scalate-core" % "latest.integration",
    "net.openreview" %% "scalate-core-plus" % "latest.integration",
    "net.openreview" %% "scalate-util" % "latest.integration",

    scalaCompiler() ,
    scalaIoFile() ,
    "org.scalaz" %% "scalaz-core" % "7.0.5",
    scalatime() ,
    ieslScalaCommons("latest.integration") ,

    "net.sf.ehcache" % "ehcache-core" % "latest.release" ,
    "org.json4s" %% "json4s-native" % "latest.release"
  )

  lazy val ieslFrame = {
    (play.Project("iesl-frame", vers, path = file("."))
      .ieslSetup(vers, deps, Public, WithSnapshotDependencies, org = organization, conflict = ConflictStrict)
      .cleanLogging.standardLogging
    )
  }

}

