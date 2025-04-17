import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.jmh-conventions")
  id("org.jetbrains.kotlin.jvm")
  id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry Kotlin Extensions"
otelJava.moduleName.set("io.opentelemetry.extension.kotlin")

dependencies {
  implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

  api(project(":api:all"))

  compileOnly("org.jetbrains.kotlin:kotlin-stdlib-common")
  compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")

  testImplementation(project(":sdk:testing"))
  testImplementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
  testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
}

testing {
  suites {
    register<JvmTestSuite>("testStrictContext") {
      dependencies {
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
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
  withType(KotlinJvmCompile::class) {
    compilerOptions {
      jvmTarget.set(JvmTarget.JVM_1_8)
      apiVersion.set(KotlinVersion.KOTLIN_1_8)
    }
  }

  // We don't have any public Java classes
  named("javadoc") {
    enabled = false
  }

  check {
    dependsOn(testing.suites)
  }
}
