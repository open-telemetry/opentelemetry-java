subprojects {
    val proj = this
    plugins.withId("java") {
        configure<BasePluginConvention> {
            archivesBaseName = "opentelemetry-sdk-extension-${proj.name}"
        }
    }
}
