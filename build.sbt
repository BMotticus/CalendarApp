import sbt._
import Keys._
import play.Play.autoImport._
import PlayKeys._
import play.twirl.sbt.SbtTwirl
import play.twirl.sbt.Import.TwirlKeys
import _root_.com.typesafe.sbteclipse.plugin.EclipsePlugin.{EclipseKeys, EclipseCreateSrc}

name := """play23"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala,SbtWeb)

offline := true

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  ws,
  "org.scala-lang" % "scala-reflect" % "2.11.6",
  "org.slf4j"      % "slf4j-api" % "1.7.5",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
  "mysql"          % "mysql-connector-java" % "5.1.36",
  "com.edulify"    %% "play-hikaricp" % "2.0.6",
  "org.scalaz"     %% "scalaz-core" % "7.1.3",
  "com.squants"    %% "squants"  % "0.5.3",
  "org.scalatest"  %% "scalatest" % "2.2.4" % "test",
  "org.scalamock"  %% "scalamock-scalatest-support" % "3.2" % "test",
  "org.webjars"    %% "webjars-play" % "2.4.0-1",
  "org.webjars"    % "bootstrap" % "3.3.5",
  "org.webjars"    % "normalize.css" % "3.0.2",
  "org.webjars"    % "animate.css" % "3.3.0",
  "org.webjars"    % "jquery" % "1.11.0",
  "org.webjars"    % "react" % "0.13.3"
)

CoffeeScriptKeys.sourceMap := true

CoffeeScriptKeys.bare := false

EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Managed

EclipseKeys.withSource := true

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

resolvers += Resolver.url("Edulify Repository", url("https://edulify.github.io/modules/releases/"))(Resolver.ivyStylePatterns)

