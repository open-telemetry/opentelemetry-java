import de.undercouch.gradle.tasks.download.Download
import io.opentelemetry.gradle.DeclarativeConfigPojoGenerator

plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.animalsniffer-conventions")

  id("de.undercouch.download")
}

description = "OpenTelemetry SDK Declarative Config"
otelJava.moduleName.set("io.opentelemetry.sdk.autoconfigure.declarativeconfig")
otelJava.osgiOptionalPackages.set(listOf("io.opentelemetry.sdk.autoconfigure.spi"))
otelJava.osgiServiceLoaderProvides.set(listOf(
  "io.opentelemetry.sdk.autoconfigure.spi.internal.ComponentProvider",
))
// declarative-config discovers customizer providers at runtime via ServiceLoader
otelJava.osgiServiceLoaderRequires.set(listOf(
  "io.opentelemetry.sdk.autoconfigure.declarativeconfig.DeclarativeConfigurationCustomizerProvider",
))

dependencies {
  api(project(":sdk:all"))
  api(project(":api:incubator"))
  compileOnly(project(":sdk-extensions:autoconfigure-spi"))
  // Needed for composable samplers
  implementation(project(":sdk-extensions:incubator"))

  annotationProcessor("com.google.auto.value:auto-value")

  api("com.fasterxml.jackson.core:jackson-annotations")
  implementation("org.snakeyaml:snakeyaml-engine")
  implementation("com.fasterxml.jackson.core:jackson-databind")

  testImplementation(project(":sdk:testing"))
  testImplementation(project(":sdk-extensions:autoconfigure-spi"))
  testImplementation(project(":exporters:logging"))
  testImplementation(project(":exporters:logging-otlp"))
  testImplementation(project(":exporters:otlp:all"))
  testImplementation(project(":exporters:prometheus"))
  testImplementation(project(":sdk-extensions:jaeger-remote-sampler"))
  testImplementation(project(":extensions:trace-propagators"))
  testImplementation("io.opentelemetry.contrib:opentelemetry-aws-xray-propagator")
  testImplementation("io.opentelemetry.instrumentation:opentelemetry-resources")
  testImplementation("com.linecorp.armeria:armeria-junit5")
//
  testImplementation("com.google.guava:guava-testlib")
  testImplementation("nl.jqno.equalsverifier:equalsverifier")
}

// The following tasks download the JSON Schema files from open-telemetry/opentelemetry-configuration
// and regenerate the committed model POJOs in src/main/java. The sequence is:
// 1. downloadConfigurationSchema - download configuration schema from open-telemetry/opentelemetry-configuration
// 2. unzipConfigurationSchema - unzip the schema archive to $buildDir/configuration/
// 3. syncPojoModelsToSrc - run PojoGenerator to write Java sources into src/main/java
// The generated POJOs are committed to src/main/java and are NOT regenerated as part of the normal build.
// To regenerate (e.g. after a schema update), run: ./gradlew :sdk-extensions:declarative-config:syncPojoModelsToSrc

val configurationTag = "1.1.0"
val configurationRef = "refs/tags/v$configurationTag" // Replace with commit SHA to point to experiment with a specific commit
val configurationRepoZip = "https://github.com/open-telemetry/opentelemetry-configuration/archive/$configurationRef.zip"
val buildDirectory = layout.buildDirectory.asFile.get()

val downloadConfigurationSchema = tasks.register<Download>("downloadConfigurationSchema") {
  src(configurationRepoZip)
  // The version is encoded in the filename so that a configurationTag change results in a new
  // path that doesn't yet exist, triggering a fresh download. On subsequent builds with the same
  // tag the file already exists and overwrite(false) skips the network request. Note: the
  // de.undercouch Download task always reports itself as not up-to-date, so overwrite(false) is
  // the intended mechanism for avoiding redundant downloads.
  //
  // The zip is stored in tmp/ so it is outside the Sync task's output directory (configuration/).
  dest("$buildDirectory/tmp/opentelemetry-configuration-v$configurationTag.zip")
  overwrite(false)
}

val unzipConfigurationSchema = tasks.register<Sync>("unzipConfigurationSchema") {
  // Sync (not Copy) removes stale files from the destination when the source changes, ensuring
  // files deleted or renamed between schema versions don't linger in the build dir.
  dependsOn(downloadConfigurationSchema)

  from(zipTree(downloadConfigurationSchema.get().dest))
  eachFile(closureOf<FileCopyDetails> {
    // Remove the top level folder "/opentelemetry-configuration-$configurationRef"
    val pathParts = path.split("/")
    path = pathParts.subList(1, pathParts.size).joinToString("/")
  })
  into("$buildDirectory/configuration/")
  includeEmptyDirs = false
}

