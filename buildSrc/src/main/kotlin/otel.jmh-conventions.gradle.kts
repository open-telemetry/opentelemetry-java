plugins {
  id("me.champeau.jmh")
  id("io.morethan.jmhreport")
}

dependencies {
  jmh(platform(project(":dependencyManagement")))
  jmh("org.openjdk.jmh:jmh-core")
  jmh("org.openjdk.jmh:jmh-generator-bytecode")
}

val testJavaVersion = gradle.startParameter.projectProperties.get("testJavaVersion")?.let(JavaVersion::toVersion)

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

  if (testJavaVersion != null) {
    var toolchain = javaToolchains.launcherFor {
      languageVersion.set(JavaLanguageVersion.of(testJavaVersion.majorVersion))
    };
    javaLauncher.set(toolchain)
    jvm.set(toolchain.map { it -> it.executablePath.asFile.absolutePath }.orNull)
  }
}

jmhReport {
  jmhResultPath = file("$buildDir/results/jmh/results.json").absolutePath
  jmhReportOutput = file("$buildDir/results/jmh").absolutePath
}

tasks {
  named("jmh") {
    finalizedBy(named("jmhReport"))

    outputs.cacheIf { false }
  }
}
