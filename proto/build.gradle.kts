import de.undercouch.gradle.tasks.download.Download
import de.undercouch.gradle.tasks.download.Verify

plugins {
    id("java-library")
    id("maven-publish")

    id("com.google.protobuf")
    id("de.undercouch.download")
    id("ru.vyarus.animalsniffer")
}

description = "OpenTelemetry Proto"
extra["moduleName"] = "io.opentelemetry.proto"

dependencies {
    api("com.google.protobuf:protobuf-java")
    api("io.grpc:grpc-api")
    api("io.grpc:grpc-protobuf")
    api("io.grpc:grpc-stub")
}

val protoVersion = "0.7.0"
// To generate checksum, download the file and run "shasum -a 256 ~/path/to/vfoo.zip"
val protoChecksum = "0b581c654b2360485b99c2de3731dd59275b0fe7b91d78e7f6c5efd5997f4c82"
val protoArchivePath = "$buildDir/archives/opentelemetry-proto-${protoVersion}.zip"

tasks {
    val downloadProtoArchive by registering(Download::class) {
        onlyIf { !file(protoArchivePath).exists() }
        src("https://github.com/open-telemetry/opentelemetry-proto/archive/v${protoVersion}.zip")
        dest(protoArchivePath)
    }

    val verifyProtoArchive by registering(Verify::class) {
        dependsOn(downloadProtoArchive)
        src(protoArchivePath)
        algorithm("SHA-256")
        checksum(protoChecksum)
    }

    val unzipProtoArchive by registering(Copy::class) {
        dependsOn(verifyProtoArchive)
        from(zipTree(protoArchivePath))
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
