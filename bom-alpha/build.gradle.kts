plugins {
  id("otel.bom-conventions")
}

description = "OpenTelemetry Bill of Materials (Alpha)"
group = "io.opentelemetry"
base.archivesBaseName = "opentelemetry-bom-alpha"

otelBom.projectFilter.set { it.findProperty("otel.release") == "alpha" }