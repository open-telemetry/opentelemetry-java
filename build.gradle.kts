import com.diffplug.gradle.spotless.SpotlessExtension
import com.google.protobuf.gradle.*
import de.marcphilipp.gradle.nexus.NexusPublishExtension
import io.morethan.jmhreport.gradle.JmhReportExtension
import me.champeau.gradle.JMHPluginExtension
import nebula.plugin.release.git.opinion.Strategies
import net.ltgt.gradle.errorprone.ErrorProneOptions
import net.ltgt.gradle.errorprone.ErrorPronePlugin
import org.gradle.api.plugins.JavaPlugin.*
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import ru.vyarus.gradle.plugin.animalsniffer.AnimalSniffer
import ru.vyarus.gradle.plugin.animalsniffer.AnimalSnifferPlugin
import java.time.Duration

plugins {
    id("com.diffplug.spotless")
    id("com.github.ben-manes.versions")
    id("io.codearte.nexus-staging")
    id("nebula.release")

    id("com.google.protobuf") apply false
    id("de.marcphilipp.nexus-publish") apply false
    id("io.morethan.jmhreport") apply false
    id("me.champeau.gradle.jmh") apply false
    id("net.ltgt.errorprone") apply false
    id("ru.vyarus.animalsniffer") apply false
}

if (!JavaVersion.current().isJava11Compatible()) {
    throw GradleException("JDK 11 or higher is required to build. " +
            "One option is to download it from https://adoptopenjdk.net/. If you believe you already " +
            "have it, please check that the JAVA_HOME environment variable is pointing at the " +
            "JDK 11 installation.")
}

// Nebula plugin will not configure if .git doesn't exist, let's allow building on it by stubbing it
// out. This supports building from the zip archive downloaded from GitHub.
var releaseTask: TaskProvider<Task>
if (file(".git").exists()) {
    release {
        defaultVersionStrategy = Strategies.getSNAPSHOT()
    }

    nebulaRelease {
        addReleaseBranchPattern("""v\d+\.\d+\.x""")
    }

    releaseTask = tasks.named("release")
    releaseTask.configure {
        mustRunAfter("snapshotSetup", "finalSetup")
    }
} else {
    releaseTask = tasks.register("release")
}

nexusStaging {
    packageGroup = "io.opentelemetry"
    username = System.getenv("SONATYPE_USER")
    password = System.getenv("SONATYPE_KEY")

    // We have many artifacts so Maven Central takes a long time on its compliance checks. This sets
    // the timeout for waiting for the repository to close to a comfortable 50 minutes.
    numberOfRetries = 300
    delayBetweenRetriesInMillis = 10000
}

