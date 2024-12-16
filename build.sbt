name := "dino-game"

version := "1.0"

scalaVersion := "3.3.4"

// Fork a new JVM for running the application
fork := true

// Set the required repositories for fetching dependencies
resolvers += Resolver.sonatypeRepo("releases")

// Dependencies
libraryDependencies ++= Seq(
  // Logging
  "ch.qos.logback" % "logback-classic" % "1.2.13",

  // Testing (for unit tests)
  "org.scalatest" %% "scalatest" % "3.2.15" % Test
)
