subprojects {
  val proj = this
  plugins.withId("java") {
    configure<BasePluginConvention> { archivesBaseName = "opentelemetry-extension-${proj.name}" }
  }
}
