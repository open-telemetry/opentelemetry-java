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
    create("java25") {
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

testing {
  sourceSets {
    create("java25test") {
      java {
        srcDir("src/test/java25")
      }
      compileClasspath += sourceSets.test.get().output + sourceSets.test.get().compileClasspath
      compileClasspath += sourceSets.main.get().output + sourceSets["java25"].output
    }
  }
}

tasks.withType<JavaCompile> {
  options.release.set(25)
}

tasks.named<Jar>("jar") {
  manifest {
    attributes["Multi-Release"] = "true"
  }
  from(sourceSets.named("java25").get().output) {
    into("META-INF/versions/25")
  }
}
