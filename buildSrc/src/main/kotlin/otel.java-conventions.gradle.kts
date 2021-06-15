import net.ltgt.gradle.errorprone.CheckSeverity
import net.ltgt.gradle.errorprone.errorprone
import net.ltgt.gradle.nullaway.nullaway
import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    `java-library`

    checkstyle
    eclipse
    idea
    jacoco

    id("com.diffplug.spotless")
    id("net.ltgt.errorprone")
    id("net.ltgt.nullaway")
}


val enableNullaway: String? by project

base {
    // May be set already by a parent project, only set if not.
    // TODO(anuraaga): Make this less hacky by creating a "module group" plugin.
    if (!archivesBaseName.startsWith("opentelemetry-")) {
        archivesBaseName = "opentelemetry-${name}"
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }

    withJavadocJar()
    withSourcesJar()
}

checkstyle {
    configDirectory.set(file("$rootDir/buildscripts/"))
    toolVersion = "8.12"
    isIgnoreFailures = false
    configProperties["rootDir"] = rootDir
}

jacoco {
    toolVersion = "0.8.7"
}

tasks {
    val testJava8 by registering(Test::class) {
        javaLauncher.set(javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(8))
        })

        configure<JacocoTaskExtension> {
            enabled = false
        }
    }

    val testAdditionalJavaVersions: String? by rootProject
    if (testAdditionalJavaVersions == "true") {
        named("check") {
            dependsOn(testJava8)
        }
    }

    withType<JavaCompile>().configureEach {
        with(options) {
            release.set(8)

            if (name != "jmhCompileGeneratedClasses") {
                compilerArgs.addAll(listOf(
                        "-Xlint:all",
                        // We suppress the "try" warning because it disallows managing an auto-closeable with
                        // try-with-resources without referencing the auto-closeable within the try block.
                        "-Xlint:-try",
                        // We suppress the "processing" warning as suggested in
                        // https://groups.google.com/forum/#!topic/bazel-discuss/_R3A9TJSoPM
                        "-Xlint:-processing",
                        // We suppress the "options" warning because it prevents compilation on modern JDKs
                        "-Xlint:-options",

                        // Fail build on any warning
                        "-Werror"
                ))
            }

            //disable deprecation warnings for the protobuf module
            if (project.name == "proto") {
                compilerArgs.add("-Xlint:-deprecation")
            }

            encoding = "UTF-8"

            if (name.contains("Test")) {
                // serialVersionUID is basically guaranteed to be useless in tests
                compilerArgs.add("-Xlint:-serial")
            }

            errorprone {
                disableWarningsInGeneratedCode.set(true)
                allDisabledChecksAsWarnings.set(true)

                // Doesn't currently use Var annotations.
                disable("Var") // "-Xep:Var:OFF"

                // ImmutableRefactoring suggests using com.google.errorprone.annotations.Immutable,
                // but currently uses javax.annotation.concurrent.Immutable
                disable("ImmutableRefactoring") // "-Xep:ImmutableRefactoring:OFF"

                // AutoValueImmutableFields suggests returning Guava types from API methods
                disable("AutoValueImmutableFields")
                // Suggests using Guava types for fields but we don't use Guava
                disable("ImmutableMemberCollection")
                // "-Xep:AutoValueImmutableFields:OFF"

                // Fully qualified names may be necessary when deprecating a class to avoid
                // deprecation warning.
                disable("UnnecessarilyFullyQualified")

                // Ignore warnings for protobuf and jmh generated files.
                excludedPaths.set(".*generated.*|.*internal.shaded.*")
                // "-XepExcludedPaths:.*/build/generated/source/proto/.*"

                disable("Java7ApiChecker")
                disable("AndroidJdkLibsChecker")
                //apparently disabling android doesn't disable this
                disable("StaticOrDefaultInterfaceMethod")

                //until we have everything converted, we need these
                disable("JdkObsolete")
                disable("UnnecessaryAnonymousClass")

                // Limits APIs
                disable("NoFunctionalReturnType")

                // We don't depend on Guava so use normal splitting
                disable("StringSplitter")

                // Prevents lazy initialization
                disable("InitializeInline")

                if (name.contains("Jmh") || name.contains("Test")) {
                    // Allow underscore in test-type method names
                    disable("MemberName")
                }
            }

            errorprone.nullaway {
                // Enable nullaway on main sources.
                // TODO(anuraaga): Remove enableNullaway flag when all errors fixed
                if (!name.contains("Test") && !name.contains("Jmh") && enableNullaway == "true") {
                    severity.set(CheckSeverity.ERROR)
                } else {
                    severity.set(CheckSeverity.OFF)
                }
                annotatedPackages.add("io.opentelemetry")
            }
        }
    }

    withType<Test>().configureEach {
        useJUnitPlatform()

        testLogging {
            exceptionFormat = TestExceptionFormat.FULL
            showExceptions = true
            showCauses = true
            showStackTraces = true
        }
        maxHeapSize = "1500m"
    }

    withType<Javadoc>().configureEach {
        exclude("io/opentelemetry/**/internal/**")

        with(options as StandardJavadocDocletOptions) {
            source = "8"
            encoding = "UTF-8"
            docEncoding = "UTF-8"
            breakIterator(true)

            addBooleanOption("html5", true)

            links("https://docs.oracle.com/javase/8/docs/api/")
            addBooleanOption("Xdoclint:all,-missing", true)
        }
    }

    afterEvaluate {
        withType<Javadoc>().configureEach {
            with(options as StandardJavadocDocletOptions) {
                val title = "${project.description}"
                docTitle = title
                windowTitle = title
            }
        }

        withType<Jar>().configureEach {
            val moduleName: String by project
            inputs.property("moduleName", moduleName)

            manifest {
                attributes(
                        "Automatic-Module-Name" to moduleName,
                        "Built-By" to System.getProperty("user.name"),
                        "Built-JDK" to System.getProperty("java.version"),
                        "Implementation-Title" to project.name,
                        "Implementation-Version" to project.version)
            }
        }
    }
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
        val mainSources = the<JavaPluginConvention>().sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME)
        mainSources.java.srcDirs.forEach {
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
        tasks.withType(Test::class) {
            outgoing.artifact(extensions.getByType<JacocoTaskExtension>().destinationFile!!)
        }
    }

    configureEach {
        resolutionStrategy {
            failOnVersionConflict()
            preferProjectModules()
        }
    }
}

