import org.gradle.api.JavaVersion

plugins {
  id("otel.java-conventions")
}

dependencies {
  implementation("com.fasterxml.jackson.core:jackson-databind")
  testImplementation("org.assertj:assertj-core:3.27.6")
}

description = "OpenTelemetry Javadoc Crawler"
otelJava.moduleName.set("io.opentelemetry.javadocs")
otelJava.minJavaVersionSupported.set(JavaVersion.VERSION_17)

tasks {
  val crawl by registering(JavaExec::class) {
    dependsOn(classes)

    mainClass.set("io.opentelemetry.javadocs.JavaDocsCrawler")
    classpath(sourceSets["main"].runtimeClasspath)
  }
}
