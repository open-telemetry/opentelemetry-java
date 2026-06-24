/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import com.google.common.reflect.ClassPath;
import java.io.IOException;
import java.util.Comparator;
import java.util.stream.Stream;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Verifies the {@code equals}/{@code hashCode} contract for every generated model POJO. The models
 * and their {@code equals}/{@code hashCode} implementations are generated (see {@code
 * io.opentelemetry.gradle.js2p.OtelObjectRule}), so this test guards the generator output rather
 * than any hand-written code.
 */
class ModelEqualsHashCodeTest {

  private static final String MODEL_PACKAGE =
      "io.opentelemetry.sdk.autoconfigure.declarativeconfig.model";

  @ParameterizedTest
  @MethodSource("modelClasses")
  void equalsHashCode(Class<?> modelClass) {
    EqualsVerifier.forClass(modelClass)
        // Generated POJOs are mutable (non-final fields) and non-final with an instanceof-based
        // equals, neither of which EqualsVerifier accepts by default.
        .suppress(Warning.NONFINAL_FIELDS, Warning.STRICT_INHERITANCE)
        // Several models hold "oneOf" sub-models that are empty (e.g. ConsoleExporterModel), whose
        // equals is instanceof-only. EqualsVerifier can't produce two unequal instances of such a
        // type, so it can't prove the enclosing field is significant. The full equals/hashCode
        // contract is still verified.
        .suppress(Warning.ALL_FIELDS_SHOULD_BE_USED)
        // Break the self-referential sampler graphs (e.g. parent-based and jaeger-remote samplers
        // contain a nested SamplerModel; composable parent-threshold samplers contain a root
        // sampler). Prefab values only need to be unequal to each other.
        .withPrefabValues(
            SamplerModel.class,
            new SamplerModel(),
            new SamplerModel().withAlwaysOn(new AlwaysOnSamplerModel()))
        .withPrefabValues(
            ExperimentalComposableSamplerModel.class,
            new ExperimentalComposableSamplerModel(),
            new ExperimentalComposableSamplerModel()
                .withAlwaysOff(new ExperimentalComposableAlwaysOffSamplerModel()))
        .withPrefabValues(
            ExperimentalComposableParentThresholdSamplerModel.class,
            new ExperimentalComposableParentThresholdSamplerModel(),
            new ExperimentalComposableParentThresholdSamplerModel()
                .withRoot(new ExperimentalComposableSamplerModel()))
        .verify();
  }

  static Stream<Arguments> modelClasses() throws IOException {
    return ClassPath.from(ModelEqualsHashCodeTest.class.getClassLoader())
        .getTopLevelClasses(MODEL_PACKAGE)
        .stream()
        .map(ClassPath.ClassInfo::load)
        // The "Model" suffix (jsonschema2pojo classNameSuffix) selects the generated POJOs and
        // excludes generated enums and this test class, which share the package.
        .filter(clazz -> clazz.getSimpleName().endsWith("Model"))
        .sorted(Comparator.comparing(Class::getSimpleName))
        .map(clazz -> Arguments.argumentSet(clazz.getSimpleName(), clazz));
  }
}
