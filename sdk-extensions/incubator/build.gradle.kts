import de.undercouch.gradle.tasks.download.Download

plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.animalsniffer-conventions")

  id("de.undercouch.download")
  id("org.jsonschema2pojo")
}

// SDK modules that are still being developed.

description = "OpenTelemetry SDK Incubator"
otelJava.moduleName.set("io.opentelemetry.sdk.extension.incubator")

dependencies {
  api(project(":sdk:all"))

  annotationProcessor("com.google.auto.value:auto-value")

  // io.opentelemetry.sdk.extension.incubator.metric.viewconfig
  implementation(project(":sdk-extensions:autoconfigure-spi"))
  implementation("org.snakeyaml:snakeyaml-engine")

  // io.opentelemetry.sdk.extension.incubator.fileconfig
  implementation("com.fasterxml.jackson.core:jackson-databind")
  implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")
  implementation("org.yaml:snakeyaml:1.31")

  testImplementation(project(":sdk:testing"))
  testImplementation(project(":sdk-extensions:autoconfigure"))

  testImplementation("com.google.guava:guava-testlib")
}

// The following tasks download the JSON Schema files from open-telemetry/opentelemetry-configuration and generate classes from the type definitions which are used with jackson-databind to parse JSON / YAML to the configuration schema.
// The sequence of tasks is:
// 1. downloadConfigurationSchema - download configuration schema from open-telemetry/opentelemetry-configuration
// 2. unzipConfigurationSchema - unzip the configuration schema archive contents to $buildDir/configuration/
// 3. generateJsonSchema2Pojo - generate java POJOs from the configuration schema
// 4. jsonSchema2PojoPostProcessing - perform various post processing on the generated POJOs, e.g. replace javax.annotation.processing.Generated with javax.annotation.Generated, add @SuppressWarning("rawtypes") annotation
// 5. overwriteJs2p - overwrite original generated classes with versions containing updated @Generated annotation
// 6. deleteJs2pTmp - delete tmp directory
// ... proceed with normal sourcesJar, compileJava, etc

// TODO(jack-berg): update ref to be released version when available
val configurationRef = "2107dbb6f2a6c99fe2f55d550796ee7e2286fd1d"
val configurationRepoZip = "https://github.com/open-telemetry/opentelemetry-configuration/archive/$configurationRef.zip"

val downloadConfigurationSchema by tasks.registering(Download::class) {
  src(configurationRepoZip)
  dest("$buildDir/configuration/opentelemetry-configuration.zip")
  overwrite(false)
}

val unzipConfigurationSchema by tasks.registering(Copy::class) {
  dependsOn(downloadConfigurationSchema)

  from(zipTree(downloadConfigurationSchema.get().dest))
  eachFile(closureOf<FileCopyDetails> {
    // Remove the top level folder "/opentelemetry-configuration-$configurationRef"
    val pathParts = path.split("/")
    path = pathParts.subList(1, pathParts.size).joinToString("/")
  })
  into("$buildDir/configuration/")
}

jsonSchema2Pojo {
  sourceFiles = setOf(file("$buildDir/configuration/schema"))
  targetDirectory = file("$buildDir/generated/sources/js2p/java/main")
  targetPackage = "io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model"

  // Clear old source files to avoid contaminated source dir when updating
  removeOldOutput = true

  // Prefer builders to setters
  includeSetters = false
  generateBuilders = true

  // Use title field to generate class name, instead of default which is based on filename / propertynames
  useTitleAsClassname = true

  // Force java 9+ @Generated annotation, since java 8 @Generated annotation isn't detected by
  // jsonSchema2Pojo and annotation is skipped altogether
  targetVersion = "1.9"
}

val generateJsonSchema2Pojo = tasks.getByName("generateJsonSchema2Pojo")
generateJsonSchema2Pojo.dependsOn(unzipConfigurationSchema)

val jsonSchema2PojoPostProcessing by tasks.registering(Copy::class) {
  dependsOn(generateJsonSchema2Pojo)

  from("$buildDir/generated/sources/js2p")
  into("$buildDir/generated/sources/js2p-tmp")
  filter {
    it
      // Replace java 9+ @Generated annotation with java 8 version
      .replace("import javax.annotation.processing.Generated", "import javax.annotation.Generated")
      // Add @SuppressWarnings("rawtypes") annotation to address raw types used in jsonschema2pojo builders
      .replace("@Generated(\"jsonschema2pojo\")", "@Generated(\"jsonschema2pojo\")\n@SuppressWarnings(\"rawtypes\")")
  }
}
val overwriteJs2p by tasks.registering(Copy::class) {
  dependsOn(jsonSchema2PojoPostProcessing)

  from("$buildDir/generated/sources/js2p-tmp")
  into("$buildDir/generated/sources/js2p")
}
val deleteJs2pTmp by tasks.registering(Delete::class) {
  dependsOn(overwriteJs2p)

  delete("$buildDir/generated/sources/js2p-tmp/")
}

tasks.getByName("compileJava").dependsOn(deleteJs2pTmp)
tasks.getByName("sourcesJar").dependsOn(deleteJs2pTmp)

// Exclude jsonschema2pojo generated sources from checkstyle
tasks.named<Checkstyle>("checkstyleMain") {
  exclude("**/fileconfig/internal/model/**")
}

tasks {
  withType<Test>().configureEach {
    environment(
      mapOf(
        // Expose the kitchen sink example file to tests
        "CONFIG_EXAMPLE_DIR" to "$buildDir/configuration/examples/"
      )
    )
  }
}