// Generates POJOs from the configuration schema and writes them to src/main/java.
// The generated POJOs are committed to src/main/java and are NOT regenerated as part of the normal
// build. To regenerate (e.g. after a schema update), run:
//   ./gradlew :sdk-extensions:declarative-config:syncPojoModelsToSrc
val syncPojoModelsToSrc = tasks.register("syncPojoModelsToSrc") {
  // This task is only run manually (to regenerate committed model POJOs after a schema update).
  // It writes into src/main/java, which is outside the normal Gradle output boundary, so it
  // intentionally doesn't participate in the configuration cache.
  notCompatibleWithConfigurationCache("Regenerates committed model POJOs in src/main/java")
  dependsOn(unzipConfigurationSchema)
  finalizedBy("spotlessApply")
  val schemaFile = File("$buildDirectory/configuration/opentelemetry_configuration.json")
  val modelPackage = "io.opentelemetry.sdk.autoconfigure.declarativeconfig.model"
  val modelSrcDir = File(projectDir, "src/main/java")
  doLast {
    val modelDir = File(modelSrcDir, modelPackage.replace('.', '/'))
    // Delete only @Generated files so hand-written files (ModelMapper, ExtensionPropertyUtil)
    // in model.internal survive the regeneration cycle.
    modelDir.walkTopDown()
      .filter { it.isFile && it.extension == "java" }
      .filter { it.readText().contains("@Generated(") }
      .forEach { it.delete() }
    DeclarativeConfigPojoGenerator(schemaFile, modelSrcDir, modelPackage).generate()
  }
}

// Copies EnvironmentResource.java from the autoconfigure module into a generated source set so
// that declarative config can use the exact same source without taking a runtime dependency on
// autoconfigure and without the risk of divergence from manual syncing.
val generatedResourceConfigDir =
  layout.buildDirectory.dir("generated/sources/resource-configuration/java/main")
val copyResourceConfiguration = tasks.register<Copy>("copyResourceConfiguration") {
  from(
    project(":sdk-extensions:autoconfigure").file(
      "src/main/java/io/opentelemetry/sdk/autoconfigure/EnvironmentResource.java"
    )
  )
  into(generatedResourceConfigDir.map { it.dir("io/opentelemetry/sdk/autoconfigure/declarativeconfig") })
  // Only the package declaration needs rewriting; everything else is copied verbatim.
  filter { line: String ->
    line.replace(
      "package io.opentelemetry.sdk.autoconfigure;",
      "package io.opentelemetry.sdk.autoconfigure.declarativeconfig;"
    )
  }
}

sourceSets {
  main {
    java {
      srcDir(generatedResourceConfigDir)
    }
  }
}

val buildGraalVmReflectionJson = tasks.register("buildGraalVmReflectionJson") {
  val buildDir = buildDirectory
  val targetFile = File(
    buildDir,
    "resources/main/META-INF/native-image/io.opentelemetry/io.opentelemetry.sdk.autoconfigure.declarativeconfig/reflect-config.json"
  )
  val sourcePackage =
    "io.opentelemetry.sdk.autoconfigure.declarativeconfig.model"
  val sourcePackagePath = sourcePackage.replace(".", "/")
  val classesDir =
    File(
      buildDir,
      "classes/java/main/$sourcePackagePath"
    )

  inputs.dir(classesDir)
  outputs.file(targetFile)

  onlyIf { !targetFile.exists() }

  dependsOn("compileJava")

  doLast {
    println("Generating GraalVM reflection config at: ${targetFile.absolutePath}")

    val classes = mutableListOf<String>()
    classesDir.walkTopDown().filter { it.isFile && it.extension == "class" }.forEach { file ->
      val relativePath = file.toRelativeString(classesDir)
      val className = relativePath
        .removeSuffix(".class")
        .replace(File.separatorChar, '.')
      classes.add("$sourcePackage.$className")
    }
    classes.sort()

    targetFile.parentFile.mkdirs()
    targetFile.bufferedWriter().use { writer ->
      writer.write("[\n")
      classes.forEachIndexed { index, className ->
        writer.write("  {\n")
        writer.write("    \"name\": \"$className\",\n")
        writer.write("    \"allDeclaredMethods\": true,\n")
        writer.write("    \"allDeclaredFields\": true,\n")
        writer.write("    \"allDeclaredConstructors\": true\n")
        writer.write("  }")
        if (index < classes.size - 1) {
          writer.write(",\n")
        } else {
          writer.write("\n")
        }
      }
      writer.write("]\n")
    }
  }
}

tasks.getByName("compileJava").dependsOn(copyResourceConfiguration)
tasks.getByName("sourcesJar").dependsOn(buildGraalVmReflectionJson, copyResourceConfiguration)
tasks.getByName("jar").dependsOn(buildGraalVmReflectionJson)
tasks.getByName("javadoc").dependsOn(buildGraalVmReflectionJson)
tasks.getByName("compileTestJava").dependsOn(buildGraalVmReflectionJson)

// When syncPojoModelsToSrc runs it writes to src/main/java. Both spotlessJava and spotlessMisc
// declare the module directory as an input region, so Gradle requires ordering to be explicit.
// mustRunAfter satisfies the implicit-dependency validator without making spotless depend on
// generation during normal builds.
tasks.named("spotlessJava") { mustRunAfter(syncPojoModelsToSrc) }
tasks.named("spotlessMisc") { mustRunAfter(syncPojoModelsToSrc) }

// Exclude committed generated POJO sources from checkstyle
tasks.named<Checkstyle>("checkstyleMain") {
  dependsOn(buildGraalVmReflectionJson)
  exclude("**/declarativeconfig/model/**")
}

tasks {
  withType<Test>().configureEach {
    dependsOn(unzipConfigurationSchema)
    environment(
      mapOf(
        // Expose the kitchen sink example file to tests
        "CONFIG_REPO_ROOT" to "$buildDirectory/configuration"
      )
    )
  }
}
