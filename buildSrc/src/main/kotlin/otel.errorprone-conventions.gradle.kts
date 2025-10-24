import net.ltgt.gradle.errorprone.CheckSeverity
import net.ltgt.gradle.errorprone.errorprone
import net.ltgt.gradle.nullaway.nullaway

plugins {
  id("net.ltgt.errorprone")
  id("net.ltgt.nullaway")
}

dependencies {
  errorprone("com.google.errorprone:error_prone_core")
  errorprone("com.uber.nullaway:nullaway")
  errorprone(project(":custom-checks"))
}

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

        // Ignore warnings for generated and vendored classes
        excludedPaths.set(".*/build/generated/.*|.*/internal/shaded/.*")

        // Still Java 8
        disable("Varifier")

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

        // We use animal sniffer
        disable("Java8ApiChecker")

        // apparently disabling android doesn't disable this
        disable("StaticOrDefaultInterfaceMethod")

        // Limits APIs
        disable("PreferredInterfaceType")

        // We don't depend on Guava so use normal splitting
        disable("StringSplitter")

        // Prevents lazy initialization
        disable("InitializeInline")

        // Seems to trigger even when a deprecated method isn't called anywhere.
        // We don't get much benefit from it anyways.
        disable("InlineMeSuggester")

        // We have nullaway so don't need errorprone nullable checks which have more false positives.
        disable("FieldMissingNullable")
        disable("ParameterMissingNullable")
        disable("ReturnMissingNullable")
        disable("VoidMissingNullable")

        // Only used in comments, but couldn't SuppressWarnings for some reason
        disable("UnicodeEscape")

        // We make liberal use of builders returning "this" which can be safely ignored. Annotating
        // every one of these methods is too noisy.
        disable("CanIgnoreReturnValueSuggester")

        // YodaConditions may improve safety in some cases. The argument of increased
        // cognitive load is dubious.
        disable("YodaCondition")

        // We annotate packages with @ParametersAreNonnullByDefault from com.google.code.findbugs:jsr305.
        // @NullMarked is the equivalent from jspecify.
        disable("AddNullMarkedToPackageInfo")

        if ((name.contains("Jmh") || name.contains("Test") || project.name.contains("testing-internal")) && !project.name.equals("custom-checks")) {
          // Allow underscore in test-type method names
          disable("MemberName")
          // Internal javadoc not needed for test or jmh classes
          disable("OtelInternalJavadoc")
        }

        option("NullAway:CustomContractAnnotations", "io.opentelemetry.api.internal.Contract")
      }

      with(options) {
        errorprone.nullaway {
          annotatedPackages.add("io.opentelemetry")
          // Disable nullaway by default, we enable for main sources below.
          severity.set(CheckSeverity.OFF)
        }
      }
    }
  }

  // Enable nullaway on main sources.
  named<JavaCompile>("compileJava") {
    with(options) {
      errorprone.nullaway {
        severity.set(CheckSeverity.ERROR)
      }
    }
  }
}
