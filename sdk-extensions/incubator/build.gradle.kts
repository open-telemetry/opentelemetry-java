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
  api(project(":api:incubator"))
  implementation("com.fasterxml.jackson.core:jackson-databind")
  api("com.fasterxml.jackson.core:jackson-annotations")
  implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")
  implementation(project(":sdk-extensions:autoconfigure"))

  testImplementation(project(":sdk:testing"))
  testImplementation(project(":sdk-extensions:autoconfigure"))
  testImplementation(project(":exporters:logging"))
  testImplementation(project(":exporters:logging-otlp"))
  testImplementation(project(":exporters:otlp:all"))
  testImplementation(project(":exporters:prometheus"))
  testImplementation(project(":exporters:zipkin"))
  testImplementation(project(":sdk-extensions:jaeger-remote-sampler"))
  testImplementation(project(":extensions:trace-propagators"))
  testImplementation("edu.berkeley.cs.jqf:jqf-fuzz")
  testImplementation("io.opentelemetry.contrib:opentelemetry-aws-xray-propagator")
  testImplementation("com.linecorp.armeria:armeria-junit5")

  testImplementation("com.google.guava:guava-testlib")
}

// The following tasks download the JSON Schema files from open-telemetry/opentelemetry-configuration and generate classes from the type definitions which are used with jackson-databind to parse JSON / YAML to the configuration schema.
// The sequence of tasks is:
// 1. downloadConfigurationSchema - download configuration schema from open-telemetry/opentelemetry-configuration
// 2. unzipConfigurationSchema - unzip the configuration schema archive contents to $buildDir/configuration/
// 3. deleteTypeDescriptions - delete type_descriptions.yaml $buildDir/configuration/schema, which is not part of core schema and causes problems resolving type refs
// 4. generateJsonSchema2Pojo - generate java POJOs from the configuration schema
// 5. jsonSchema2PojoPostProcessing - perform various post processing on the generated POJOs, e.g. replace javax.annotation.processing.Generated with javax.annotation.Generated, add @SuppressWarning("rawtypes") annotation
// 6. overwriteJs2p - overwrite original generated classes with versions containing updated @Generated annotation
// 7. deleteJs2pTmp - delete tmp directory
// ... proceed with normal sourcesJar, compileJava, etc

val configurationTag = "1.0.0-rc.1"
val configurationRef = "refs/tags/v$configurationTag" // Replace with commit SHA to point to experiment with a specific commit
val configurationRepoZip = "https://github.com/open-telemetry/opentelemetry-configuration/archive/$configurationRef.zip"
val buildDirectory = layout.buildDirectory.asFile.get()

val downloadConfigurationSchema by tasks.registering(Download::class) {
  src(configurationRepoZip)
  dest("$buildDirectory/configuration/opentelemetry-configuration.zip")
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
  into("$buildDirectory/configuration/")
}

val deleteTypeDescriptions by tasks.registering(Delete::class) {
  dependsOn(unzipConfigurationSchema)
  delete("$buildDirectory/configuration/schema/type_descriptions.yaml")
}

jsonSchema2Pojo {
  sourceFiles = setOf(file("$buildDirectory/configuration/schema"))
  targetDirectory = file("$buildDirectory/generated/sources/js2p/java/main")
  targetPackage = "io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model"

  // Clear old source files to avoid contaminated source dir when updating
  removeOldOutput = true

  // Include @Nullable annotation. Note: jsonSchmea2Pojo will not add @Nullable annotations on getters
  // so we perform some steps in jsonSchema2PojoPostProcessing to add these.
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
}

val generateJsonSchema2Pojo = tasks.getByName("generateJsonSchema2Pojo")
generateJsonSchema2Pojo.dependsOn(deleteTypeDescriptions)

val jsonSchema2PojoPostProcessing by tasks.registering(Copy::class) {
  dependsOn(generateJsonSchema2Pojo)

  from("$buildDirectory/generated/sources/js2p")
  into("$buildDirectory/generated/sources/js2p-tmp")
  filter {
    it
      // Remove @Nullable annotation so it can be deterministically added later
      .replace("import javax.annotation.Nullable;\n", "")
      // Replace java 9+ @Generated annotation with java 8 version, add @Nullable annotation
      .replace("import javax.annotation.processing.Generated;", "import javax.annotation.Nullable;\nimport javax.annotation.Generated;")
      // Add @SuppressWarnings("rawtypes") annotation to address raw types used in jsonschema2pojo builders
      .replace("@Generated(\"jsonschema2pojo\")", "@Generated(\"jsonschema2pojo\")\n@SuppressWarnings(\"rawtypes\")")
      // Add @Nullable annotations to all getters
      .replace("( *)public ([a-zA-Z]*) get([a-zA-Z]*)".toRegex(), "$1@Nullable\n$1public $2 get$3")
  }
}
val overwriteJs2p by tasks.registering(Copy::class) {
  dependsOn(jsonSchema2PojoPostProcessing)

  from("$buildDirectory/generated/sources/js2p-tmp")
  into("$buildDirectory/generated/sources/js2p")
}
val deleteJs2pTmp by tasks.registering(Delete::class) {
  dependsOn(overwriteJs2p)

  delete("$buildDirectory/generated/sources/js2p-tmp/")
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
        "CONFIG_EXAMPLE_DIR" to "$buildDirectory/configuration/examples/"
      )
    )
  }
}
