plugins {
  id("otel.bom-conventions")
}

description = "OpenTelemetry Bill of Materials"
group = "io.opentelemetry"
base.archivesName.set("opentelemetry-bom")

otelBom.projectFilter.set { !it.hasProperty("otel.release") }

// Artifacts that were previously published and included in the BOM for backwards compatibility
otelBom.addFallback("opentelemetry-exporter-jaeger-proto", "1.17.0")
otelBom.addFallback("opentelemetry-extension-annotations", "1.18.0")
otelBom.addFallback("opentelemetry-sdk-extension-resources", "1.19.0")
otelBom.addFallback("opentelemetry-sdk-extension-aws", "1.19.0")
otelBom.addFallback("opentelemetry-extension-aws", "1.20.1")
