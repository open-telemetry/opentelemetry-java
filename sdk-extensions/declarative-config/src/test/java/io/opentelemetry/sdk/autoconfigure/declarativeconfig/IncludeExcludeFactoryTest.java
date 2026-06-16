/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.api.incubator.config.DeclarativeConfigException;
import io.opentelemetry.common.ComponentLoader;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.IncludeExcludeModel;
import io.opentelemetry.sdk.common.internal.IncludeExcludePredicate;
import java.util.Arrays;
import java.util.Collections;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class IncludeExcludeFactoryTest {

  private final DeclarativeConfigContext context =
      new DeclarativeConfigContext(ComponentLoader.forClassLoader(getClass().getClassLoader()));

  @ParameterizedTest
  @MethodSource("createArguments")
  void create(IncludeExcludeModel model, Predicate<String> expectedPredicate) {
    Predicate<String> predicate = IncludeExcludeFactory.getInstance().create(model, context);
    assertThat(predicate.toString()).isEqualTo(expectedPredicate.toString());
  }

  private static Stream<Arguments> createArguments() {
    return Stream.of(
        Arguments.argumentSet(
            "both null",
            new IncludeExcludeModel().withIncluded(null).withExcluded(null),
            IncludeExcludePredicate.createPatternMatching(null, null)),
        Arguments.argumentSet(
            "included present excluded null",
            new IncludeExcludeModel().withIncluded(Arrays.asList("foo", "bar")).withExcluded(null),
            IncludeExcludePredicate.createPatternMatching(Arrays.asList("foo", "bar"), null)),
        Arguments.argumentSet(
            "included null excluded present",
            new IncludeExcludeModel()
                .withIncluded(null)
                .withExcluded(Collections.singletonList("baz")),
            IncludeExcludePredicate.createPatternMatching(null, Collections.singletonList("baz"))),
        Arguments.argumentSet(
            "both present",
            new IncludeExcludeModel()
                .withIncluded(Arrays.asList("foo", "bar"))
                .withExcluded(Collections.singletonList("baz")),
            IncludeExcludePredicate.createPatternMatching(
                Arrays.asList("foo", "bar"), Collections.singletonList("baz"))));
  }

  @ParameterizedTest
  @MethodSource("createInvalidArguments")
  void createInvalid(IncludeExcludeModel model, String expectedMessage) {
    assertThatThrownBy(() -> IncludeExcludeFactory.getInstance().create(model, context))
        .isInstanceOf(DeclarativeConfigException.class)
        .hasMessage(expectedMessage);
  }

  private static Stream<Arguments> createInvalidArguments() {
    return Stream.of(
        Arguments.argumentSet(
            "included empty",
            new IncludeExcludeModel().withIncluded(Collections.emptyList()).withExcluded(null),
            "included must not be empty"),
        Arguments.argumentSet(
            "excluded empty",
            new IncludeExcludeModel().withIncluded(null).withExcluded(Collections.emptyList()),
            "excluded must not be empty"));
  }
}
