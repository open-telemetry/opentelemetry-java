import nebula.plugin.release.git.opinion.Strategies
import java.time.Duration

plugins {
  id("com.github.ben-manes.versions")
  id("io.github.gradle-nexus.publish-plugin")
  id("nebula.release")

  id("otel.spotless-conventions")
}

if (!JavaVersion.current().isJava11Compatible()) {
  throw GradleException(
    "JDK 11 or higher is required to build. " +
      "One option is to download it from https://adoptopenjdk.net/. If you believe you already " +
      "have it, please check that the JAVA_HOME environment variable is pointing at the " +
      "JDK 11 installation."
  )
}

// Nebula plugin will not configure if .git doesn't exist, let's allow building on it by stubbing it
// out. This supports building from the zip archive downloaded from GitHub.
var releaseTask: TaskProvider<Task>
if (file(".git").exists()) {
  release {
    defaultVersionStrategy = Strategies.getSNAPSHOT()
  }

  nebulaRelease {
    addReleaseBranchPattern("""v\d+\.\d+\.x""")
  }

  releaseTask = tasks.named("release")
  releaseTask.configure {
    mustRunAfter("snapshotSetup", "finalSetup")
  }
} else {
  releaseTask = tasks.register("release")
}

nexusPublishing {
  packageGroup.set("io.opentelemetry")

  repositories {
    sonatype {
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