subprojects {
    group = "io.opentelemetry"

    plugins.withId("java") {
        plugins.apply("checkstyle")
        plugins.apply("eclipse")
        plugins.apply("idea")
        plugins.apply("jacoco")

        plugins.apply("com.diffplug.spotless")
        plugins.apply("net.ltgt.errorprone")

        configure<BasePluginConvention> {
            archivesBaseName = "opentelemetry-${name}"
        }

        repositories {
            mavenCentral()
            jcenter()
            mavenLocal()
        }

        configure<JavaPluginExtension> {
            sourceCompatibility = JavaVersion.VERSION_1_8
            targetCompatibility = JavaVersion.VERSION_1_8

            toolchain {
                languageVersion.set(JavaLanguageVersion.of(11))
            }

            withJavadocJar()
            withSourcesJar()
        }

        configure<CheckstyleExtension> {
            configDirectory.set(file("$rootDir/buildscripts/"))
            toolVersion = "8.12"
            isIgnoreFailures = false
            configProperties["rootDir"] = rootDir
        }

        configure<JacocoPluginExtension> {
            toolVersion = "0.8.6"
        }

        val javaToolchains = the<JavaToolchainService>()

        tasks {
            withType(Javadoc::class) {
                exclude("io/opentelemetry/internal/**")
            }

            val testJava8 by registering(Test::class) {
                javaLauncher.set(javaToolchains.launcherFor {
                    languageVersion.set(JavaLanguageVersion.of(14))
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

            withType(JavaCompile::class) {
                options.release.set(8)

                options.compilerArgs.addAll(listOf(
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

                options.encoding = "UTF-8"

                if (name.contains("Test")) {
                    // serialVersionUID is basically guaranteed to be useless in tests
                    options.compilerArgs.add("-Xlint:-serial")
                }

                (options as ExtensionAware).extensions.configure<ErrorProneOptions> {
                    disableWarningsInGeneratedCode.set(true)
                    allDisabledChecksAsWarnings.set(true)

                    // Doesn't currently use Var annotations.
                    disable("Var") // "-Xep:Var:OFF"

                    // ImmutableRefactoring suggests using com.google.errorprone.annotations.Immutable,
                    // but currently uses javax.annotation.concurrent.Immutable
                    disable("ImmutableRefactoring") // "-Xep:ImmutableRefactoring:OFF"

                    // AutoValueImmutableFields suggests returning Guava types from API methods
                    disable("AutoValueImmutableFields")
                    // "-Xep:AutoValueImmutableFields:OFF"

                    // Fully qualified names may be necessary when deprecating a class to avoid
                    // deprecation warning.
                    disable("UnnecessarilyFullyQualified")

                    // Ignore warnings for protobuf and jmh generated files.
                    excludedPaths.set(".*generated.*")
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
                }
            }

            withType(Test::class) {
                useJUnitPlatform()

                testLogging {
                    exceptionFormat = TestExceptionFormat.FULL
                    showExceptions = true
                    showCauses = true
                    showStackTraces = true
                }
                maxHeapSize = "1500m"
            }

            withType(Javadoc::class) {
                with(options as StandardJavadocDocletOptions) {
                    source = "8"
                    encoding = "UTF-8"
                    links("https://docs.oracle.com/javase/8/docs/api/")
                    addBooleanOption("Xdoclint:all,-missing", true)
                }
            }

            afterEvaluate {
                withType(Jar::class) {
                    val moduleName: String by project
                    inputs.property("moduleName", moduleName)

                    manifest {
                        attributes("Automatic-Module-Name" to moduleName)
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
                tasks.withType(Test::class).forEach { task ->
                    outgoing.artifact(task.extensions.getByType<JacocoTaskExtension>().destinationFile!!)
                }
            }

            // TODO(anuraaga): Probably remove this, compile is a deprecated configuration.
            named("compile") {
                // Detect Maven Enforcer's dependencyConvergence failures. We only
                // care for artifacts used as libraries by others.
                // TODO: Enable failOnVersionConflict()
                resolutionStrategy.preferProjectModules()
            }
        }

        configure<SpotlessExtension> {
            java {
                googleJavaFormat("1.9")
                licenseHeaderFile(rootProject.file("buildscripts/spotless.license.java"), "(package|import|class|// Includes work from:)")
            }
        }

        dependencies {
            configurations.configureEach {
                // Gradle and newer plugins will set these configuration properties correctly.
                if (isCanBeResolved && !isCanBeConsumed
                        // Older ones (like JMH) may not, so check the name as well.
                        // Kotlin compiler classpaths don't support BOM nor need it.
                        || name.endsWith("Classpath") && !name.startsWith("kotlin")) {
                    add(name, platform(project(":dependencyManagement")))
                }
            }

            add(COMPILE_ONLY_CONFIGURATION_NAME, "com.google.auto.value:auto-value-annotations")
            add(COMPILE_ONLY_CONFIGURATION_NAME, "com.google.errorprone:error_prone_annotations")
            add(COMPILE_ONLY_CONFIGURATION_NAME, "com.google.code.findbugs:jsr305")

            add(TEST_COMPILE_ONLY_CONFIGURATION_NAME, "com.google.auto.value:auto-value-annotations")
            add(TEST_COMPILE_ONLY_CONFIGURATION_NAME, "com.google.errorprone:error_prone_annotations")
            add(TEST_COMPILE_ONLY_CONFIGURATION_NAME, "com.google.code.findbugs:jsr305")

            add(TEST_IMPLEMENTATION_CONFIGURATION_NAME, "org.junit.jupiter:junit-jupiter-api")
            add(TEST_IMPLEMENTATION_CONFIGURATION_NAME, "nl.jqno.equalsverifier:equalsverifier")
            add(TEST_IMPLEMENTATION_CONFIGURATION_NAME, "org.mockito:mockito-core")
            add(TEST_IMPLEMENTATION_CONFIGURATION_NAME, "org.mockito:mockito-junit-jupiter")
            add(TEST_IMPLEMENTATION_CONFIGURATION_NAME, "org.assertj:assertj-core")
            add(TEST_IMPLEMENTATION_CONFIGURATION_NAME, "org.awaitility:awaitility")
            add(TEST_IMPLEMENTATION_CONFIGURATION_NAME, "io.github.netmikey.logunit:logunit-jul")

            add(TEST_RUNTIME_ONLY_CONFIGURATION_NAME, "org.junit.jupiter:junit-jupiter-engine")
            add(TEST_RUNTIME_ONLY_CONFIGURATION_NAME, "org.junit.vintage:junit-vintage-engine")

            add(ErrorPronePlugin.CONFIGURATION_NAME, "com.google.errorprone:error_prone_core")

            add(ANNOTATION_PROCESSOR_CONFIGURATION_NAME, "com.google.guava:guava-beta-checker")

            // Workaround for @javax.annotation.Generated
            // see: https://github.com/grpc/grpc-java/issues/3633
            add(COMPILE_ONLY_CONFIGURATION_NAME, "javax.annotation:javax.annotation-api")
        }

        plugins.withId("com.google.protobuf") {
            protobuf {
                val versions: Map<String, String> by project
                protoc {
                    // The artifact spec for the Protobuf Compiler
                    artifact = "com.google.protobuf:protoc:${versions["com.google.protobuf"]}"
                }
                plugins {
                    id("grpc") {
                        artifact = "io.grpc:protoc-gen-grpc-java:${versions["io.grpc"]}"
                    }
                }
                generateProtoTasks {
                    all().configureEach {
                        plugins {
                            id("grpc")
                        }
                    }
                }
            }
        }

        plugins.withId("ru.vyarus.animalsniffer") {
            dependencies {
                add(AnimalSnifferPlugin.SIGNATURE_CONF, "com.toasttab.android:gummy-bears-api-24:0.3.0:coreLib@signature")

                tasks {
                    withType(AnimalSniffer::class) {
                        if (name.startsWith("animalsnifferTest")) {
                            enabled = false
                        }
                    }

                    // If JMH enabled ignore animalsniffer.
                    plugins.withId("me.champeau.gradle.jmh") {
                        named("animalsnifferJmh") {
                            enabled = false
                        }
                    }
                }
            }
        }

        plugins.withId("me.champeau.gradle.jmh") {
            // Always include the jmhreport plugin and run it after jmh task.
            plugins.apply("io.morethan.jmhreport")
            dependencies {
                add("jmh", "org.openjdk.jmh:jmh-core")
                add("jmh", "org.openjdk.jmh:jmh-generator-bytecode")
            }

            // invoke jmh on a single benchmark class like so:
            //   ./gradlew -PjmhIncludeSingleClass=StatsTraceContextBenchmark clean :grpc-core:jmh
            configure<JMHPluginExtension> {
                failOnError = true
                resultFormat = "JSON"
                // Otherwise an error will happen:
                // Could not expand ZIP 'byte-buddy-agent-1.9.7.jar'.
                isIncludeTests = false
                profilers = listOf("gc")
                val jmhIncludeSingleClass: String? by project
                if (jmhIncludeSingleClass != null) {
                    include = listOf(jmhIncludeSingleClass)
                }
            }

            configure<JmhReportExtension> {
                jmhResultPath = file("${buildDir}/reports/jmh/results.json").absolutePath
                jmhReportOutput = file("${buildDir}/reports/jmh").absolutePath
            }

            tasks {
                named("jmh") {
                    finalizedBy(named("jmhReport"))
                }
            }
        }

        plugins.withId("maven-publish") {
            plugins.apply("signing")

            plugins.apply("de.marcphilipp.nexus-publish")

            configure<PublishingExtension> {
                publications {
                    register<MavenPublication>("mavenPublication") {
                        val release = findProperty("otel.release")
                        if (release != null) {
                            val versionParts = version.split('-').toMutableList()
                            versionParts[0] += "-${release}"
                            version = versionParts.joinToString("-")
                        }
                        groupId = "io.opentelemetry"
                        afterEvaluate {
                            // not available until evaluated.
                            artifactId = the<BasePluginConvention>().archivesBaseName
                            description = project.description
                        }

                        plugins.withId("java-platform") {
                            from(components["javaPlatform"])
                        }
                        plugins.withId("java-library") {
                            from(components["java"])
                        }

                        versionMapping {
                            allVariants {
                                fromResolutionResult()
                            }
                        }

                        pom {
                            name.set("OpenTelemetry Java")
                            url.set("https://github.com/open-telemetry/opentelemetry-java")

                            licenses {
                                license {
                                    name.set("The Apache License, Version 2.0")
                                    url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                                }
                            }

                            developers {
                                developer {
                                    id.set("opentelemetry")
                                    name.set("OpenTelemetry")
                                    url.set("https://github.com/open-telemetry/community")
                                }
                            }

                            scm {
                                connection.set("scm:git:git@github.com:open-telemetry/opentelemetry-java.git")
                                developerConnection.set("scm:git:git@github.com:open-telemetry/opentelemetry-java.git")
                                url.set("git@github.com:open-telemetry/opentelemetry-java.git")
                            }
                        }
                    }
                }
            }

            configure<NexusPublishExtension> {
                repositories {
                    sonatype()
                }

                connectTimeout.set(Duration.ofMinutes(5))
                clientTimeout.set(Duration.ofMinutes(5))
            }

            val publishToSonatype by tasks.getting
            releaseTask.configure {
                finalizedBy(publishToSonatype)
            }
            rootProject.tasks.named("closeAndReleaseRepository") {
                mustRunAfter(publishToSonatype)
            }

            tasks.withType(Sign::class) {
                onlyIf { System.getenv("CI") != null }
            }

            configure<SigningExtension> {
                useInMemoryPgpKeys(System.getenv("GPG_PRIVATE_KEY"), System.getenv("GPG_PASSWORD"))
                sign(the<PublishingExtension>().publications["mavenPublication"])
            }
        }
    }
}

allprojects {
    tasks.register("updateVersionInDocs") {
        group = "documentation"
        doLast {
            val versionParts = version.toString().split('.')
            val minorVersionNumber = Integer.parseInt(versionParts[1])
            val nextSnapshot = "${versionParts[0]}.${minorVersionNumber + 1}.0-SNAPSHOT"

            val readme = file("README.md")
            if (readme.exists()) {
                val readmeText = readme.readText()
                val updatedText = readmeText
                        .replace("""<version>\d+\.\d+\.\d+</version>""".toRegex(), "<version>${version}</version>")
                        .replace("""<version>\d+\.\d+\.\d+-SNAPSHOT</version>""".toRegex(), "<version>${nextSnapshot}</version>")
                        .replace("""(implementation.*io\.opentelemetry:.*:)(\d+\.\d+\.\d+)(?!-SNAPSHOT)(.*)""".toRegex(), "\$1${version}\$3")
                        .replace("""(implementation.*io\\.opentelemetry:.*:)(\d+\\.\d+\.\d+-SNAPSHOT)(.*)""".toRegex(), "\$1${nextSnapshot}\$3")
                        .replace("""<!--VERSION_STABLE-->.*<!--/VERSION_STABLE-->""".toRegex(), "<!--VERSION_STABLE-->${version}<!--/VERSION_STABLE-->")
                        .replace("""<!--VERSION_UNSTABLE-->.*<!--/VERSION_UNSTABLE-->""".toRegex(), "<!--VERSION_UNSTABLE-->${version}-alpha<!--/VERSION_UNSTABLE-->")
                readme.writeText(updatedText)
            }
        }
    }
}
