/*
 * Copyright 2020, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
