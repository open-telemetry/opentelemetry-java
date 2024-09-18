/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.AttributesModel;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class AttributesFactoryTest {

  @ParameterizedTest
  @MethodSource("invalidAttributes")
  void create_InvalidAttributes(AttributesModel model, String expectedMessage) {
    assertThatThrownBy(
            () ->
                AttributesFactory.getInstance()
                    .create(model, mock(SpiHelper.class), Collections.emptyList()))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining(expectedMessage);
  }

  private static Stream<Arguments> invalidAttributes() {
    return Stream.of(
        Arguments.of(
            new AttributesModel().withAdditionalProperty("key", null),
            "Error processing attribute with key \"key\": unexpected null value"),
        Arguments.of(
            new AttributesModel().withAdditionalProperty("key", new Object()),
            "Error processing attribute with key \"key\": unrecognized value type java.lang.Object"),
        Arguments.of(
            new AttributesModel().withAdditionalProperty("key", Arrays.asList(1L, 1)),
            "Error processing attribute with key \"key\": expected value entries to be of type class java.lang.Long but found entry with type class java.lang.Integer"),
        Arguments.of(
            new AttributesModel().withAdditionalProperty("key", Arrays.asList(1L, null)),
            "Error processing attribute with key \"key\": unexpected null element in value"));
  }

  @Test
  void create() {
    assertThat(
            AttributesFactory.getInstance()
                .create(
                    new AttributesModel()
                        .withServiceName("my-service")
                        .withAdditionalProperty("strKey", "val")
                        .withAdditionalProperty("longKey", 1L)
                        .withAdditionalProperty("intKey", 2)
                        .withAdditionalProperty("doubleKey", 1.0d)
                        .withAdditionalProperty("floatKey", 2.0f)
                        .withAdditionalProperty("boolKey", true)
                        .withAdditionalProperty("strArrKey", Arrays.asList("val1", "val2"))
                        .withAdditionalProperty("longArrKey", Arrays.asList(1L, 2L))
                        .withAdditionalProperty("intArrKey", Arrays.asList(1, 2))
                        .withAdditionalProperty("doubleArrKey", Arrays.asList(1.0d, 2.0d))
                        .withAdditionalProperty("floatArrKey", Arrays.asList(1.0f, 2.0f))
                        .withAdditionalProperty("boolArrKey", Arrays.asList(true, false))
                        .withAdditionalProperty("emptyArrKey", Collections.emptyList()),
                    mock(SpiHelper.class),
                    Collections.emptyList()))
        .isEqualTo(
            io.opentelemetry.api.common.Attributes.builder()
                .put("service.name", "my-service")
                .put("strKey", "val")
                .put("longKey", 1L)
                .put("intKey", 2)
                .put("doubleKey", 1.0d)
                .put("floatKey", 2.0f)
                .put("boolKey", true)
                .put("strArrKey", "val1", "val2")
                .put("longArrKey", 1L, 2L)
                .put("intArrKey", 1, 2)
                .put("doubleArrKey", 1.0d, 2.0d)
                .put("floatArrKey", 1.0f, 2.0f)
                .put("boolArrKey", true, false)
                .build());
  }
}
