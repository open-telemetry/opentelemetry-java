import java.time.Duration

plugins {
  id("io.github.gradle-nexus.publish-plugin")

  id("otel.spotless-conventions")
}

apply(from = "version.gradle.kts")

nexusPublishing {
  packageGroup.set("io.opentelemetry")

  repositories {
    // see https://central.sonatype.org/publish/publish-portal-ossrh-staging-api/#configuration
    sonatype {
      nexusUrl.set(uri("https://ossrh-staging-api.central.sonatype.com/service/local/"))
      snapshotRepositoryUrl.set(uri("https://central.sonatype.com/repository/maven-snapshots/"))
      username.set(System.getenv("SONATYPE_USER"))
      password.set(System.getenv("SONATYPE_KEY"))
    }
  }

  connectTimeout.set(Duration.ofMinutes(5))
  clientTimeout.set(Duration.ofMinutes(5))

  transitionCheckOptions {
    // We have many artifacts so Maven Central takes a long time on its compliance checks. This sets
    // the timeout for waiting for the repository to close to a comfortable 50 minutes.
    maxRetries.set(300)
    delayBetween.set(Duration.ofSeconds(10))
  }
}

// The BOM projects register dependent tasks that actually do the generating.
tasks.register("generateBuildSubstitutions") {
  group = "publishing"
  description = "Generate a code snippet that can be copy-pasted for use in composite builds."
}

subprojects {
  group = "io.opentelemetry"
}

tasks {
  register("updateVersionInDocs") {
    group = "documentation"
    val version = findProperty("release.version")
    val readme = file("README.md")
    doLast {
      val versionParts = version.toString().split('.')
      val minorVersionNumber = Integer.parseInt(versionParts[1])
      val nextSnapshot = "${versionParts[0]}.${minorVersionNumber + 1}.0-SNAPSHOT"

      if (readme.exists()) {
        val readmeText = readme.readText()
        val updatedText = readmeText
          .replace("""<version>\d+\.\d+\.\d+</version>""".toRegex(), "<version>$version</version>")
          .replace("""<version>\d+\.\d+\.\d+-SNAPSHOT</version>""".toRegex(), "<version>$nextSnapshot</version>")
          .replace("""(implementation.*io\.opentelemetry:.*:)(\d+\.\d+\.\d+)(?!-SNAPSHOT)(.*)""".toRegex(), "\$1${version}\$3")
          .replace("""(implementation.*io\.opentelemetry:.*:)(\d+\.\d+\.\d+-SNAPSHOT)(.*)""".toRegex(), "\$1${nextSnapshot}\$3")
          .replace("""<!--VERSION_STABLE-->.*<!--/VERSION_STABLE-->""".toRegex(), "<!--VERSION_STABLE-->$version<!--/VERSION_STABLE-->")
          .replace("""<!--VERSION_UNSTABLE-->.*<!--/VERSION_UNSTABLE-->""".toRegex(), "<!--VERSION_UNSTABLE-->$version-alpha<!--/VERSION_UNSTABLE-->")
        readme.writeText(updatedText)
      }
    }
  }
}
