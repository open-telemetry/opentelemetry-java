import java.util.function.Consumer

plugins {
  id("otel.bom-conventions")
}

description = "OpenTelemetry Bill of Materials (Alpha)"
group = "io.opentelemetry"
base.archivesName.set("opentelemetry-bom-alpha")

otelBom.projectFilter.set { it.findProperty("otel.release") == "alpha" }

// Required to place dependency on opentelemetry-bom
javaPlatform.allowDependencies()

dependencies {
  // Add dependency on opentelemetry-bom to ensure synchronization between alpha and stable artifacts
  api(platform(project(":bom")))

  // Get the semconv version from :dependencyManagement
  val semconvConstraint = project(":dependencyManagement").dependencyProject.configurations["api"].allDependencyConstraints
    .find { it.group.equals("io.opentelemetry.semconv")
        && it.name.equals("opentelemetry-semconv") }
    ?: throw Exception("semconv constraint not found")
  otelBom.addExtra(semconvConstraint.group, semconvConstraint.name, semconvConstraint.version ?: throw Exception("missing version"))
}
