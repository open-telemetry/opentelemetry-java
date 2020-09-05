/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.syntax.elements.ClassesShouldConjunction;
import org.junit.jupiter.api.Test;

class InternalApiProtectionTest {

  private static final String OTEL_BASE_PACKAGE = "io.opentelemetry";
  private static final JavaClasses ALL_OTEL_CLASSES =
      new ClassFileImporter().importPackages(OTEL_BASE_PACKAGE);

  @Test
  void contrib_should_not_use_internal_api() {
    ClassesShouldConjunction contribRule =
        noClasses()
            .that()
            .resideInAPackage("..extensions..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage(OTEL_BASE_PACKAGE + ".internal");

    contribRule.check(ALL_OTEL_CLASSES);
  }
}
