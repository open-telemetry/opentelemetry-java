import com.google.auto.value.AutoValue
import com.google.common.io.Files
import japicmp.model.*
import me.champeau.gradle.japicmp.JapicmpTask
import me.champeau.gradle.japicmp.report.Violation
import me.champeau.gradle.japicmp.report.stdrules.*


plugins {
  base

  id("me.champeau.gradle.japicmp")
}

/**
 * The latest *released* version of the project. Evaluated lazily so the work is only done if necessary.
 */
val latestReleasedVersion: String by lazy {
  // hack to find the current released version of the project
  val temp: Configuration = configurations.create("tempConfig") {
    resolutionStrategy.cacheChangingModulesFor(0, "seconds")
    resolutionStrategy.cacheDynamicVersionsFor(0, "seconds")
  }
  // pick the api, since it's always there.
  dependencies.add(temp.name, "io.opentelemetry:opentelemetry-api:latest.release")
  val moduleVersion =
    configurations["tempConfig"].resolvedConfiguration.firstLevelModuleDependencies.elementAt(0).moduleVersion
  configurations.remove(temp)
  logger.debug("Discovered latest release version: " + moduleVersion)
  moduleVersion
}

class AllowNewAbstractMethodOnAutovalueClasses : AbstractRecordingSeenMembers() {
  override fun maybeAddViolation(member: JApiCompatibility): Violation? {
    val allowableAutovalueChanges = setOf(
      JApiCompatibilityChangeType.METHOD_ABSTRACT_ADDED_TO_CLASS,
      JApiCompatibilityChangeType.METHOD_ADDED_TO_PUBLIC_CLASS,
      JApiCompatibilityChangeType.ANNOTATION_ADDED
    )
    if (member.compatibilityChanges.filter { !allowableAutovalueChanges.contains(it.type) }
        .isEmpty() &&
      member is JApiMethod && isAutoValueClass(member.getjApiClass())
    ) {
      return Violation.accept(member, "Autovalue will automatically add implementation")
    }
    if (member.compatibilityChanges.isEmpty() &&
      member is JApiClass && isAutoValueClass(member)
    ) {
      return Violation.accept(member, "Autovalue class modification is allowed")
    }
    return null
  }

  fun isAutoValueClass(japiClass: JApiClass): Boolean {
    return japiClass.newClass.get().getAnnotation(AutoValue::class.java) != null ||
        japiClass.newClass.get().getAnnotation(AutoValue.Builder::class.java) != null
  }
}

class SourceIncompatibleRule : AbstractRecordingSeenMembers() {
  override fun maybeAddViolation(member: JApiCompatibility): Violation? {
    if (!member.isSourceCompatible()) {
      return Violation.error(member, "Not source compatible: $member")
    }
    return null
  }
}

fun getAllPublishedModules(): List<Project> {
  return project.rootProject.allprojects.filter {
    it.plugins.hasPlugin("otel.publish-conventions") && !it.hasProperty("otel.release") && it.tasks.findByName(
      "jar"
    ) != null
  }.toList()
}

fun getClasspathForVersion(version: String): List<File> {
  // Temporarily change the group name because we want to fetch an artifact with the same
  // Maven coordinates as the project, which Gradle would not allow otherwise.
  val existingGroup = group
  group = "virtual_group"
  try {
    return getAllPublishedModules().map {
      val depModule = "io.opentelemetry:${it.base.archivesName.get()}:$version@jar"
      val depJar = "${it.base.archivesName.get()}-$version.jar"
      val configuration: Configuration = configurations.detachedConfiguration(
        dependencies.create(depModule),
      )
      files(configuration.files).filter { file ->
        file.name.equals(depJar)
      }.singleFile
    }.toList()
  } finally {
    group = existingGroup
  }
}

fun getCurrentClassPath(): List<File> {
  return getAllPublishedModules().map {
    val archiveFile = it.tasks.getByName<Jar>("jar").archiveFile
    archiveFile.get().asFile
  }.toList()
}

