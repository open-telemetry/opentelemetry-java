import gradle.kotlin.dsl.accessors._33c50961cbf9e9e490400740be7b5d25.base

plugins {
  `maven-publish`
  signing

  id("otel.japicmp-conventions")
}

base {
  // May be set already by a parent project, only set if not.
  // TODO(anuraaga): Make this less hacky by creating a "module group" plugin.
  if (!project.name.startsWith("opentelemetry-")) {
    throw Error("archivesName of ${project.path} should start with opentelemetry-")
  }
}

// We must not change group ID or artifact ID here, otherwise composite builds won't work.
// Ensure that the project name matches the expected artifact ID.

publishing {
  publications {
    register<MavenPublication>("mavenPublication") {
      val release = findProperty("otel.release")
      if (release != null) {
        val versionParts = version.split('-').toMutableList()
        versionParts[0] += "-${release}"
        version = versionParts.joinToString("-")
      }
      afterEvaluate {
        // not available until evaluated.
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

afterEvaluate {
  val publishToSonatype by tasks.getting
  val release by rootProject.tasks.existing
  release.configure {
    finalizedBy(publishToSonatype)
  }
}

if (System.getenv("CI") != null) {
  signing {
    useInMemoryPgpKeys(System.getenv("GPG_PRIVATE_KEY"), System.getenv("GPG_PASSWORD"))
    sign(publishing.publications["mavenPublication"])
  }
}

tasks {
  register("updateVersionInDocs") {
    group = "documentation"
    doLast {
      val versionParts = version.toString().split('.')
      val minorVersionNumber = Integer.parseInt(versionParts[1])
      val nextSnapshot = "${versionParts[0]}.${minorVersionNumber + 1}.0-SNAPSHOT"

      val readme = file("README.md")
      if (readme.exists()) {
        val readmeText = readme.readText()
        val updatedText = readmeText
          .replace("""<version>\d+\.\d+\.\d+</version>""".toRegex(), "<version>${version}</version>")
          .replace("""<version>\d+\.\d+\.\d+-SNAPSHOT</version>""".toRegex(), "<version>${nextSnapshot}</version>")
          .replace("""(implementation.*io\.opentelemetry:.*:)(\d+\.\d+\.\d+)(?!-SNAPSHOT)(.*)""".toRegex(), "\$1${version}\$3")
          .replace("""(implementation.*io\.opentelemetry:.*:)(\d+\.\d+\.\d+-SNAPSHOT)(.*)""".toRegex(), "\$1${nextSnapshot}\$3")
          .replace("""<!--VERSION_STABLE-->.*<!--/VERSION_STABLE-->""".toRegex(), "<!--VERSION_STABLE-->${version}<!--/VERSION_STABLE-->")
          .replace("""<!--VERSION_UNSTABLE-->.*<!--/VERSION_UNSTABLE-->""".toRegex(), "<!--VERSION_UNSTABLE-->${version}-alpha<!--/VERSION_UNSTABLE-->")
        readme.writeText(updatedText)
      }
    }
  }
}
