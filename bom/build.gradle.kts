plugins {
    id("java-platform")
    id("maven-publish")
}

description = "OpenTelemetry Bill of Materials"
group = "io.opentelemetry"
base.archivesBaseName = "opentelemetry-bom"

rootProject.subprojects.forEach { subproject ->
    if (project != subproject) {
        evaluationDependsOn(subproject.path)
    }
}

afterEvaluate {
    dependencies {
        constraints {
            rootProject.subprojects
                    .sortedBy { it.findProperty("archivesBaseName") as String? }
                    .filter { it.name != project.name }
                    .filter { !it.hasProperty("otel.release") }
                    .forEach { project ->
                        project.plugins.withId("maven-publish") {
                            api(project)
                        }
                    }
        }
    }
}
