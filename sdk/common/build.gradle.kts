plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.animalsniffer-conventions")
  id("org.unbroken-dome.test-sets")
}

description = "OpenTelemetry SDK Common"

otelJava.moduleName.set("io.opentelemetry.sdk.common")

val mrJarVersions = listOf(9)

testSets {
  create("testResourceDisabledByProperty")
  create("testResourceDisabledByEnv")
}

dependencies {
  api(project(":api:all"))

  implementation(project(":semconv"))

  annotationProcessor("com.google.auto.value:auto-value")

  testAnnotationProcessor("com.google.auto.value:auto-value")

  testImplementation(project(":sdk:testing"))
  testImplementation(project(":sdk-extensions:resources"))
  testImplementation("com.google.guava:guava-testlib")
}

for (version in mrJarVersions) {
  sourceSets { create("java${version}") { java { setSrcDirs(listOf("src/main/java${version}")) } } }

  tasks {
    named<JavaCompile>("compileJava${version}Java") {
      sourceCompatibility = "${version}"
      targetCompatibility = "${version}"
      options.release.set(version)
    }
  }

  configurations {
    named("java${version}Implementation") { extendsFrom(configurations["implementation"]) }
  }

  dependencies {
    // Common to reference classes in main sourceset from Java 9 one (e.g., to return a common
    // interface)
    add("java${version}Implementation", files(sourceSets.main.get().output.classesDirs))
  }
}

tasks {
  withType(Jar::class) {
    for (version in mrJarVersions) {
      into("META-INF/versions/${version}") { from(sourceSets["java${version}"].output) }
    }
    manifest.attributes("Multi-Release" to "true")
  }

  test {
    // For checking version number included in Resource.
    systemProperty("otel.test.project-version", project.version.toString())
  }
}
