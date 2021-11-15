import io.opentelemetry.gradle.OtelBomExtension
import org.gradle.kotlin.dsl.create

plugins {
  id("otel.publish-conventions")
  id("java-platform")
}

if (!project.name.startsWith("bom")) {
  throw IllegalStateException("Name of BOM projects must start with 'bom'.")
}

rootProject.subprojects.forEach { subproject ->
  if (!subproject.name.startsWith("bom")) {
    evaluationDependsOn(subproject.path)
  }
}
val otelBom = extensions.create<OtelBomExtension>("otelBom")

val generateBuildSubstitutions by tasks.registering {
  group = "publishing"
  description = "Generate a code snippet that can be copy-pasted for use in composite builds."
}

tasks.named("publish") {
  dependsOn(generateBuildSubstitutions)
}

rootProject.tasks.named(generateBuildSubstitutions.name) {
  dependsOn(generateBuildSubstitutions)
}

afterEvaluate {
  otelBom.projectFilter.finalizeValue()
  val bomProjects = rootProject.subprojects
    .sortedBy { it.findProperty("archivesName") as String? }
    .filter { !it.name.startsWith("bom") }
    .filter(otelBom.projectFilter.get()::test)
    .filter { it.plugins.hasPlugin("maven-publish") }

  generateBuildSubstitutions {
    val outputFile = File(buildDir, "substitutions.gradle.kts")
    outputs.file(outputFile)
    val substitutionSnippet = bomProjects.joinToString(
      separator = "\n",
      prefix = "dependencySubstitution {\n",
      postfix = "\n}\n"
    ) { project ->
      val publication = project.publishing.publications.getByName("mavenPublication") as MavenPublication
      "  substitute(module(\"${publication.groupId}:${publication.artifactId}\")).using(project(\"${project.path}\"))"
    }
    inputs.property("projectPathsAndArtifactCoordinates", substitutionSnippet)
    doFirst {
      outputFile.writeText(substitutionSnippet)
    }
  }
  bomProjects.forEach { project ->
    dependencies {
      constraints {
        api(project)
      }
    }
  }
}