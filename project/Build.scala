import sbt._
import sbt.Keys._
//import _root_.sbtassembly.Plugin.AssemblyKeys
import edu.umass.cs.iesl.sbtbase.Dependencies
import edu.umass.cs.iesl.sbtbase.IeslProject._
// import sbtassembly.Plugin._
// import AssemblyKeys._

import play.Project._

object IeslFrameBuild extends Build {

  val vers = "0.1-SNAPSHOT"
  val organization = "edu.umass.cs.iesl"

  implicit val allDeps: Dependencies = new Dependencies()

  import allDeps._
  
  val deps = Seq(
    "edu.umass.cs.iesl" %% "play-navigator" % "0.4.0" exclude("org.scala-stm", "scala-stm_2.10.0"),
    "org.apache.commons" % "commons-email" % "1.2" exclude("org.scala-stm", "scala-stm_2.10.0"),
    "org.mindrot" % "jbcrypt" % "0.3m" exclude("org.scala-stm", "scala-stm_2.10.0"),
    "org.ccil.cowan.tagsoup" % "tagsoup" % "latest.release" exclude("org.scala-stm", "scala-stm_2.10.0"),
    
    // 

     
    // it's OK for the webapp to depend on the mongo layer for now, even though it should be entirely independent
    // until the runtime wiring / cake baking
    "securesocial" %% "securesocial" % "2.1.1" exclude("org.scala-stm", "scala-stm_2.10.0"),

    // todo : figure out which of these are not actually used locally
    // (I had to include them explicitly due to mysterious issues with transitive resolution)

    "net.openreview" %% "scalate-core" % "latest.integration",
    "net.openreview" %% "scalate-core-plus" % "latest.integration",
    "net.openreview" %% "scalate-util" % "latest.integration",
    //"javax.servlet" % "servlet-api" % "latest.release",
    scalaCompiler() exclude("org.scala-stm", "scala-stm_2.10.0"),
    scalaIoFile() exclude("org.scala-stm", "scala-stm_2.10.0"),
    scalazCore() exclude("org.scala-stm", "scala-stm_2.10.0"),
    scalatime() exclude("org.scala-stm", "scala-stm_2.10.0"),
    ieslScalaCommons("latest.integration") exclude("org.scala-stm", "scala-stm_2.10.0"),
    //"javax.transaction" % "jta" % "latest.release",
    "net.sf.ehcache" % "ehcache-core" % "latest.release" exclude("org.scala-stm", "scala-stm_2.10.0"),
    // "org.scalaz" %% "scalaz-concurrent" % "latest.release",
    "org.json4s" %% "json4s-native" % "latest.release" exclude("org.scala-stm", "scala-stm_2.10.0")
  )

  lazy val ieslFrame = {
    (play.Project("iesl-frame", vers, path = file("."))
      .ieslSetup(vers, deps, Public, WithSnapshotDependencies, org = organization, conflict = ConflictStrict)
      .cleanLogging.standardLogging
    )
  }

}





