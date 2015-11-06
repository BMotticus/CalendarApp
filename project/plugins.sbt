logLevel := Level.Warn

resolvers ++= Seq(
  "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/",
  "grvt" at "https://devstack.io/repo/gravitydev/public",
  "devstack" at "https://devstack.io/repo/gravitydev/public"
)

// The Play plugin
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.3.10")

// web plugins
addSbtPlugin("com.typesafe.sbt" % "sbt-coffeescript" % "1.0.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-less" % "1.0.6")

addSbtPlugin("com.typesafe.sbt" % "sbt-jshint" % "1.0.3")

addSbtPlugin("com.typesafe.sbt" % "sbt-rjs" % "1.0.7")

addSbtPlugin("com.typesafe.sbt" % "sbt-digest" % "1.0.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-mocha" % "1.0.2")

addSbtPlugin("com.github.ddispaltro" % "sbt-reactjs" % "0.5.2")

addSbtPlugin("com.gravitydev" % "scoop-sbt-plugin" % "0.0.3-SNAPSHOT")

libraryDependencies ++= Seq(
  "mysql" % "mysql-connector-java" % "5.1.34",
  "joda-time" % "joda-time" % "2.7"
)

