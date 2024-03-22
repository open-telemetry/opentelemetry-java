import java.util.*

plugins {
  `maven-publish`
  signing

  id("otel.japicmp-conventions")
  id("org.spdx.sbom")
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

      if (!project.name.startsWith("bom")) {
        afterEvaluate {
          artifact("${layout.buildDirectory.get()}/spdx/${"opentelemetry-java_" + base.archivesName.get()}.spdx.json") {
            classifier = "spdx"
            extension = "spdx.json"
          }
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

if (!project.name.startsWith("bom")) {
  project.afterEvaluate {
    spdxSbom {
      targets {
        val sbomName = "opentelemetry-java_" + base.archivesName.get()
        // Create a target to match the published jar name.
        // This is used for the task name (spdxSbomFor<SbomName>)
        // and output file (<sbomName>.spdx.json).
        create(sbomName) {
          scm {
            uri.set("https://github.com/" + System.getenv("GITHUB_REPOSITORY"))
            revision.set(System.getenv("GITHUB_SHA"))
          }
          document {
            name.set(sbomName)
            namespace.set("https://opentelemetry.io/spdx/" + UUID.randomUUID())
          }
        }
      }
    }
    tasks.named("assemble") {
      dependsOn("spdxSbom")
    }
  }
}
