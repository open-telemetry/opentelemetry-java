import com.google.protobuf.gradle.*

plugins {
    id("com.google.protobuf")

    id("otel.java-conventions")
}

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
