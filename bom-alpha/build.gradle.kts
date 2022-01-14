plugins {
  id("otel.bom-conventions")
}

description = "OpenTelemetry Bill of Materials (Alpha)"
group = "io.opentelemetry"
base.archivesName.set("opentelemetry-bom-alpha")

otelBom.projectFilter.set { it.findProperty("otel.release") == "alpha" }