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

  private static final String OTEL_BASE_PACKAGE = "io.opentelemetry";
  private static final Logger logger = Logger.getLogger(NoSharedInternalCodeTest.class.getName());

  @ParameterizedTest
  @MethodSource("artifactsAndJars")
  void noSharedInternalCode(String artifactId, String absolutePath) throws IOException {
    JavaClasses artifactClasses =
        new ClassFileImporter().importJar(new JarFile(new File(absolutePath)));

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

    // TODO: when all shared internal code is removed, remove the catch block and fail when detected
    try {
      noSharedInternalCodeRule.check(artifactClasses);
    } catch (AssertionError e) {
      logger.log(
          Level.WARNING,
          "Internal shared code detected for: " + artifactId + "\n" + e.getMessage() + "\n");
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
