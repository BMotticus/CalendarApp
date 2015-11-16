import sbt._
import Keys._
import play.Play.autoImport._
import PlayKeys._
import play.twirl.sbt.SbtTwirl
import play.twirl.sbt.Import.TwirlKeys
import com.gravitydev.scoop.sbt.ScoopPlugin.autoImport._
import _root_.com.typesafe.sbteclipse.plugin.EclipsePlugin._

name := """play23"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala,SbtWeb,com.gravitydev.scoop.sbt.ScoopPlugin)
                                      .settings(com.gravitydev.scoop.sbt.ScoopPlugin.projectSettings:_*)
offline := true

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  ws,
  "org.scala-lang"              % "scala-reflect" % "2.11.6",
  "org.slf4j"                   % "slf4j-api" % "1.7.5",
  "mysql"                       % "mysql-connector-java" % "5.1.36",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
  "com.edulify"                %% "play-hikaricp" % "2.0.6",
  "com.gravitydev"             %%  "scoop"   % "1.1.0-SNAPSHOT",
  "org.scalaz"                 %% "scalaz-core" % "7.1.3",
  "com.squants"                %% "squants"  % "0.5.3",
  "org.scalatest"              %% "scalatest" % "2.2.4" % "test",
  "org.scalamock"              %% "scalamock-scalatest-support" % "3.2" % "test",
  "org.webjars"                %% "webjars-play" % "2.4.0-1",
  "org.webjars"                 % "bootstrap" % "3.3.5",
  "org.webjars"                 % "normalize.css" % "3.0.2",
  "org.webjars"                 % "animate.css" % "3.3.0",
  "org.webjars"                 % "jquery" % "1.11.0",
  "org.webjars"                 % "react" % "0.13.3"
)

CoffeeScriptKeys.sourceMap := true

CoffeeScriptKeys.bare := false

EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Managed

EclipseKeys.withSource := true

TwirlKeys.templateImports += "play.api.Play.current" //bmotticus.BMPlugin

ReactJsKeys.harmony := true

scoopPackage := "mysql"
scoopJdbcUrl := "jdbc:mysql://localhost/play23?characterEncoding=UTF-8"
scoopJdbcUsername := "root"
scoopJdbcPassword := ""
scoopMapType := {(tpe: Int) =>
  tpe match {
    case java.sql.Types.TIMESTAMP => "org.joda.time.DateTime"
    case java.sql.Types.DATE => "org.joda.time.LocalDate"
    case x => scoopMapType.value(x)	
  }
}
//scoopOverrideColumnType := {
//  
//}

resolvers ++= Seq(
  "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases",
  Resolver.url("Edulify Repository", url("https://edulify.github.io/modules/releases/"))(Resolver.ivyStylePatterns),
  "gravity" at "https://devstack.io/repo/gravitydev/public"
)


