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

        withXml {
          // Since 5.0 okhttp uses gradle metadata to choose either okhttp-jvm or okhttp-android.
          // This does not work for maven builds that don't understand gradle metadata. They end up
          // using the okhttp artifact that is an empty jar. Here we replace usages of okhttp with
          // okhttp-jvm so that maven could get the actual okhttp dependency instead of the empty jar.
          var result = asString()
          var modified = result.toString().replace(">okhttp<", ">okhttp-jvm<")
          result.clear()
          result.append(modified)
        }

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
