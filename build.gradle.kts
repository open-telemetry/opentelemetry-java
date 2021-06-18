import com.google.protobuf.gradle.*
import me.champeau.gradle.japicmp.JapicmpTask
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

    id("com.google.protobuf") apply false
    id("io.morethan.jmhreport") apply false
    id("otel.jmh-conventions") apply false
    id("ru.vyarus.animalsniffer") apply false
    id("me.champeau.gradle.japicmp") apply false
}

/**
 * Locate the project's artifact of a particular version.
 */
fun Project.findArtifact(version: String) : File {
    val existingGroup = this.group
    try {
        // Temporarily change the group name because we want to fetch an artifact with the same
        // Maven coordinates as the project, which Gradle would not allow otherwise.
        this.group = "virtual_group"
        val depModule = "io.opentelemetry:${base.archivesBaseName}:$version@jar"
        val depJar = "${base.archivesBaseName}-${version}.jar"
        val configuration: Configuration = configurations.detachedConfiguration(
                dependencies.create(depModule)
        )
        return files(configuration.files).filter {
            it.name.equals(depJar)
        }.singleFile
    } finally {
        this.group = existingGroup
    }
}

/**
 * The latest *released* version of the project. Evaluated lazily so the work is only done if necessary.
 */
val latestReleasedVersion : String by lazy {
    // hack to find the current released version of the project
    val temp: Configuration = project.configurations.create("tempConfig")
    // pick the api, since it's always there.
    dependencies.add("tempConfig", "io.opentelemetry:opentelemetry-api:latest.release")
    val moduleVersion = project.configurations["tempConfig"].resolvedConfiguration.firstLevelModuleDependencies.elementAt(0).moduleVersion
    project.configurations.remove(temp)
    println("Discovered latest release version: " + moduleVersion)
    moduleVersion
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

            afterEvaluate {
                // Classpath when compiling protos, we add dependency management directly
                // since it doesn't follow Gradle conventions of naming / properties.
                dependencies {
                    add("compileProtoPath", platform(project(":dependencyManagement")))
                    add("testCompileProtoPath", platform(project(":dependencyManagement")))
                }
            }
        }

        plugins.withId("ru.vyarus.animalsniffer") {
            dependencies {
                add(AnimalSnifferPlugin.SIGNATURE_CONF, "com.toasttab.android:gummy-bears-api-21:0.3.0:coreLib@signature")
            }

            configure<AnimalSnifferExtension> {
                sourceSets = listOf(the<JavaPluginConvention>().sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME))
            }
        }

        plugins.withId("me.champeau.gradle.japicmp") {
            afterEvaluate {
                tasks {
                    val jApiCmp by registering(JapicmpTask::class) {
                        dependsOn("jar")
                        // the japicmp "old" version is either the user-specified one, or the latest release.
                        val userRequestedBase = project.properties["apiBaseVersion"] as String?
                        val baselineVersion: String = userRequestedBase ?: latestReleasedVersion
                        val baselineArtifact: File = project.findArtifact(baselineVersion)
                        oldClasspath = files(baselineArtifact)

                        // the japicmp "new" version is either the user-specified one, or the locally built jar.
                        val newVersion: String? = project.properties["apiNewVersion"] as String?
                        val newArtifact: File = if (newVersion == null) {
                            val jar = getByName("jar") as Jar
                            file(jar.archiveFile)
                        } else {
                            project.findArtifact(newVersion)
                        }
                        newClasspath = files(newArtifact)

                        //only output changes, not everything
                        isOnlyModified = true
                        //this is needed so that we only consider the current artifact, and not dependencies
                        isIgnoreMissingClasses = true
                        // double wildcards don't seem to work here (*.internal.*)
                        packageExcludes = listOf("*.internal", "io.opentelemetry.internal.shaded.jctools.*")
                        if (newVersion == null) {
                            val baseVersionString = if (userRequestedBase == null) "latest" else baselineVersion
                            txtOutputFile = file("$rootDir/docs/apidiffs/current_vs_${baseVersionString}/${project.base.archivesBaseName}.txt")
                        } else {
                            txtOutputFile = file("$rootDir/docs/apidiffs/${newVersion}_vs_${baselineVersion}/${project.base.archivesBaseName}.txt")
                        }
                    }
                    // have the check task depend on the api comparison task, to make it more likely it will get used.
                    named("check") {
                        dependsOn(jApiCmp)
                    }
                }
            }
        }
    }

    plugins.withId("maven-publish") {
        // generate the api diff report for any module that is stable and publishes a jar.
        if (!project.hasProperty("otel.release") && !project.name.startsWith("bom")) {
            plugins.apply("me.champeau.gradle.japicmp")
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
