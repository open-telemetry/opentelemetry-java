subprojects {
    // https://github.com/gradle/gradle/issues/847
    group = "io.opentelemetry.exporters"
    val proj = this
    plugins.withId("java") {
        configure<BasePluginConvention> {
            archivesBaseName = "opentelemetry-exporter-${proj.name}"
        }
    }
}
