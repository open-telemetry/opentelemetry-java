subprojects {
    plugins.withId("java") {
        configure<BasePluginConvention> {
            archivesBaseName = "opentelemetry-extension-${project.name}"
        }
    }
}
