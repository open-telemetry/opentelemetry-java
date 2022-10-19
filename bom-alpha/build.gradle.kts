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
}