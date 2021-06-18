import de.undercouch.gradle.tasks.download.Download
import de.undercouch.gradle.tasks.download.Verify

plugins {
    id("otel.protobuf-conventions")
    id("maven-publish")

    id("de.undercouch.download")
    id("ru.vyarus.animalsniffer")
}

description = "OpenTelemetry Proto"
otelJava.moduleName.set("io.opentelemetry.proto")

dependencies {
    api("com.google.protobuf:protobuf-java")
    api("io.grpc:grpc-api")
    api("io.grpc:grpc-protobuf")
    api("io.grpc:grpc-stub")
}

val protoVersion = "0.9.0"
// To generate checksum, download the file and run "shasum -a 256 ~/path/to/vfoo.zip"
val protoChecksum = "5e4131064e9471eb09294374db0d55028fdb73898b08aa07a835d17d61e5f017"
val protoArchive = file("$buildDir/archives/opentelemetry-proto-${protoVersion}.zip")

tasks {
    val downloadProtoArchive by registering(Download::class) {
        onlyIf { !protoArchive.exists() }
        src("https://github.com/open-telemetry/opentelemetry-proto/archive/v${protoVersion}.zip")
        dest(protoArchive)
    }

    val verifyProtoArchive by registering(Verify::class) {
        dependsOn(downloadProtoArchive)
        src(protoArchive)
        algorithm("SHA-256")
        checksum(protoChecksum)
    }

    val unzipProtoArchive by registering(Copy::class) {
        dependsOn(verifyProtoArchive)
        from(zipTree(protoArchive))
        into("$buildDir/protos")
    }

    afterEvaluate {
        named("generateProto") {
            dependsOn(unzipProtoArchive)
        }
    }
}

sourceSets {
    main {
        proto {
            srcDir("$buildDir/protos/opentelemetry-proto-${protoVersion}")
        }
    }
}

// IntelliJ complains that the generated classes are not found, ask IntelliJ to include the
// generated Java directories as source folders.
idea {
    module {
        sourceDirs.add(file("build/generated/source/proto/main/java"))
        // If you have additional sourceSets and/or codegen plugins, add all of them
    }
}
