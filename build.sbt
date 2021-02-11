
name := "akka-bench-app"

version := "0.4"

scalaVersion := "2.13.1"

lazy val root = (project in file(".")).enablePlugins(JavaAppPackaging, sbtdocker.DockerPlugin, AshScriptPlugin, GraalVMNativeImagePlugin)

val namespace = "ioperezlaborda"

val slickVersion = "3.3.2"
val graalAkkaVersion = "0.5.0"

libraryDependencies ++= Seq(
  "org.slf4j"                % "slf4j-nop"       % "1.6.4",
  "com.typesafe.akka"        %% "akka-http"      % "10.1.9",
  "com.typesafe.akka"        %% "akka-stream"    % "2.5.24",
  "com.softwaremill.sttp"    %% "okhttp-backend" % "1.6.0", //"async-http-client-backend-future" % "1.6.4",
  "com.softwaremill.common"  %% "tagging"        % "2.2.1",
  "com.softwaremill.macwire" %% "macros"         % "2.3.2" % Provided,
  "com.github.vmencik" %% "graal-akka-http" % graalAkkaVersion,
  "com.github.vmencik" %% "graal-akka-slf4j" % graalAkkaVersion
)

graalVMNativeImageOptions ++= Seq(
  "-H:IncludeResources=.*\\.properties",
  "-H:IncludeResources=.*\\.conf",
  "-H:ReflectionConfigurationFiles=" + baseDirectory.value / "graal" / "reflectconf-jul.json",
  "-H:+TraceClassInitialization",
  //"--static",
  "--initialize-at-build-time",
  "--initialize-at-run-time=" +
    "akka.protobuf.DescriptorProtos," +
    "com.typesafe.config.impl.ConfigImpl$EnvVariablesHolder," +
    "com.typesafe.config.impl.ConfigImpl$SystemPropertiesHolder",
  "--no-fallback",
  "--allow-incomplete-classpath"
)


dockerfile in docker := {

  val appDir: File = stage.value
  val targetDir = "/app"

  new Dockerfile {
    from("adoptopenjdk/openjdk11:alpine-jre")//from("openjdk:11-jre-slim")
    entryPoint(s"$targetDir/bin/${executableScriptName.value}")
    copy(appDir, targetDir)
  }
}

scalacOptions := Seq(
  "-encoding",
  "utf8",
  "-g:vars",
  "-feature",
  "-unchecked",
  "-deprecation",
  "-target:11",
  "-language:postfixOps",
  "-language:implicitConversions",
  "-Xlog-reflective-calls"
)

imageNames in docker := {

  val current = Seq(ImageName(
    namespace = Some(namespace),
    repository = name.value,
    tag = Some(version.value)
  ))

  if(isSnapshot.value)
    current
  else
    current //:+ ImageName(s"$namespace/${name.value}:latest")
}