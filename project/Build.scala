import _root_.sbtassembly.Plugin.AssemblyKeys
import edu.umass.cs.iesl.sbtbase.IeslProject.ConflictStrict
import edu.umass.cs.iesl.sbtbase.IeslProject.Public
import edu.umass.cs.iesl.sbtbase.IeslProject.WithSnapshotDependencies
import sbt._
import Keys._
import edu.umass.cs.iesl.sbtbase.Dependencies
import edu.umass.cs.iesl.sbtbase.IeslProject._
import sbt.Keys._
import Keys._
import edu.umass.cs.iesl.sbtbase.Dependencies
import edu.umass.cs.iesl.sbtbase.IeslProject._

import sbtassembly.Plugin._
import AssemblyKeys._

import play.Project._

object IeslFrameBuild extends Build {

  val vers = "0.1-SNAPSHOT"
  val organization = "edu.umass.cs.iesl"

  implicit val allDeps: Dependencies = new Dependencies()

  import allDeps._
  
  val deps = Seq(
    "edu.umass.cs.iesl" %% "play-navigator" % "0.4.0",
    "org.apache.commons" % "commons-email" % "1.2",
    "org.mindrot" % "jbcrypt" % "0.3m",
    "org.ccil.cowan.tagsoup" % "tagsoup" % "latest.release",

    // it's OK for the webapp to depend on the mongo layer for now, even though it should be entirely independent
    // until the runtime wiring / cake baking
    "net.openreview" %% "securesocial" % "latest.integration",

    // todo : figure out which of these are not actually used locally
    // (I had to include them explicitly due to mysterious issues with transitive resolution)

    "net.openreview" %% "scalate-core" % "latest.integration",
    "net.openreview" %% "scalate-core-plus" % "latest.integration",
    "net.openreview" %% "scalate-util" % "latest.integration",
    //"javax.servlet" % "servlet-api" % "latest.release",
    scalaCompiler(),
    scalaIoFile(),
    scalazCore(),
    scalatime(),
    ieslScalaCommons("latest.integration"),
    //"javax.transaction" % "jta" % "latest.release",
    "net.sf.ehcache" % "ehcache-core" % "latest.release",
    "org.scalaz" %% "scalaz-concurrent" % "latest.release",
    "org.json4s" %% "json4s-native" % "latest.release"
  )

  lazy val ieslFrame = {
    (play.Project("iesl-frame", vers, path = file("."))
      .ieslSetup(vers, deps, Public, WithSnapshotDependencies, org = organization, conflict = ConflictStrict)
      .cleanLogging.standardLogging
      )
  }

}





