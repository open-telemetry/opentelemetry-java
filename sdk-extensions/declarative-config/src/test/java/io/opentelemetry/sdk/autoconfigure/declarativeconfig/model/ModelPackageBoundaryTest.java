/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.reflect.ClassPath;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/**
 * Guards the experimental/stable package boundary for the generated model types.
 *
 * <p>Experimental (unstable) types must live in {@code MODEL_PACKAGE.internal} (exempt from the
 * stability guarantees in {@code VERSIONING.md}) and stable types in {@code MODEL_PACKAGE}. The
 * generator enforces this by routing types whose name starts with {@code Experimental} into the
 * internal sub-package. This test fails if a schema update or rename ever breaks that invariant
 * &mdash; e.g. leaking a mutable experimental type into the stable public API.
 */
class ModelPackageBoundaryTest {

  private static final String MODEL_PACKAGE =
      "io.opentelemetry.sdk.autoconfigure.declarativeconfig.model";
  private static final String INTERNAL_PACKAGE = MODEL_PACKAGE + ".internal";
  private static final String EXPERIMENTAL_PREFIX = "Experimental";

  @Test
  void experimentalTypesAreExactlyTheInternalPackage() throws IOException {
    List<Class<?>> generated =
        ClassPath.from(ModelPackageBoundaryTest.class.getClassLoader())
            .getTopLevelClassesRecursive(MODEL_PACKAGE)
            .stream()
            .map(ClassPath.ClassInfo::load)
            // The model package also holds the hand-written tests; exclude them.
            .filter(clazz -> !clazz.getSimpleName().endsWith("Test"))
            .collect(Collectors.toList());

    // Guard against a vacuous pass if the scan finds nothing (e.g. wrong package or classpath).
    assertThat(generated).isNotEmpty();
    assertThat(generated)
        .filteredOn(clazz -> clazz.getPackage().getName().equals(INTERNAL_PACKAGE))
        .isNotEmpty();

    assertThat(generated)
        .allSatisfy(
            clazz -> {
              boolean inInternal = clazz.getPackage().getName().equals(INTERNAL_PACKAGE);
              boolean experimental = clazz.getSimpleName().startsWith(EXPERIMENTAL_PREFIX);
              assertThat(experimental)
                  .as(
                      "%s: experimental types (name starts with %s) must live in %s, all other "
                          + "types in %s",
                      clazz.getName(), EXPERIMENTAL_PREFIX, INTERNAL_PACKAGE, MODEL_PACKAGE)
                  .isEqualTo(inInternal);
            });
  }
}
