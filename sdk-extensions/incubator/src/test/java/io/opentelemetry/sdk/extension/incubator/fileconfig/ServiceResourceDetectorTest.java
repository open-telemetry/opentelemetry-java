/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Objects;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.ClearSystemProperty;

class ServiceResourceDetectorTest {

  @Test
  void getTypeAndName() {
    ServiceResourceDetector detector = new ServiceResourceDetector();

    assertThat(detector.getType()).isEqualTo(Resource.class);
    assertThat(detector.getName()).isEqualTo("service");
  }

  @Test
  @ClearSystemProperty(key = "otel.service.name")
  void create_SystemPropertySet() {
    System.setProperty("otel.service.name", "test");

    assertThat(new ServiceResourceDetector().create(DeclarativeConfigProperties.empty()))
        .satisfies(
            resource -> {
              Attributes attributes = resource.getAttributes();
              assertThat(attributes.get(AttributeKey.stringKey("service.name"))).isEqualTo("test");
              assertThatCode(
                      () ->
                          UUID.fromString(
                              Objects.requireNonNull(
                                  attributes.get(AttributeKey.stringKey("service.instance.id")))))
                  .doesNotThrowAnyException();
            });
  }

  @Test
  void create_NoSystemProperty() {
    assertThat(new ServiceResourceDetector().create(DeclarativeConfigProperties.empty()))
        .satisfies(
            resource -> {
              Attributes attributes = resource.getAttributes();
              assertThat(attributes.get(AttributeKey.stringKey("service.name"))).isNull();
              assertThatCode(
                      () ->
                          UUID.fromString(
                              Objects.requireNonNull(
                                  attributes.get(AttributeKey.stringKey("service.instance.id")))))
                  .doesNotThrowAnyException();
            });
  }
}
