subprojects {
    plugins.withId("java") {
        configure<BasePluginConvention> {
            archivesBaseName = "opentelemetry-sdk-extension-${project.name}"
        }
    }
}
