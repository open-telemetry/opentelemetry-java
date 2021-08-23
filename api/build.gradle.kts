subprojects {
  // Workaround https://github.com/gradle/gradle/issues/847
  group = "io.opentelemetry.api"
  val proj = this
  plugins.withId("java") {
    configure<BasePluginConvention> { archivesBaseName = "opentelemetry-api-${proj.name}" }
  }
}
