plugins {
    `java-library`

    jacoco
}

jacoco {
    toolVersion = "0.8.7"
}

// https://docs.gradle.org/current/samples/sample_jvm_multi_project_with_code_coverage.html

// Do not generate reports for individual projects
tasks.named("jacocoTestReport") {
    enabled = false
}

configurations {
    val implementation by getting

    create("transitiveSourceElements") {
        isVisible = false
        isCanBeResolved = false
        isCanBeConsumed = true
        extendsFrom(implementation)
        attributes {
            attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
            attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.DOCUMENTATION))
            attribute(DocsType.DOCS_TYPE_ATTRIBUTE, objects.named("source-folders"))
        }
        sourceSets.main.get().java.srcDirs.forEach {
            outgoing.artifact(it)
        }
    }

    create("coverageDataElements") {
        isVisible = false
        isCanBeResolved = false
        isCanBeConsumed = true
        extendsFrom(implementation)
        attributes {
            attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
            attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.DOCUMENTATION))
            attribute(DocsType.DOCS_TYPE_ATTRIBUTE, objects.named("jacoco-coverage-data"))
        }
        // This will cause the test task to run if the coverage data is requested by the aggregation task
        // The tasks must be eagerly evaluated (no configureEach) to ensure jacoco is wired up
        // correctly.
        tasks.withType<Test>() {
            outgoing.artifact(the<JacocoTaskExtension>().destinationFile!!)
        }
    }
}
