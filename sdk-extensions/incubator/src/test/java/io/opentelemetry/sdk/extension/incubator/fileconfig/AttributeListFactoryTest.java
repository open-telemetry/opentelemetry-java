/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.config.DeclarativeConfigException;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.AttributeNameValueModel;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class AttributeListFactoryTest {

  @ParameterizedTest
  @MethodSource("invalidAttributes")
  void create_InvalidAttributes(List<AttributeNameValueModel> model, String expectedMessage) {
    assertThatThrownBy(
            () ->
                AttributeListFactory.getInstance()
                    .create(model, mock(DeclarativeConfigContext.class)))
        .isInstanceOf(DeclarativeConfigException.class)
        .hasMessageContaining(expectedMessage);
  }

  private static Stream<Arguments> invalidAttributes() {
    return Stream.of(
        Arguments.of(
            Collections.singletonList(new AttributeNameValueModel().withName("key")),
            "attribute value is required but is null"),
        Arguments.of(
            Collections.singletonList(
                new AttributeNameValueModel().withName("key").withValue(new Object())),
            "Error processing attribute with name \"key\": value did not match type STRING"),
        Arguments.of(
            Collections.singletonList(
                new AttributeNameValueModel()
                    .withName("key")
                    .withType(AttributeNameValueModel.AttributeType.INT)
                    .withValue(Arrays.asList(1L, 1))),
            "Error processing attribute with name \"key\": value did not match type INT"),
        Arguments.of(
            Collections.singletonList(
                new AttributeNameValueModel()
                    .withName("key")
                    .withType(AttributeNameValueModel.AttributeType.INT)
                    .withValue(true)),
            "Error processing attribute with name \"key\": value did not match type INT"));
  }

  @Test
  void create() {
    Attributes expectedAttributes =
        Attributes.builder()
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
            .build();
    assertThat(
            AttributeListFactory.getInstance()
                .create(
                    Arrays.asList(
                        new AttributeNameValueModel()
                            .withName("service.name")
                            .withValue("my-service"),
                        new AttributeNameValueModel()
                            .withName("strKey")
                            .withValue("val")
                            .withType(AttributeNameValueModel.AttributeType.STRING),
                        new AttributeNameValueModel()
                            .withName("longKey")
                            .withValue(1L)
                            .withType(AttributeNameValueModel.AttributeType.INT),
                        new AttributeNameValueModel()
                            .withName("intKey")
                            .withValue(2)
                            .withType(AttributeNameValueModel.AttributeType.INT),
                        new AttributeNameValueModel()
                            .withName("doubleKey")
                            .withValue(1.0d)
                            .withType(AttributeNameValueModel.AttributeType.DOUBLE),
                        new AttributeNameValueModel()
                            .withName("floatKey")
                            .withValue(2.0f)
                            .withType(AttributeNameValueModel.AttributeType.DOUBLE),
                        new AttributeNameValueModel()
                            .withName("boolKey")
                            .withValue(true)
                            .withType(AttributeNameValueModel.AttributeType.BOOL),
                        new AttributeNameValueModel()
                            .withName("strArrKey")
                            .withValue(Arrays.asList("val1", "val2"))
                            .withType(AttributeNameValueModel.AttributeType.STRING_ARRAY),
                        new AttributeNameValueModel()
                            .withName("longArrKey")
                            .withValue(Arrays.asList(1L, 2L))
                            .withType(AttributeNameValueModel.AttributeType.INT_ARRAY),
                        new AttributeNameValueModel()
                            .withName("intArrKey")
                            .withValue(Arrays.asList(1, 2))
                            .withType(AttributeNameValueModel.AttributeType.INT_ARRAY),
                        new AttributeNameValueModel()
                            .withName("doubleArrKey")
                            .withValue(Arrays.asList(1.0d, 2.0d))
                            .withType(AttributeNameValueModel.AttributeType.DOUBLE_ARRAY),
                        new AttributeNameValueModel()
                            .withName("floatArrKey")
                            .withValue(Arrays.asList(1.0f, 2.0f))
                            .withType(AttributeNameValueModel.AttributeType.DOUBLE_ARRAY),
                        new AttributeNameValueModel()
                            .withName("boolArrKey")
                            .withValue(Arrays.asList(true, false))
                            .withType(AttributeNameValueModel.AttributeType.BOOL_ARRAY)),
                    mock(DeclarativeConfigContext.class)))
        .isEqualTo(expectedAttributes);
  }
}
