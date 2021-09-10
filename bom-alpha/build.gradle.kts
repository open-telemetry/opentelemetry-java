plugins {
  id("java-platform")
  id("otel.publish-conventions")
}

description = "OpenTelemetry Bill of Materials (Alpha)"

rootProject.subprojects.forEach { subproject ->
  if (!project.name.startsWith("opentelemetry-bom")) {
    evaluationDependsOn(subproject.path)
  }
}

afterEvaluate {
  dependencies {
    constraints {
      rootProject.subprojects
        .sortedBy { it.findProperty("archivesBaseName") as String? }
        .filter { !it.name.startsWith("opentelemetry-bom") }
        .filter { it.findProperty("otel.release") == "alpha" }
        .forEach { project ->
          project.plugins.withId("maven-publish") {
            api(project)
          }
        }
    }
  }
}
