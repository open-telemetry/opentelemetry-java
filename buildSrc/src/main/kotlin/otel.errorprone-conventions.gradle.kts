import net.ltgt.gradle.errorprone.CheckSeverity
import net.ltgt.gradle.errorprone.errorprone
import net.ltgt.gradle.nullaway.nullaway

plugins {
    id("net.ltgt.errorprone")
    id("net.ltgt.nullaway")
}

val enableNullaway: String? by project

val disableErrorProne = properties["disableErrorProne"]?.toString()?.toBoolean() ?: false

tasks {
    withType<JavaCompile>().configureEach {
        with(options) {
            errorprone {
                if (disableErrorProne) {
                    logger.warn("Errorprone has been disabled. Build may not result in a valid PR build.")
                    isEnabled.set(false)
                }

                disableWarningsInGeneratedCode.set(true)
                allDisabledChecksAsWarnings.set(true)

                // Doesn't currently use Var annotations.
                disable("Var") // "-Xep:Var:OFF"

                // ImmutableRefactoring suggests using com.google.errorprone.annotations.Immutable,
                // but currently uses javax.annotation.concurrent.Immutable
                disable("ImmutableRefactoring")

                // AutoValueImmutableFields suggests returning Guava types from API methods
                disable("AutoValueImmutableFields")
                // Suggests using Guava types for fields but we don't use Guava
                disable("ImmutableMemberCollection")

                // Fully qualified names may be necessary when deprecating a class to avoid
                // deprecation warning.
                disable("UnnecessarilyFullyQualified")

                // Ignore warnings for protobuf and jmh generated files.
                excludedPaths.set(".*generated.*|.*internal.shaded.*")

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
}
