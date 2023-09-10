val Http4sVersion = "1.0.0-M40"
val CirceVersion = "0.14.3"
val MunitVersion = "0.7.29"
val LogbackVersion = "1.2.11"
val MunitCatsEffectVersion = "1.0.7"
val doobieVersion = "1.0.0-RC3"
val h2Version = "2.2.220"
val flywayVersion = "9.22.0"

lazy val root = (project in file("."))
  .settings(
    organization := "com.example",
    name := "reviews_service",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.13.12",
    libraryDependencies ++= Seq(
      "org.http4s"      %% "http4s-ember-server" % Http4sVersion,
      "org.http4s"      %% "http4s-ember-client" % Http4sVersion,
      "org.http4s"      %% "http4s-circe"        % Http4sVersion,
      "org.http4s"      %% "http4s-dsl"          % Http4sVersion,
      "io.circe"        %% "circe-generic"       % CirceVersion,
      "org.tpolecat" %% "doobie-h2" % doobieVersion,
      "org.tpolecat" %% "doobie-hikari" % doobieVersion,
      "de.lhns" %% "doobie-flyway" % "0.4.0",
      "com.h2database" % "h2" % h2Version,
      "org.flywaydb" % "flyway-core" % flywayVersion,
      "org.scalameta"   %% "svm-subs"            % "20.2.0",
      "ch.qos.logback"  %  "logback-classic"     % LogbackVersion         % Runtime,
      "org.scalameta"   %% "munit"               % MunitVersion           % Test,
      "org.typelevel"   %% "munit-cats-effect-3" % MunitCatsEffectVersion % Test,
      "com.alejandrohdezma" %% "http4s-munit" % "0.15.1" % Test,
      "org.typelevel" %% "munit-cats-effect-3" % "1.0.6" % Test,
      "org.scalatest" %% "scalatest" % "3.2.17" % Test
    ),
    addCompilerPlugin("org.typelevel" %% "kind-projector"     % "0.13.2" cross CrossVersion.full),
    addCompilerPlugin("com.olegpy"    %% "better-monadic-for" % "0.3.1"),
    testFrameworks += new TestFramework("munit.Framework")
  )
