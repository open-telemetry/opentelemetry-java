/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.all;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.syntax.elements.ClassesShouldConjunction;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class NoSharedInternalCodeTest {

  private static final Set<String> exemptions =
      Set.of(
          "opentelemetry-api-incubator",
          "opentelemetry-exporter-common",
          "opentelemetry-exporter-logging",
          "opentelemetry-exporter-logging-otlp",
          "opentelemetry-exporter-prometheus",
          "opentelemetry-exporter-zipkin",
          "opentelemetry-extension-trace-propagators",
          "opentelemetry-opencensus-shim",
          "opentelemetry-sdk-common",
          "opentelemetry-sdk-logs",
          "opentelemetry-sdk-metrics",
          "opentelemetry-sdk-testing",
          "opentelemetry-sdk-trace",
          "opentelemetry-sdk-extension-autoconfigure",
          "opentelemetry-sdk-extension-autoconfigure-spi",
          "opentelemetry-sdk-extension-incubator",
          "opentelemetry-sdk-extension-jaeger-remote-sampler",
          "opentelemetry-exporter-otlp",
          "opentelemetry-exporter-otlp-common",
          "opentelemetry-exporter-sender-grpc-managed-channel",
          "opentelemetry-exporter-sender-jdk",
          "opentelemetry-exporter-sender-okhttp",
          "opentelemetry-exporter-sender-okhttp4");

  private static final String OTEL_BASE_PACKAGE = "io.opentelemetry";
  private static final Logger logger = Logger.getLogger(NoSharedInternalCodeTest.class.getName());

  @ParameterizedTest
  @MethodSource("artifactsAndJars")
  void noSharedInternalCode(String artifactId, String absolutePath) throws IOException {
    try (JarFile jarFile = new JarFile(new File(absolutePath))) {
      JavaClasses artifactClasses = new ClassFileImporter().importJar(jarFile);

      Set<String> artifactOtelPackages =
          artifactClasses.stream()
              .map(JavaClass::getPackageName)
              .filter(packageName -> packageName.startsWith(OTEL_BASE_PACKAGE))
              .collect(Collectors.toSet());

      ClassesShouldConjunction noSharedInternalCodeRule =
          noClasses()
              .that()
              .resideInAnyPackage(artifactOtelPackages.toArray(new String[0]))
              .should()
              .dependOnClassesThat(
                  new DescribedPredicate<>(
                      "are in internal modules of other opentelemetry artifacts") {
                    @Override
                    public boolean test(JavaClass javaClass) {
                      String packageName = javaClass.getPackageName();
                      return packageName.startsWith(OTEL_BASE_PACKAGE)
                          && packageName.contains(".internal")
                          && !artifactOtelPackages.contains(packageName);
                    }
                  });

      try {
        noSharedInternalCodeRule
            .as(artifactId + " should not use internal code from other artifacts")
            .check(artifactClasses);
        // To view artifacts which do not contain shared internal code, change test log level or
        // increase log level of this statement to WARNING
        logger.log(Level.INFO, artifactId + " does not contain shared internal code");
      } catch (AssertionError e) {
        if (exemptions.contains(artifactId)) {
          // To view details, remove from exemptions list
          logger.log(
              Level.WARNING,
              artifactId + " contains shared internal code but is temporarily exempt");
        } else {
          throw e;
        }
      }
    }
  }

  private static Stream<Arguments> artifactsAndJars() throws IOException {
    List<String> lines = Files.readAllLines(Path.of(System.getenv("ARTIFACTS_AND_JARS")));
    return lines.stream()
        .map(
            line -> {
              String[] parts = line.split(":", 2);
              return Arguments.of(parts[0], parts[1]);
            });
  }
}
