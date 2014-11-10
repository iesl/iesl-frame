import sbt._
import sbt.Keys._
//import edu.umass.cs.iesl.sbtbase.Dependencies
//import edu.umass.cs.iesl.sbtbase.IeslProject._

import edu.umass.cs.iesl.sbtbase.{IeslProject => Iesl, Config=>IeslConfig}

import play.Play.autoImport._
import play.Play._
import PlayKeys._

object IeslFrameBuild extends Build {

  val scalaV = "2.11.2"
  val vers = "0.1-SNAPSHOT"
  val org = "edu.umass.cs.iesl"

  //implicit val allDeps: Dependencies = new Dependencies()

  //import allDeps._
  
  val deps = Seq(
    //"eu.teamon" %% "play-navigator" % "0.5.0",
    "org.apache.commons" % "commons-email" % "1.2",
    "org.mindrot" % "jbcrypt" % "0.3m",
    "org.ccil.cowan.tagsoup" % "tagsoup" % "latest.release",
    
    "ws.securesocial" %% "securesocial" % "3.0-M1", 
    //"ws.securesocial" %% "securesocial" % "2.1.3",

    //"net.openreview" %% "scalate-core" % "latest.integration",
    //"net.openreview" %% "scalate-core-plus" % "latest.integration",
    //"net.openreview" %% "scalate-util" % "latest.integration",
    "com.scalatags" %% "scalatags" % "0.4.1",

    // scalaCompiler() ,
    // scalaIoFile() ,
    //"com.github.scala-incubator.io" %% "scala-io-file" % "latest.release",
    "org.scalaz" %% "scalaz-core" % "7.1.0",
    "com.github.nscala-time"        %% "nscala-time" % "latest.release",
    "edu.umass.cs.iesl"             %% "scalacommons" % "latest.integration",

    "net.sf.ehcache" % "ehcache-core" % "latest.release" ,
    "org.json4s" %% "json4s-native" % "latest.release"
  )


  lazy val root = ((project in file(".")).enablePlugins(play.PlayScala)
    .settings(Iesl.scalaSettings(Iesl.DebugVars):_*)
    .settings(
      name := "iesl-frame",
      organization := org,
      Iesl.setConflictStrategy(Iesl.ConflictStrict),
      resolvers ++= (
        IeslConfig.IESLReleaseRepos ++ IeslConfig.IESLSnapshotRepos ++ Seq(
          "anormcypher" at "http://repo.anormcypher.org/",
          "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/"
        )
      ),
      scalaVersion := scalaV,
      libraryDependencies ++= deps
    )
  )

}

