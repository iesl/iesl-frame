import sbt._
import sbt.Keys._
import sbt.Keys._


object IeslPluginLoader extends Build {
  
  // .settings(resolvers += Resolver.url("sbt-plugin-releases", new URL("http://repo.scala-sbt.org/scalasbt/sbt-plugin-releases/"))(Resolver.ivyStylePatterns))

  lazy val root = Project(id = "plugins", base = file("."))
    .settings(resolvers += "IESL Public Releases" at "http://dev-iesl.cs.umass.edu/nexus/content/groups/public")
    .settings(resolvers += "IESL Public Snapshots" at "http://dev-iesl.cs.umass.edu/nexus/content/groups/public-snapshots")
    .settings(addSbtPlugin("edu.umass.cs.iesl" %% "iesl-sbt-base" % "latest.release"))
    .settings(addSbtPlugin("com.typesafe.play" %% "sbt-plugin" % "2.2.1"))
    // .settings(addSbtPlugin("com.mojolly.scalate" % "xsbt-scalate-generator" % "0.4.2"))
    // .settings(addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.8.8"))
}
 
 