// generate the api diff report for any module that is stable and publishes a jar.
if (!project.hasProperty("otel.release") && !project.name.startsWith("bom")) {
  afterEvaluate {
    tasks {
      val jApiCmp by registering(JapicmpTask::class) {
        // Depends on jar task for all published modules. See notes below.
        getAllPublishedModules().forEach {
          dependsOn(it.tasks.getByName("jar"))
        }

        // the japicmp "new" version is either the user-specified one, or the locally built jar.
        val apiNewVersion: String? by project
        // the japicmp "old" version is either the user-specified one, or the latest release.
        val apiBaseVersion: String? by project
        val baselineVersion = apiBaseVersion ?: latestReleasedVersion

        // Setup new and old classpath, new and old archives.
        // New and old classpaths are set to the set of all artifacts published by this project, at
        // the appropriate version. Without this, japicmp is unable to resolve inheritance across module
        // boundaries (i.e. Span from opentelemetry-api extends ImplicitContextKeyed from opentelemetry-context)
        // and generates false positive compatibility errors when methods are lifted into superclasses,
        // superinterfaces.
        val archiveName = base.archivesName.get()
        val newClassPath = apiNewVersion?.let { getClasspathForVersion(it) } ?: getCurrentClassPath()
        val oldClassPath = getClasspathForVersion(baselineVersion)
        val pattern = (archiveName + "-([0-9\\.]*)(-SNAPSHOT)?.jar").toRegex()
        val newArchive = newClassPath.singleOrNull { it.name.matches(pattern) }
        val oldArchive = oldClassPath.singleOrNull { it.name.matches(pattern) }
        newClasspath.from(newClassPath)
        oldClasspath.from(oldClassPath)
        newArchives.from(newArchive)
        oldArchives.from(oldArchive)

        // Only generate API diff for changes.
        onlyModified.set(true)

        // Reproduce defaults from https://github.com/melix/japicmp-gradle-plugin/blob/09f52739ef1fccda6b4310cf3f4b19dc97377024/src/main/java/me/champeau/gradle/japicmp/report/ViolationsGenerator.java#L130
        // with some changes.
        val exclusions = mutableListOf<String>()
        // Generics are not detected correctly
        exclusions.add("CLASS_GENERIC_TEMPLATE_CHANGED")
        // Allow new default methods on interfaces
        exclusions.add("METHOD_NEW_DEFAULT")
        // Allow adding default implementations for default methods
        exclusions.add("METHOD_ABSTRACT_NOW_DEFAULT")
        // Bug prevents recognizing default methods of superinterface.
        // Fixed in https://github.com/siom79/japicmp/pull/343 but not yet available in me.champeau.gradle.japicmp
        exclusions.add("METHOD_ABSTRACT_ADDED_IN_IMPLEMENTED_INTERFACE")
        compatibilityChangeExcludes.set(exclusions)
        richReport {
          addSetupRule(RecordSeenMembersSetup::class.java)
          addRule(JApiChangeStatus.NEW, SourceCompatibleRule::class.java)
          addRule(JApiChangeStatus.MODIFIED, SourceCompatibleRule::class.java)
          addRule(JApiChangeStatus.UNCHANGED, UnchangedMemberRule::class.java)
          // Allow new abstract methods on autovalue
          addRule(AllowNewAbstractMethodOnAutovalueClasses::class.java)
          addRule(BinaryIncompatibleRule::class.java)
          // Disallow source incompatible changes, which are allowed by default for some reason
          addRule(SourceIncompatibleRule::class.java)
        }

        // this is needed so that we only consider the current artifact, and not dependencies
        ignoreMissingClasses.set(true)
        packageExcludes.addAll(
          "*.internal",
          "*.internal.*",
          "io.opentelemetry.internal.shaded.jctools.*",
          // Temporarily suppress warnings from public generated classes from :sdk-extensions:jaeger-remote-sampler
          "io.opentelemetry.sdk.extension.trace.jaeger.proto.api_v2"
        )
        annotationExcludes.add("@kotlin.Metadata")
        val baseVersionString = if (apiBaseVersion == null) "latest" else baselineVersion
        txtOutputFile.set(
          apiNewVersion?.let { file("$rootDir/docs/apidiffs/${apiNewVersion}_vs_$baselineVersion/${base.archivesName.get()}.txt") }
            ?: file("$rootDir/docs/apidiffs/current_vs_$baseVersionString/${base.archivesName.get()}.txt"),
        )
      }
      // have the check task depend on the api comparison task, to make it more likely it will get used.
      named("check") {
        dependsOn(jApiCmp)
      }
    }
  }
}
