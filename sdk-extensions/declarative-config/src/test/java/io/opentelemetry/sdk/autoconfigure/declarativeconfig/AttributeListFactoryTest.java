/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.config.DeclarativeConfigException;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.AttributeNameValueModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.AttributeTypeModel;
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
        Arguments.argumentSet(
            "null value",
            Collections.singletonList(new AttributeNameValueModel().setName("key")),
            "attribute value is required but is null"),
        Arguments.argumentSet(
            "wrong type string",
            Collections.singletonList(
                new AttributeNameValueModel().setName("key").setValue(new Object())),
            "Error processing attribute with name \"key\": value did not match type STRING"),
        Arguments.argumentSet(
            "wrong type int list",
            Collections.singletonList(
                new AttributeNameValueModel()
                    .setName("key")
                    .setType(AttributeTypeModel.INT)
                    .setValue(Arrays.asList(1L, 1))),
            "Error processing attribute with name \"key\": value did not match type INT"),
        Arguments.argumentSet(
            "wrong type int boolean",
            Collections.singletonList(
                new AttributeNameValueModel()
                    .setName("key")
                    .setType(AttributeTypeModel.INT)
                    .setValue(true)),
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
                            .setName("service.name")
                            .setValue("my-service"),
                        new AttributeNameValueModel()
                            .setName("strKey")
                            .setValue("val")
                            .setType(AttributeTypeModel.STRING),
                        new AttributeNameValueModel()
                            .setName("longKey")
                            .setValue(1L)
                            .setType(AttributeTypeModel.INT),
                        new AttributeNameValueModel()
                            .setName("intKey")
                            .setValue(2)
                            .setType(AttributeTypeModel.INT),
                        new AttributeNameValueModel()
                            .setName("doubleKey")
                            .setValue(1.0d)
                            .setType(AttributeTypeModel.DOUBLE),
                        new AttributeNameValueModel()
                            .setName("floatKey")
                            .setValue(2.0f)
                            .setType(AttributeTypeModel.DOUBLE),
                        new AttributeNameValueModel()
                            .setName("boolKey")
                            .setValue(true)
                            .setType(AttributeTypeModel.BOOL),
                        new AttributeNameValueModel()
                            .setName("strArrKey")
                            .setValue(Arrays.asList("val1", "val2"))
                            .setType(AttributeTypeModel.STRING_ARRAY),
                        new AttributeNameValueModel()
                            .setName("longArrKey")
                            .setValue(Arrays.asList(1L, 2L))
                            .setType(AttributeTypeModel.INT_ARRAY),
                        new AttributeNameValueModel()
                            .setName("intArrKey")
                            .setValue(Arrays.asList(1, 2))
                            .setType(AttributeTypeModel.INT_ARRAY),
                        new AttributeNameValueModel()
                            .setName("doubleArrKey")
                            .setValue(Arrays.asList(1.0d, 2.0d))
                            .setType(AttributeTypeModel.DOUBLE_ARRAY),
                        new AttributeNameValueModel()
                            .setName("floatArrKey")
                            .setValue(Arrays.asList(1.0f, 2.0f))
                            .setType(AttributeTypeModel.DOUBLE_ARRAY),
                        new AttributeNameValueModel()
                            .setName("boolArrKey")
                            .setValue(Arrays.asList(true, false))
                            .setType(AttributeTypeModel.BOOL_ARRAY)),
                    mock(DeclarativeConfigContext.class)))
        .isEqualTo(expectedAttributes);
  }
}
