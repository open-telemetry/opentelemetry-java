plugins {
  id("otel.bom-conventions")
}

description = "OpenTelemetry Bill of Materials"
group = "io.opentelemetry"
base.archivesBaseName = "opentelemetry-bom"

otelBom.projectFilter.set { !it.hasProperty("otel.release") }