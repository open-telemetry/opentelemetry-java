plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry SDK Resource Providers"

otelJava.moduleName.set("io.opentelemetry.sdk.extension.resources")

val mrJarVersions = listOf(11)

dependencies {
  api(project(":sdk:common"))

  implementation(project(":semconv"))

  compileOnly(project(":sdk-extensions:autoconfigure"))

  compileOnly("org.codehaus.mojo:animal-sniffer-annotations")

  testImplementation("org.junit-pioneer:junit-pioneer")
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
    named("java${version}CompileOnly") { extendsFrom(configurations["compileOnly"]) }
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
}
