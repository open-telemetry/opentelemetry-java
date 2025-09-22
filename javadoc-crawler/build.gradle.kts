plugins {
  id("otel.java-conventions")
}

dependencies {
  implementation("com.fasterxml.jackson.core:jackson-databind")
  testImplementation("org.assertj:assertj-core:3.27.6")
}

description = "OpenTelemetry Javadoc Crawler"
otelJava.moduleName.set("io.opentelemetry.javadocs")

tasks {
  withType<JavaCompile>().configureEach {
    sourceCompatibility = "17"
    targetCompatibility = "17"
    options.release.set(17)
  }

  // only test on java 17+
  val testJavaVersion: String? by project
  if (testJavaVersion != null && Integer.valueOf(testJavaVersion) < 17) {
    test {
      enabled = false
    }
  }

  val crawl by registering(JavaExec::class) {
    dependsOn(classes)

    mainClass.set("io.opentelemetry.javadocs.JavaDocsCrawler")
    classpath(sourceSets["main"].runtimeClasspath)
  }
}
