subprojects {
  // Workaround https://github.com/gradle/gradle/issues/847
  group = "io.opentelemetry.exporter.otlp"
  val proj = this
  plugins.withId("java") {
    configure<BasePluginConvention> {
      archivesBaseName = "opentelemetry-exporter-otlp-${proj.name}"
    }
  }
}
