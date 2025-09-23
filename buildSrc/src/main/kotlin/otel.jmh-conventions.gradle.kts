plugins {
  id("me.champeau.jmh")
  id("io.morethan.jmhreport")
}

dependencies {
  jmh(platform(project(":dependencyManagement")))
  jmh("org.openjdk.jmh:jmh-core")
  jmh("org.openjdk.jmh:jmh-generator-bytecode")

  // This enables running JMH benchmark classes within IntelliJ using
  // JMH plugins
  jmh("org.openjdk.jmh:jmh-generator-annprocess")
  jmhAnnotationProcessor("org.openjdk.jmh:jmh-generator-annprocess")
}

// invoke jmh on a single benchmark class like so:
//   ./gradlew -PjmhIncludeSingleClass=StatsTraceContextBenchmark clean :grpc-core:jmh
jmh {
  failOnError.set(true)
  resultFormat.set("JSON")
  // Otherwise an error will happen:
  // Could not expand ZIP 'byte-buddy-agent-1.9.7.jar'.
  includeTests.set(false)
  profilers.add("gc")
  val jmhIncludeSingleClass: String? by project
  if (jmhIncludeSingleClass != null) {
    includes.add(jmhIncludeSingleClass as String)
  }

  val testJavaVersion = gradle.startParameter.projectProperties.get("testJavaVersion")?.let(JavaVersion::toVersion)
  if (testJavaVersion != null) {
    val javaExecutable = javaToolchains.launcherFor {
      languageVersion.set(JavaLanguageVersion.of(testJavaVersion.majorVersion))
    }.get().executablePath.asFile.absolutePath

    jvm.set(javaExecutable)
  }
}

jmhReport {
  val buildDirectory = layout.buildDirectory.asFile.get()
  jmhResultPath = file("$buildDirectory/results/jmh/results.json").absolutePath
  jmhReportOutput = file("$buildDirectory/results/jmh").absolutePath
}

tasks {
  named("jmh") {
    finalizedBy(named("jmhReport"))

    outputs.cacheIf { false }
  }
}
