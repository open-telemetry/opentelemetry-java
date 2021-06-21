import com.google.protobuf.gradle.*
import nebula.plugin.release.git.opinion.Strategies
import org.gradle.api.plugins.JavaPlugin.*
import ru.vyarus.gradle.plugin.animalsniffer.AnimalSnifferExtension
import ru.vyarus.gradle.plugin.animalsniffer.AnimalSnifferPlugin
import java.time.Duration

plugins {
    id("com.diffplug.spotless")
    id("com.github.ben-manes.versions")
    id("io.github.gradle-nexus.publish-plugin")
    id("nebula.release")

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

nexusPublishing {
    packageGroup.set("io.opentelemetry")

    repositories {
        sonatype {
            username.set(System.getenv("SONATYPE_USER"))
            password.set(System.getenv("SONATYPE_KEY"))
        }
    }

    connectTimeout.set(Duration.ofMinutes(5))
    clientTimeout.set(Duration.ofMinutes(5))

    transitionCheckOptions {
        // We have many artifacts so Maven Central takes a long time on its compliance checks. This sets
        // the timeout for waiting for the repository to close to a comfortable 50 minutes.
        maxRetries.set(300)
        delayBetween.set(Duration.ofSeconds(10))
    }
}

subprojects {
    group = "io.opentelemetry"

    plugins.withId("java") {
        plugins.withId("ru.vyarus.animalsniffer") {
            dependencies {
                add(AnimalSnifferPlugin.SIGNATURE_CONF, "com.toasttab.android:gummy-bears-api-21:0.3.0:coreLib@signature")
            }

            configure<AnimalSnifferExtension> {
                sourceSets = listOf(the<JavaPluginConvention>().sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME))
            }
        }
    }

    plugins.withId("maven-publish") {
        // generate the api diff report for any module that is stable and publishes a jar.
        if (!project.hasProperty("otel.release") && !project.name.startsWith("bom")) {
            plugins.apply("otel.japicmp-conventions")
        }
        plugins.apply("signing")

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
                        pom.description.set(project.description)
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

        afterEvaluate {
            val publishToSonatype by tasks.getting
            releaseTask.configure {
                finalizedBy(publishToSonatype)
            }
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
                        .replace("""(implementation.*io\.opentelemetry:.*:)(\d+\.\d+\.\d+-SNAPSHOT)(.*)""".toRegex(), "\$1${nextSnapshot}\$3")
                        .replace("""<!--VERSION_STABLE-->.*<!--/VERSION_STABLE-->""".toRegex(), "<!--VERSION_STABLE-->${version}<!--/VERSION_STABLE-->")
                        .replace("""<!--VERSION_UNSTABLE-->.*<!--/VERSION_UNSTABLE-->""".toRegex(), "<!--VERSION_UNSTABLE-->${version}-alpha<!--/VERSION_UNSTABLE-->")
                readme.writeText(updatedText)
            }
        }
    }
}
