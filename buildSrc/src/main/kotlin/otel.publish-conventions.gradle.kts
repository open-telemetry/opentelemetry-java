plugins {
  `maven-publish`
  signing

  id("otel.japicmp-conventions")
}

publishing {
  publications {
    register<MavenPublication>("mavenPublication") {
      groupId = "io.opentelemetry"
      afterEvaluate {
        // not available until evaluated.
        artifactId = base.archivesName.get()
        pom.description.set(project.description)
      }

      plugins.withId("java-platform") {
        from(components["javaPlatform"])
      }
      plugins.withId("java-library") {
        from(components["java"])
      }

      versionMapping {
        allVariants {
          fromResolutionResult()
        }
      }

      pom {
        name.set("OpenTelemetry Java")
        url.set("https://github.com/open-telemetry/opentelemetry-java")

        licenses {
          license {
            name.set("The Apache License, Version 2.0")
            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
          }
        }

        developers {
          developer {
            id.set("opentelemetry")
            name.set("OpenTelemetry")
            url.set("https://github.com/open-telemetry/community")
          }
        }

        scm {
          connection.set("scm:git:git@github.com:open-telemetry/opentelemetry-java.git")
          developerConnection.set("scm:git:git@github.com:open-telemetry/opentelemetry-java.git")
          url.set("git@github.com:open-telemetry/opentelemetry-java.git")
        }
      }
    }
  }
}

if (System.getenv("CI") != null) {
  signing {
    useInMemoryPgpKeys(System.getenv("GPG_PRIVATE_KEY"), System.getenv("GPG_PASSWORD"))
    sign(publishing.publications["mavenPublication"])
  }
}
