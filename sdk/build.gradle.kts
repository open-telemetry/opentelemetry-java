subprojects {
  // Workaround https://github.com/gradle/gradle/issues/847
  group = "io.opentelemetry.sdk"
  val proj = this
  plugins.withId("java") {
    configure<BasePluginConvention> {
      archivesBaseName = "opentelemetry-sdk-${proj.name}"
    }
  }
}