spotless {
    java {
        googleJavaFormat("1.9")
        licenseHeaderFile(rootProject.file("buildscripts/spotless.license.java"), "(package|import|class|// Includes work from:)")
    }
}

val dependencyManagement by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = false
    isVisible = false
}

dependencies {
    dependencyManagement(platform(project(":dependencyManagement")))
    afterEvaluate {
        configurations.configureEach {
            if (isCanBeResolved && !isCanBeConsumed) {
                extendsFrom(dependencyManagement)
            }
        }
    }

    compileOnly("com.google.auto.value:auto-value-annotations")
    compileOnly("com.google.code.findbugs:jsr305")

    testCompileOnly("com.google.auto.value:auto-value-annotations")
    testCompileOnly("com.google.errorprone:error_prone_annotations")
    testCompileOnly("com.google.code.findbugs:jsr305")

    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testImplementation("nl.jqno.equalsverifier:equalsverifier")
    testImplementation("org.mockito:mockito-core")
    testImplementation("org.mockito:mockito-junit-jupiter")
    testImplementation("org.assertj:assertj-core")
    testImplementation("org.awaitility:awaitility")
    testImplementation("io.github.netmikey.logunit:logunit-jul")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine")

    errorprone("com.google.errorprone:error_prone_core")
    errorprone("com.uber.nullaway:nullaway")

    annotationProcessor("com.google.guava:guava-beta-checker")

    // Workaround for @javax.annotation.Generated
    // see: https://github.com/grpc/grpc-java/issues/3633
    compileOnly("javax.annotation:javax.annotation-api")
}
