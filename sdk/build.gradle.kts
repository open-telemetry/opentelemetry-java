subprojects {
    // Workaround https://github.com/gradle/gradle/issues/847
    group = "io.opentelemetry.sdk"
    plugins.withId("java") {
        configure<BasePluginConvention> {
            archivesBaseName = "opentelemetry-sdk-${project.name}"
        }
    }
}
