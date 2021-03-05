plugins {
    id("java-platform")
    id("maven-publish")
}

description = "OpenTelemetry Bill of Materials (Alpha)"
group = "io.opentelemetry"
base.archivesBaseName = "opentelemetry-bom-alpha"

rootProject.subprojects.forEach { subproject ->
    if (!project.name.startsWith("bom")) {
        evaluationDependsOn(subproject.path)
    }
}

afterEvaluate {
    dependencies {
        constraints {
            rootProject.subprojects
                    .sortedBy { it.findProperty("archivesBaseName") as String? }
                    .filter { !it.name.startsWith("bom") }
                    .filter { it.findProperty("otel.release") == "alpha" }
                    .forEach { project ->
                        project.plugins.withId("maven-publish") {
                            api(project)
                        }
                    }
        }
    }
}
