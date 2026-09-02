plugins {
  id("otel.java-conventions")
  // id("otel.publish-conventions")

  // id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry - ProcessContext SDK"
otelJava.moduleName.set("sdk.processcontext")

dependencies {
  api(project(":sdk:common"))
  api(project(":exporters:common")) // MessageWriter

  annotationProcessor("com.google.auto.value:auto-value")

  implementation(project(":exporters:otlp:common"))

  testImplementation(project(":sdk:testing"))
  testImplementation("io.opentelemetry.proto:opentelemetry-proto")
  testImplementation("com.google.protobuf:protobuf-java-util")
}

java {
  sourceSets {
    create("Java25") {
      java {
        srcDir("src/main/java25")
      }
      compileClasspath += sourceSets.main.get().output + sourceSets.main.get().compileClasspath
    }
  }
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(25))
  }
}

tasks.named<JavaCompile>("compileJava") {
  options.release.set(8)
}

tasks.named<JavaCompile>("compileJava25Java") {
  options.release.set(25)
}

testing {
  sourceSets {
    create("Java25Test") {
      java {
        srcDir("src/test/java25")
      }
      compileClasspath += sourceSets.test.get().output + sourceSets.test.get().compileClasspath
      compileClasspath += sourceSets.main.get().output + sourceSets["Java25"].output
    }
  }
}

tasks.named<JavaCompile>("compileJava") {
  options.release.set(8)
}

tasks.named<JavaCompile>("compileJava25TestJava") {
  options.release.set(25)
}

// only test on java 25+
val testJavaVersion = project.findProperty("testJavaVersion") as String?
tasks.test {
  enabled = (testJavaVersion != null && Integer.valueOf(testJavaVersion) >= 25)
}

tasks.named<Jar>("jar") {
  manifest {
    attributes["Multi-Release"] = "true"
  }
  from(sourceSets.named("Java25").get().output) {
    into("META-INF/versions/25")
  }
}
