//logLevel := Level.Warn

lazy val handlebarsPlugin = RootProject(uri("git://github.com/Amadeus82/sbt-handlebars.git#ed2a0670c00e25245a90afb5ac88fed4ca2b0f6b"))

lazy val playmatcherBuild = (project in file(".")).dependsOn(handlebarsPlugin)

resolvers ++= Seq(
  "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"
)

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.3.2"
  exclude("org.slf4j", "slf4j-simple")
  exclude("org.slf4j", "slf4j-nop")
)

addSbtPlugin("com.typesafe.sbt" % "sbt-coffeescript" % "1.0.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-rjs" % "1.0.1")

addSbtPlugin("com.typesafe.sbt" % "sbt-digest" % "1.0.0")

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.7.4")
