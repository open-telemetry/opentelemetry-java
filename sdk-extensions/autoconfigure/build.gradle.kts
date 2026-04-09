import de.undercouch.gradle.tasks.download.Download
import java.io.FileFilter

plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("de.undercouch.download")
  id("org.jsonschema2pojo")
}

description = "OpenTelemetry SDK Auto-configuration"
otelJava.moduleName.set("io.opentelemetry.sdk.autoconfigure")

dependencies {
  api(project(":sdk:all"))
  api(project(":sdk-extensions:autoconfigure-spi"))

  compileOnly(project(":api:incubator"))
  compileOnly(project(":sdk-extensions:incubator"))

  annotationProcessor("com.google.auto.value:auto-value")

  compileOnly("org.snakeyaml:snakeyaml-engine")
  compileOnly("com.fasterxml.jackson.core:jackson-databind")
  api("com.fasterxml.jackson.core:jackson-annotations")

  testImplementation(project(":sdk:trace-shaded-deps"))
  testImplementation(project(":sdk:testing"))

  testImplementation("com.google.guava:guava")
  testImplementation("edu.berkeley.cs.jqf:jqf-fuzz")
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
generateJsonSchema2Pojo.dependsOn(unzipConfigurationSchema)

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

val buildGraalVmReflectionJson = tasks.register("buildGraalVmReflectionJson") {
  val buildDir = buildDirectory
  val targetFile = File(
    buildDir,
    "resources/main/META-INF/native-image/io.opentelemetry/io.opentelemetry.sdk.autoconfigure/reflect-config.json"
  )
  val sourcePackage =
    "io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model"
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

tasks.getByName("compileJava").dependsOn(deleteJs2pTmp)
tasks.getByName("sourcesJar").dependsOn(deleteJs2pTmp, buildGraalVmReflectionJson)
tasks.getByName("jar").dependsOn(deleteJs2pTmp, buildGraalVmReflectionJson)
tasks.getByName("javadoc").dependsOn(buildGraalVmReflectionJson)
tasks.getByName("compileTestJava").dependsOn(buildGraalVmReflectionJson)

// Exclude jsonschema2pojo generated sources from checkstyle
tasks.named<Checkstyle>("checkstyleMain") {
  dependsOn(buildGraalVmReflectionJson)
  exclude("**/fileconfig/internal/model/**")
}

tasks {
  withType<Test>().configureEach {
    environment(
      mapOf(
        // Expose the kitchen sink example file to tests
        "CONFIG_REPO_ROOT" to "$buildDirectory/configuration"
      )
    )
  }
}

testing {
  suites {
    register<JvmTestSuite>("testAutoConfigureOrder") {
      targets {
        all {
          testTask {
            environment("OTEL_TRACES_EXPORTER", "none")
            environment("OTEL_METRICS_EXPORTER", "none")
            environment("OTEL_LOGS_EXPORTER", "none")
          }
        }
      }
    }
    register<JvmTestSuite>("testConditionalResourceProvider") {
      targets {
        all {
          testTask {
            environment("OTEL_TRACES_EXPORTER", "none")
            environment("OTEL_METRICS_EXPORTER", "none")
            environment("OTEL_LOGS_EXPORTER", "none")
          }
        }
      }
    }
    register<JvmTestSuite>("testFullConfig") {
      dependencies {
        implementation(project(":extensions:trace-propagators"))
        implementation(project(":exporters:logging"))
        implementation(project(":exporters:logging-otlp"))
        implementation(project(":exporters:otlp:all"))
        implementation(project(":exporters:prometheus"))
        implementation("io.prometheus:prometheus-metrics-exporter-httpserver") {
          exclude(group = "io.prometheus", module = "prometheus-metrics-exposition-formats")
        }
        implementation(project(":exporters:zipkin"))
        implementation(project(":sdk:testing"))
        implementation(project(":sdk:trace-shaded-deps"))
        implementation(project(":sdk-extensions:jaeger-remote-sampler"))
        implementation(project(":sdk-extensions:incubator"))

        implementation("com.google.guava:guava")
        implementation("io.opentelemetry.proto:opentelemetry-proto")
        implementation("com.linecorp.armeria:armeria-junit5")
        implementation("com.linecorp.armeria:armeria-grpc")
      }

      targets {
        all {
          testTask {
            environment("OTEL_SERVICE_NAME", "test")
            environment("OTEL_RESOURCE_ATTRIBUTES", "cat=meow")
            environment("OTEL_PROPAGATORS", "tracecontext,baggage,b3,b3multi,jaeger,ottrace,test")
            environment("OTEL_EXPORTER_OTLP_HEADERS", "cat=meow,dog=bark")
            environment("OTEL_EXPORTER_OTLP_TIMEOUT", "5000")
            environment("OTEL_TEST_CONFIGURED", "true")
            environment("OTEL_TEST_WRAPPED", "1")
            environment("OTEL_EXPERIMENTAL_SDK_TELEMETRY_VERSION", "latest")
          }
        }
      }
    }
    register<JvmTestSuite>("testIncubating") {
      dependencies {
        implementation(project(":api:incubator"))
        implementation(project(":sdk-extensions:incubator"))
        implementation(project(":exporters:logging"))
        implementation(project(":exporters:otlp:all"))
        implementation(project(":sdk:testing"))

        implementation("org.snakeyaml:snakeyaml-engine")
        implementation("com.fasterxml.jackson.core:jackson-databind")

        implementation("io.opentelemetry.proto:opentelemetry-proto")
        implementation("com.linecorp.armeria:armeria-junit5")
        implementation("com.linecorp.armeria:armeria-grpc")
      }
    }

    register<JvmTestSuite>("testDeclarativeConfig") {
      dependencies {
        implementation(project(":api:incubator"))
        implementation(project(":sdk-extensions:incubator"))
        implementation(project(":sdk:testing"))
        implementation(project(":sdk:trace-shaded-deps"))

        implementation("org.snakeyaml:snakeyaml-engine")
        implementation("com.fasterxml.jackson.core:jackson-databind")
        implementation("com.google.guava:guava")

        implementation(project(":sdk-extensions:jaeger-remote-sampler"))
        implementation(project(":extensions:trace-propagators"))
        implementation("com.linecorp.armeria:armeria-junit5")
        implementation("io.opentelemetry.contrib:opentelemetry-aws-xray-propagator")

        implementation(project(":exporters:logging"))
        implementation(project(":exporters:logging-otlp"))
        implementation(project(":exporters:otlp:all"))
        implementation(project(":exporters:prometheus"))
      }
    }

    register<JvmTestSuite>("testDeclarativeConfigSpi") {
      dependencies {
        implementation(project(":api:incubator"))
        implementation(project(":sdk-extensions:incubator"))
        implementation(project(":exporters:logging"))
        implementation(project(":sdk:testing"))
        implementation("org.snakeyaml:snakeyaml-engine")
        implementation("com.fasterxml.jackson.core:jackson-databind")
      }
    }
  }
}

tasks {
  check {
    dependsOn(testing.suites)
  }
}
