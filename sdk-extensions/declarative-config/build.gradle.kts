import de.undercouch.gradle.tasks.download.Download

plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.animalsniffer-conventions")

  id("de.undercouch.download")
  id("org.jsonschema2pojo")
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
}

// The following tasks download the JSON Schema files from open-telemetry/opentelemetry-configuration,
// generate POJOs, and write the post-processed result into src/main/java for version control.
// The sequence of tasks is:
// 1. downloadConfigurationSchema - download configuration schema from open-telemetry/opentelemetry-configuration
// 2. unzipConfigurationSchema - unzip the configuration schema archive contents to $buildDir/configuration/
// 3. generateJsonSchema2Pojo - generate java POJOs from the configuration schema
// 4. syncPojoModelsToSrc - post-process the generated POJOs and write them to src/main/java
// The generated POJOs are committed to src/main/java and are NOT regenerated as part of the normal build.
// To regenerate (e.g. after a schema update), run: ./gradlew :sdk-extensions:declarative-config:syncPojoModelsToSrc

val configurationTag = "1.0.0"
val configurationRef = "refs/tags/v$configurationTag" // Replace with commit SHA to point to experiment with a specific commit
val configurationRepoZip = "https://github.com/open-telemetry/opentelemetry-configuration/archive/$configurationRef.zip"
val buildDirectory = layout.buildDirectory.asFile.get()

val downloadConfigurationSchema by tasks.registering(Download::class) {
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

val unzipConfigurationSchema by tasks.registering(Sync::class) {
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

jsonSchema2Pojo {
  sourceFiles = setOf(file("$buildDirectory/configuration/opentelemetry_configuration.json"))
  targetDirectory = file("$buildDirectory/generated/sources/js2p/java/main")
  targetPackage = "io.opentelemetry.sdk.autoconfigure.declarativeconfig.model"

  // Clear old source files to avoid contaminated source dir when updating
  removeOldOutput = true

  // Include @Nullable annotation. Note: jsonSchema2Pojo will not add @Nullable annotations on getters
  // so we add these in syncPojoModelsToSrc.
  includeJsr305Annotations = true

  // Prefer builders to setters
  includeSetters = false
  generateBuilders = true

  // Use title field to generate class name, instead of default which is based on filename / propertynames
  useTitleAsClassname = true

  // Force java 9+ @Generated annotation, since java 8 @Generated annotation isn't detected by
  // jsonSchema2Pojo and annotation is skipped altogether
  targetVersion = "1.9"

  // Append Model as suffix to the generated classes.
  classNameSuffix = "Model"

  // Initialize collection fields to null rather than empty collections so that absent YAML
  // properties deserialize as null (not present) rather than [] (explicitly empty). This lets
  // factories distinguish between "user omitted the field" and "user provided an empty list",
  // which is important for validations like IncludeExcludeFactory.
  initializeCollections = false
}

val generateJsonSchema2Pojo = tasks.getByName("generateJsonSchema2Pojo")
generateJsonSchema2Pojo.dependsOn(unzipConfigurationSchema)

val syncPojoModelsToSrc by tasks.registering(Copy::class) {
  dependsOn(generateJsonSchema2Pojo)
  finalizedBy("spotlessApply")
  val modelDir = File(projectDir, "src/main/java/io/opentelemetry/sdk/autoconfigure/declarativeconfig/model")
  doFirst {
    require(JavaVersion.current() == JavaVersion.VERSION_21) {
      "syncPojoModelsToSrc requires Java 21 (current: ${JavaVersion.current()}). jsonschema2pojo output is JVM-version-sensitive; using the wrong version produces spurious diffs."
    }
    // Copy won't remove files that no longer exist in the source. Delete first so schema type removals don't leave stale classes.
    modelDir.deleteRecursively()
  }

  from("$buildDirectory/generated/sources/js2p/java/main")
  into("$projectDir/src/main/java")
  filter {
    it
      // Shorten FQCNs for same-package references generated by jsonschema2pojo
      .replace("io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.", "")
      // Remove @Nullable annotation so it can be deterministically added later
      .replace("import javax.annotation.Nullable;\n", "")
      // Replace java 9+ @Generated annotation with java 8 version, add @Nullable annotation
      .replace("import javax.annotation.processing.Generated;", "import javax.annotation.Nullable;\nimport javax.annotation.Generated;")
      // Add @SuppressWarnings annotations for issues inherent in jsonschema2pojo-generated code:
      //   "rawtypes" - raw types used in builders
      //   "NullAway" - uninitialized @NonNull fields on Jackson-deserialized POJOs
      //     TODO(jack-berg): investigate jsonschema2pojo config to avoid @Nonnull on fields / generate initializing constructors
      //   "BoxedPrimitiveEquality" - == comparison of boxed primitives in generated equals()
      //     TODO(jack-berg): investigate jsonschema2pojo config for alternative equals implementation that avoids boxed primitives comparison
      .replace(
        "@Generated(\"jsonschema2pojo\")",
        "@Generated(\"jsonschema2pojo\")\n@SuppressWarnings({\"NullAway\", \"rawtypes\", \"BoxedPrimitiveEquality\"})"
      )
      // Add @Nullable annotations to all getters (except getAdditionalProperties which is non-null)
      .replace("( *)public (.+) get(?!AdditionalProperties)([a-zA-Z]*)".toRegex(), "$1@Nullable\n$1public $2 get$3")
  }
}

// Copies EnvironmentResource.java from the autoconfigure module into a generated source set so
// that declarative config can use the exact same source without taking a runtime dependency on
// autoconfigure and without the risk of divergence from manual syncing.
val generatedResourceConfigDir =
  layout.buildDirectory.dir("generated/sources/resource-configuration/java/main")
val copyResourceConfiguration by tasks.registering(Copy::class) {
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

afterEvaluate {
  // The jsonschema2pojo plugin auto-adds its targetDirectory to the main source set. Remove it so
  // that only the committed model POJOs in src/main/java are compiled, avoiding duplicate classes.
  val js2pDir = File(buildDirectory, "generated/sources/js2p/java/main")
  sourceSets {
    main {
      java {
        setSrcDirs(srcDirs.filter { it != js2pDir })
      }
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
