//import com.typesafe.sbteclipse.core.EclipsePlugin.EclipseKeys
import sbt._
import sbt.Keys._
import play.Play.autoImport._
import PlayKeys._
import play.twirl.sbt.SbtTwirl
import play.twirl.sbt.Import.TwirlKeys
import com.gravitydev.scoop.sbt.ScoopPlugin.autoImport._
//import _root_.com.typesafe.sbteclipse.plugin.EclipsePlugin._

lazy val commonSettings = Seq(
  scalaVersion := "2.11.7",
  offline := true,
  organization in ThisBuild := "io.CalendarScheduler",
  resolvers ++= Seq(
    "typesafe" at "http://repo.typesafe.com/typesafe/releases/",
    Resolver.url("typesafe2", url("http://dl.bintray.com/typesafe/ivy-releases"))(Resolver.ivyStylePatterns),
    "gravity" at "https://devstack.io/repo/gravitydev/public",
    Resolver.url("Edulify Repository", url("https://edulify.github.io/modules/releases/"))(Resolver.ivyStylePatterns)
  )
)

lazy val core = Project("core",  file("core"))
  .enablePlugins(com.gravitydev.scoop.sbt.ScoopPlugin)
  .settings(com.gravitydev.scoop.sbt.ScoopPlugin.projectSettings:_*)
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      jdbc,
      ws,
      "org.scala-lang"              % "scala-reflect" % "2.11.6",
      "mysql"                       % "mysql-connector-java" % "5.1.36",
      "org.mnode.ical4j"            % "ical4j" % "1.0.2",
      "commons-io"                  % "commons-io" % "2.4",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
      "com.edulify"                %% "play-hikaricp" % "2.0.6",
      "com.typesafe.play"          %% "play-json" % "2.4.6",
      "com.typesafe.play"          %% "play-ws" % "2.4.6",
      "org.scalaz"                 %% "scalaz-core" % "7.1.3",
      "com.typesafe.akka"          %% "akka-actor" % "2.3.2",
      "com.squants"                %% "squants"  % "0.5.3",
      "com.gravitydev"             %% "trigger" % "0.1.3-SNAPSHOT",
      "com.gravitydev"             %%  "scoop"   % "1.1.0-SNAPSHOT",
      "org.scalamock"              %% "scalamock-scalatest-support" % "3.2" % "test",
      "com.google.api.client" % "google-api-client" % "1.4.0-alpha",
      "com.googlecode.kiama"        % "kiama_2.11" % "2.0.0-SNAPSHOT"
    ),
    EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Managed,
    EclipseKeys.withSource := true,
    scoopPackage := "mysql",
    scoopJdbcUrl := "jdbc:mysql://localhost/calendar?characterEncoding=UTF-8",
    scoopJdbcUsername := "root",
    scoopJdbcPassword := "",
    scoopMapType := {(tpe: Int) =>
      tpe match {
        case java.sql.Types.TIMESTAMP => "org.joda.time.DateTime"
        case java.sql.Types.DATE => "org.joda.time.LocalDate"
        case x => scoopMapType.value(x)	
      }
    }
  )

lazy val root = Project("site",  file("."))
  .dependsOn(core).aggregate(core)
  .enablePlugins(PlayScala,SbtWeb)
  .settings(commonSettings: _*)
  .settings(  
    offline := true,
    libraryDependencies ++= Seq(
      filters,
      cache,
      //"org.scala-lang"              % "scala-reflect" % "2.11.6",
      //"org.slf4j"                   % "slf4j-api" % "1.7.5",
      "javax.servlet" % "servlet-api" % "2.4",
      //"mysql"                       % "mysql-connector-java" % "5.1.36",
      //"org.mnode.ical4j"            % "ical4j" % "1.0.2",
      //"com.google.api.client"       % "google-api-client" % "1.4.0-alpha",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
      //"com.edulify"                %% "play-hikaricp" % "2.0.6",
      //"com.typesafe.akka"          %% "akka-actor" % "2.3.2",
      "org.scalatestplus"          %% "play" % "1.4.0" % "test",
      "org.webjars"                %% "webjars-play" % "2.4.0-1",
      "org.webjars"                 % "normalize.css" % "3.0.2",
      "org.webjars"                 % "flat-ui"          % "bcaf2de95e",
      "org.webjars"                 % "react" % "0.13.3"
    ),
    EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Managed,
    EclipseKeys.withSource := true,
    TwirlKeys.templateImports += "play.api.Play.current,controllers.AuthRequest", //bmotticus.BMPlugin
    ReactJsKeys.harmony := true,
    ReactJsKeys.es6module := false//,
    //routesGenerator := InjectedRoutesGenerator,
  )

