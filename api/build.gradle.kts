subprojects {
    // Workaround https://github.com/gradle/gradle/issues/847
    group = "io.opentelemetry.api"
    plugins.withId("java") {
        configure<BasePluginConvention> {
            archivesBaseName = "opentelemetry-api-${project.name}"
        }
    }
}
