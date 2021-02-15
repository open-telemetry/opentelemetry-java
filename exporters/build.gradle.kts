subprojects {
    // https://github.com/gradle/gradle/issues/847
    group = "io.opentelemetry.exporters"
    plugins.withId("java") {
        configure<BasePluginConvention> {
            archivesBaseName = "opentelemetry-exporter-${project.name}"
        }
    }
}
