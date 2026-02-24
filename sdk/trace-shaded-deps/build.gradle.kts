plugins {
  id("otel.java-conventions")

  id("com.gradleup.shadow")
}

// This project is not published, it is bundled into :sdk:trace

description = "Internal use only - shaded dependencies of OpenTelemetry SDK for Tracing"
otelJava.moduleName.set("io.opentelemetry.sdk.trace.internal")

dependencies {
  implementation("org.jctools:jctools-core")
}

tasks {
  shadowJar {
    minimize()

    exclude("META-INF/maven/**")
    relocate("org.jctools", "io.opentelemetry.internal.shaded.jctools")
  }

  register<Copy>("extractShadowJar") {
    dependsOn(shadowJar)
    from(zipTree(shadowJar.get().archiveFile))
    into("build/extracted/shadow")
  }
}

tasks.withType<Test>().configureEach {
  // JcToolsSecurityManagerTest interferes with JcToolsTest
  setForkEvery(1)
}
