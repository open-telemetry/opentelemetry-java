/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.api.incubator.config.DeclarativeConfigException;
import io.opentelemetry.common.ComponentLoader;
import io.opentelemetry.internal.testing.CleanupExtension;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.component.IdGeneratorComponentProvider;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.IdGeneratorModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.RandomIdGeneratorModel;
import io.opentelemetry.sdk.trace.IdGenerator;
import java.util.Collections;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class IdGeneratorFactoryTest {

  @RegisterExtension CleanupExtension cleanup = new CleanupExtension();

  private static final DeclarativeConfigContext context =
      new DeclarativeConfigContext(
          ComponentLoader.forClassLoader(IdGeneratorFactoryTest.class.getClassLoader()));

  @BeforeEach
  void setup() {
    context.setBuilder(new DeclarativeConfigurationBuilder());
  }

  @ParameterizedTest
  @MethodSource("createTestCases")
  void create(IdGeneratorModel model, IdGenerator expectedIdGenerator) {
    IdGenerator idGenerator = IdGeneratorFactory.getInstance().create(model, context);
    assertThat(idGenerator.toString()).isEqualTo(expectedIdGenerator.toString());
  }

  private static Stream<Arguments> createTestCases() {
    return Stream.of(
        Arguments.argumentSet(
            "random id generator",
            new IdGeneratorModel().withRandom(new RandomIdGeneratorModel()),
            IdGenerator.random()),
        Arguments.argumentSet(
            "SPI id generator",
            new IdGeneratorModel().withExtensionProperty("test", null),
            IdGeneratorComponentProvider.TestIdGenerator.create()));
  }

  @ParameterizedTest
  @MethodSource("createInvalidTestCases")
  void create_Invalid(IdGeneratorModel model, String expectedMessage) {
    assertThatThrownBy(() -> IdGeneratorFactory.getInstance().create(model, context))
        .isInstanceOf(DeclarativeConfigException.class)
        .hasMessage(expectedMessage);
  }

  private static Stream<Arguments> createInvalidTestCases() {
    return Stream.of(
        Arguments.argumentSet(
            "unknown SPI id generator",
            new IdGeneratorModel()
                .withExtensionProperty("unknown_key", Collections.singletonMap("key1", "value")),
            "No component provider detected for io.opentelemetry.sdk.trace.IdGenerator with name \"unknown_key\"."));
  }
}
