plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry Exporter Common"
otelJava.moduleName.set("io.opentelemetry.exporter.internal")

java {
  sourceSets {
    create("java9") {
      java {
        srcDir("src/main/java9")
      }
      // Make java9 source set depend on main source set
      // since VarHandleStringEncoder implements StringEncoder from the main source set
      compileClasspath += sourceSets.main.get().output + sourceSets.main.get().compileClasspath
    }
  }
}

// Configure java9 compilation to see main source classes
sourceSets.named("java9") {
  compileClasspath += sourceSets.main.get().output
}

tasks.named<JavaCompile>("compileJava9Java") {
  options.release.set(9)
}

tasks.named<Jar>("jar") {
  manifest {
    attributes["Multi-Release"] = "true"
  }
  from(sourceSets.named("java9").get().output) {
    into("META-INF/versions/9")
  }
}

// Configure test to include java9 classes when running on Java 9+
// so that StringEncoderHolder.createUnsafeEncoder() can instantiate the Java 9 version
val javaVersion = JavaVersion.current()
if (javaVersion >= JavaVersion.VERSION_1_9) {
  sourceSets.named("test") {
    runtimeClasspath += sourceSets.named("java9").get().output
  }
}

val versions: Map<String, String> by project
dependencies {
  api(project(":api:all"))
  api(project(":sdk-extensions:autoconfigure-spi"))

  compileOnly(project(":api:incubator"))
  compileOnly(project(":sdk:common"))
  compileOnly(project(":exporters:common:compile-stub"))

  compileOnly("org.codehaus.mojo:animal-sniffer-annotations")

  annotationProcessor("com.google.auto.value:auto-value")

  // We include helpers shared by gRPC exporters but do not want to impose these
  // dependency on all of our consumers.
  compileOnly("com.fasterxml.jackson.core:jackson-core")
  // sun.misc.Unsafe from the JDK isn't found by the compiler, we provide our own trimmed down
  // version that we can compile against.
  compileOnly("io.grpc:grpc-stub")

  testImplementation(project(":sdk:common"))
  testImplementation(project(":sdk:testing"))

  testImplementation("com.google.protobuf:protobuf-java-util")
  testImplementation("com.linecorp.armeria:armeria-junit5")
  testImplementation("org.skyscreamer:jsonassert")
  testImplementation("com.google.api.grpc:proto-google-common-protos")
  testImplementation("io.grpc:grpc-testing")
  testImplementation("edu.berkeley.cs.jqf:jqf-fuzz")
  testRuntimeOnly("io.grpc:grpc-netty-shaded")
}

val testJavaVersion: String? by project

testing {
  suites {
    register<JvmTestSuite>("testHttpSenderProvider") {
      dependencies {
        implementation(project(":exporters:sender:jdk"))
        implementation(project(":exporters:sender:okhttp"))
      }
      targets {
        all {
          testTask {
            enabled = !testJavaVersion.equals("8")
          }
        }
      }
    }
  }
  suites {
    register<JvmTestSuite>("testGrpcSenderProvider") {
      dependencies {
        implementation(project(":exporters:sender:okhttp"))
        implementation(project(":exporters:sender:grpc-managed-channel"))

        implementation("io.grpc:grpc-stub")
        implementation("io.grpc:grpc-netty")
        implementation("com.fasterxml.jackson.core:jackson-core")
      }
    }
  }
  suites {
    register<JvmTestSuite>("testWithoutUnsafe") {}
  }
}

tasks {
  check {
    dependsOn(testing.suites)
  }

  withType<Test> {
    // Allow VarHandle access to String internals
    // generally users won't do this and so won't get the VarHandle implementation
    // but the Java agent is able to automatically open these modules
    // (see ModuleOpener.java in that repository)
    jvmArgs("--add-opens=java.base/java.lang=ALL-UNNAMED")
    jvmArgs("-XX:+IgnoreUnrecognizedVMOptions") // needed for Java 8
  }
}

afterEvaluate {
  tasks.named<JavaCompile>("compileTestHttpSenderProviderJava") {
    options.release.set(11)
  }
}
