subprojects {
    plugins.withId("java") {
        configure<BasePluginConvention> {
            archivesBaseName = "opentelemetry-exporter-otlp-${project.name}"
        }
    }
}
