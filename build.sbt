import com.typesafe.sbt.coffeescript.Import.CoffeeScriptKeys
//import com.typesafe.sbteclipse.core.EclipsePlugin.EclipseKeys
import sbt._
import sbt.Keys._
import play.Play.autoImport._
import PlayKeys._
import play.twirl.sbt.SbtTwirl
import play.twirl.sbt.Import.TwirlKeys
import com.gravitydev.scoop.sbt.ScoopPlugin.autoImport._
import _root_.com.typesafe.sbteclipse.plugin.EclipsePlugin._

lazy val commonSettings = Seq(
  scalaVersion := "2.11.6",
  organization in ThisBuild := "io.CalendarScheduler",
  version := "1.0.1"
)

lazy val core = Project("core",  file("core"))
  .enablePlugins(com.gravitydev.scoop.sbt.ScoopPlugin)
  .settings(com.gravitydev.scoop.sbt.ScoopPlugin.projectSettings:_*)
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Seq( jdbc,ws,
      "org.scala-lang"              % "scala-reflect" % "2.11.6",
      //"org.slf4j"                   % "slf4j-api" % "1.7.5",
      "mysql"                       % "mysql-connector-java" % "5.1.36",
      "org.mnode.ical4j"            % "ical4j" % "1.0.2",
      //"commons-codec"               % "commons-codec" % "1.9",
      "commons-io"                  % "commons-io" % "2.4",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
      "com.edulify"                %% "play-hikaricp" % "2.0.6",
      "com.typesafe.play"          %% "play-json" % "2.3.9",
      "com.typesafe.play"          %% "play-ws" % "2.3.9",
      "org.scalaz"                 %% "scalaz-core" % "7.1.3",
      "com.typesafe.akka"          %% "akka-actor" % "2.3.2",
      //"com.squants"                %% "squants"  % "0.5.3",
      "com.gravitydev"             %% "trigger" % "0.1.3-SNAPSHOT",
      "com.gravitydev"             %%  "scoop"   % "1.1.0-SNAPSHOT",
      "org.scalamock"              %% "scalamock-scalatest-support" % "3.2" % "test",
      "com.google.api.client" % "google-api-client" % "1.4.0-alpha",
      "com.googlecode.kiama"        % "kiama_2.11" % "2.0.0-SNAPSHOT"/*,
      "org.eclipse.paho" % "org.eclipse.paho.client.mqttv3" % "1.0.2"*/
    ),
    EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Managed,
    EclipseKeys.withSource := true,
    scoopPackage := "mysql",
    scoopJdbcUrl := "jdbc:mysql://localhost/play23?characterEncoding=UTF-8",
    scoopJdbcUsername := "root",
    scoopJdbcPassword := "",
    scoopMapType := {(tpe: Int) =>
      tpe match {
        case java.sql.Types.TIMESTAMP => "org.joda.time.DateTime"
        case java.sql.Types.DATE => "org.joda.time.LocalDate"
        case x => scoopMapType.value(x)	
      }
    },
    resolvers ++= Seq(
      "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases",
      Resolver.url("Edulify Repository", url("https://edulify.github.io/modules/releases/"))(Resolver.ivyStylePatterns),
      "gravity" at "https://devstack.io/repo/gravitydev/public",
      "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
    )
  )

lazy val root = Project("site",  file("."))
  .dependsOn(core).aggregate(core)
  .enablePlugins(PlayScala,SbtWeb)
  .settings(commonSettings: _*)
  .settings(  
    offline := true,
    libraryDependencies ++= Seq(
      jdbc,
      cache,
      ws,
      //"org.scala-lang"              % "scala-reflect" % "2.11.6",
      //"org.slf4j"                   % "slf4j-api" % "1.7.5",
      //"mysql"                       % "mysql-connector-java" % "5.1.36",
      "org.mnode.ical4j"            % "ical4j" % "1.0.2",
      "com.google.api.client"       % "google-api-client" % "1.4.0-alpha",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
      "com.edulify"                %% "play-hikaricp" % "2.0.6",
      "com.typesafe.akka"          %% "akka-actor" % "2.3.2",
      "org.scalatest"              %% "scalatest" % "2.2.4" % "test",
      "org.scalamock"              %% "scalamock-scalatest-support" % "3.2" % "test",
      "org.webjars"                %% "webjars-play" % "2.4.0-1",
      "org.webjars"                 % "bootstrap" % "3.3.5",
      "org.webjars"                 % "normalize.css" % "3.0.2",
      "org.webjars"                 % "flat-ui"          % "bcaf2de95e",
      "org.webjars"                 % "react" % "0.13.3"
    ),
    CoffeeScriptKeys.sourceMap := true,
    CoffeeScriptKeys.bare := false,
    EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Managed,
    EclipseKeys.withSource := true,
    TwirlKeys.templateImports += "play.api.Play.current,controllers.AuthRequest", //bmotticus.BMPlugin
    ReactJsKeys.harmony := true,
    resolvers ++= Seq(
      "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases",
      Resolver.url("Edulify Repository", url("https://edulify.github.io/modules/releases/"))(Resolver.ivyStylePatterns)
    )
  )

