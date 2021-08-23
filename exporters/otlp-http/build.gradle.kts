subprojects {
  val proj = this
  plugins.withId("java") {
    configure<BasePluginConvention> {
      archivesBaseName = "opentelemetry-exporter-otlp-http-${proj.name}"
    }
  }
}
