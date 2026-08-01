plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.jmh-conventions")
  id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry Context (Incubator)"
otelJava.moduleName.set("io.opentelemetry.context")

java {
  sourceSets {
    create("java25") {
      java {
        srcDir("src/main/java25")
      }
      // Make java25 source set depend on main source set
      // since VarHandleStringEncoder implements StringEncoder from the main source set
      compileClasspath += sourceSets.main.get().output + sourceSets.main.get().compileClasspath
    }
  }
}

// Configure java25 compilation to see main source classes
sourceSets.named("java25") {
  compileClasspath += sourceSets.main.get().output
}

tasks.named<JavaCompile>("compileJava25Java") {
  options.release.set(25)
  javaCompiler.set(javaToolchains.compilerFor {
    languageVersion.set(JavaLanguageVersion.of(25))
  })
}

tasks.named<Jar>("jar") {
  manifest {
    attributes["Multi-Release"] = "true"
  }
  from(sourceSets.named("java25").get().output) {
    into("META-INF/versions/25")
  }
}

// Configure test to include java25 classes when running on Java 9+
// so that StringEncoderHolder.createUnsafeEncoder() can instantiate the Java 9 version
val testJavaVersion = gradle.startParameter.projectProperties.get("testJavaVersion")?.let(JavaVersion::toVersion)
val effectiveJavaVersion = testJavaVersion ?: JavaVersion.current()
if (effectiveJavaVersion >= JavaVersion.VERSION_25) {
  sourceSets.named("test") {
    runtimeClasspath += sourceSets.named("java25").get().output
  }
}

dependencies {
  api(project(":common"))
  // MustBeClosed
  compileOnly("com.google.errorprone:error_prone_annotations")

  testImplementation("com.google.guava:guava")
}

testing {
  suites {
    register<JvmTestSuite>("grpcInOtelTest") {
      dependencies {
        implementation("io.grpc:grpc-api")
      }
    }

    register<JvmTestSuite>("otelInGrpcTest") {
      dependencies {
        implementation("io.grpc:grpc-api")
      }
    }

    register<JvmTestSuite>("braveInOtelTest") {
      dependencies {
        implementation("io.zipkin.brave:brave")
      }
    }

    register<JvmTestSuite>("otelInBraveTest") {
      dependencies {
        implementation("io.zipkin.brave:brave")
      }
    }

    register<JvmTestSuite>("otelAsBraveTest") {
      dependencies {
        implementation("io.zipkin.brave:brave")
      }
    }

    register<JvmTestSuite>("storageWrappersTest") {
    }

    register<JvmTestSuite>("strictContextEnabledTest") {
      dependencies {
        implementation(project(":api:all"))
      }

      targets {
        all {
          testTask.configure {
            jvmArgs("-Dio.opentelemetry.context.enableStrictContext=true")
          }
        }
      }
    }
  }
}

tasks {
  check {
    dependsOn(testing.suites)
  }
}
