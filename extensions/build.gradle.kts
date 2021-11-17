subprojects {
  val proj = this
  plugins.withId("java") {
    configure<BasePluginExtension> {
      archivesName.set("opentelemetry-extension-${proj.name}")
    }
  }
}
