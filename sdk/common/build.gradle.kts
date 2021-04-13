plugins {
    id("java-library")
    id("maven-publish")

    id("ru.vyarus.animalsniffer")
    id("org.unbroken-dome.test-sets")
}

description = "OpenTelemetry SDK Common"
extra["moduleName"] = "io.opentelemetry.sdk.common"

val mrJarVersions = listOf(9)

testSets {
    create("testResourceDisabledByProperty")
    create("testResourceDisabledByEnv")
}

dependencies {
    api(project(":api:all"))

    implementation(project(":semconv"))

    annotationProcessor("com.google.auto.value:auto-value")

    testAnnotationProcessor("com.google.auto.value:auto-value")

    testImplementation(project(":sdk:testing"))
    testImplementation(project(":sdk-extensions:resources"))
    testImplementation("com.google.guava:guava-testlib")
}

sourceSets {
    main {
        output.dir("build/generated/properties", "builtBy" to "generateVersionResource")
    }
}

tasks {
    register("generateVersionResource") {
        val propertiesDir = file("build/generated/properties/io/opentelemetry/sdk/common")
        outputs.dir(propertiesDir)

        doLast {
            File(propertiesDir, "version.properties").writeText("sdk.version=${project.version}")
        }
    }
}

for (version in mrJarVersions) {
    sourceSets {
        create("java${version}") {
            java {
                setSrcDirs(listOf("src/main/java${version}"))
            }
        }
    }

    tasks {
        named<JavaCompile>("compileJava${version}Java") {
            sourceCompatibility = "${version}"
            targetCompatibility = "${version}"
            options.release.set(version)
        }
    }

    configurations {
        named("java${version}Implementation") {
            extendsFrom(configurations["implementation"])
        }
    }

    dependencies {
        // Common to reference classes in main sourceset from Java 9 one (e.g., to return a common interface)
        add("java${version}Implementation", files(sourceSets.main.get().output.classesDirs))

        add("java${version}AnnotationProcessor", "com.uber.nullaway:nullaway")
    }
}

tasks {
    withType(Jar::class) {
        for (version in mrJarVersions) {
            into("META-INF/versions/${version}") {
                from(sourceSets["java${version}"].output)
            }
        }
        manifest.attributes(
                "Multi-Release" to "true"
        )
    }
}
