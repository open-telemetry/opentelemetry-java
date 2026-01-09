import ru.vyarus.gradle.plugin.animalsniffer.info.SignatureInfoTask
import ru.vyarus.gradle.plugin.animalsniffer.signature.BuildSignatureTask

plugins {
  id("otel.java-conventions")
  id("ru.vyarus.animalsniffer")
}

description = "Build tool to generate the Animal Sniffer Android signature"
otelJava.moduleName.set("io.opentelemetry.internal.animalsniffer")

val signatureJar = configurations.create("signatureJar") {
  isCanBeConsumed = false
  isCanBeResolved = false
}
val signatureJarClasspath = configurations.create("signatureJarClasspath") {
  isCanBeConsumed = false
  isCanBeResolved = true
  extendsFrom(signatureJar)
}
val generatedSignature = configurations.create("generatedSignature") {
  isCanBeConsumed = true
  isCanBeResolved = false
}

dependencies {
  signature("com.toasttab.android:gummy-bears-api-23:0.12.0@signature")
  signatureJar("com.android.tools:desugar_jdk_libs")
}

val signatureSimpleName = "android.signature"
val signatureBuilderTask = tasks.register("buildSignature", BuildSignatureTask::class.java) {
  files(signatureJarClasspath) // All the jar files here will be added to the signature file.
  signatures(configurations.signature) // We'll extend from the existing signatures added to this config.
  outputName = signatureSimpleName // Name for the generated signature file.
}

// Exposing the "generatedSignature" consumable config to be used in other subprojects
artifacts {
  add("generatedSignature", project.provider { File(signatureBuilderTask.get().outputs.files.singleFile, signatureSimpleName) }) {
    builtBy(signatureBuilderTask)
  }
}

// Utility task to show what's in the signature file
tasks.register("printSignature", SignatureInfoTask::class.java) {
  signature = signatureBuilderTask.get().outputFiles
  depth = 1
}
