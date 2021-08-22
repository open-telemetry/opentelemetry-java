import me.champeau.gradle.japicmp.JapicmpTask

plugins {
  base

  id("me.champeau.gradle.japicmp")
}

/**
 * The latest *released* version of the project. Evaluated lazily so the work is only done if necessary.
 */
val latestReleasedVersion: String by lazy {
  // hack to find the current released version of the project
  val temp: Configuration = configurations.create("tempConfig")
  // pick the api, since it's always there.
  dependencies.add(temp.name, "io.opentelemetry:opentelemetry-api:latest.release")
  val moduleVersion = configurations["tempConfig"].resolvedConfiguration.firstLevelModuleDependencies.elementAt(0).moduleVersion
  configurations.remove(temp)
  logger.debug("Discovered latest release version: " + moduleVersion)
  moduleVersion
}

/**
 * Locate the project's artifact of a particular version.
 */
fun findArtifact(version: String): File {
  val existingGroup = group
  try {
    // Temporarily change the group name because we want to fetch an artifact with the same
    // Maven coordinates as the project, which Gradle would not allow otherwise.
    group = "virtual_group"
    val depModule = "io.opentelemetry:${base.archivesName.get()}:$version@jar"
    val depJar = "${base.archivesName.get()}-${version}.jar"
    val configuration: Configuration = configurations.detachedConfiguration(
      dependencies.create(depModule)
    )
    return files(configuration.files).filter {
      it.name.equals(depJar)
    }.singleFile
  } finally {
    group = existingGroup
  }
}

// generate the api diff report for any module that is stable and publishes a jar.
if (!project.hasProperty("otel.release") && !project.name.startsWith("bom")) {
  afterEvaluate {
    tasks {
      val jApiCmp by registering(JapicmpTask::class) {
        dependsOn("jar")

        // the japicmp "new" version is either the user-specified one, or the locally built jar.
        val apiNewVersion: String? by project
        val newArtifact = apiNewVersion?.let { findArtifact(it) }
          ?: file(getByName<Jar>("jar").archiveFile)
        newClasspath = files(newArtifact)

        //only output changes, not everything
        isOnlyModified = true

        // the japicmp "old" version is either the user-specified one, or the latest release.
        val apiBaseVersion: String? by project
        val baselineVersion = apiBaseVersion ?: latestReleasedVersion
        oldClasspath = try {
          files(findArtifact(baselineVersion))
        } catch (e: Exception) {
          //if we can't find the baseline artifact, this is probably one that's never been published before,
          //so publish the whole API. We do that by flipping this flag, and comparing the current against nothing.
          isOnlyModified = false
          files()
        }

        //this is needed so that we only consider the current artifact, and not dependencies
        isIgnoreMissingClasses = true
        // double wildcards don't seem to work here (*.internal.*)
        packageExcludes = listOf("*.internal", "io.opentelemetry.internal.shaded.jctools.*")
        val baseVersionString = if (apiBaseVersion == null) "latest" else baselineVersion
        txtOutputFile = apiNewVersion?.let { file("$rootDir/docs/apidiffs/${apiNewVersion}_vs_${baselineVersion}/${base.archivesName.get()}.txt") }
          ?: file("$rootDir/docs/apidiffs/current_vs_${baseVersionString}/${base.archivesName.get()}.txt")
      }
      // have the check task depend on the api comparison task, to make it more likely it will get used.
      named("check") {
        dependsOn(jApiCmp)
      }
    }
  }
}
