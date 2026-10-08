/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.resources.internal.Entity;
import io.opentelemetry.sdk.resources.internal.EntityUtil;
import java.util.Objects;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.ClearSystemProperty;
import org.junitpioneer.jupiter.SetSystemProperty;

class ServiceResourceDetectorTest {

  @Test
  void getTypeAndName() {
    ServiceResourceDetector detector = new ServiceResourceDetector();

    assertThat(detector.getType()).isEqualTo(Resource.class);
    assertThat(detector.getName()).isEqualTo("service");
  }

  @Test
  @SetSystemProperty(key = "otel.service.name", value = "test")
  void create_SystemPropertySet() {
    Resource resource = new ServiceResourceDetector().create(DeclarativeConfigProperties.empty());
    Attributes attributes = resource.getAttributes();
    assertThat(attributes.get(AttributeKey.stringKey("service.name"))).isEqualTo("test");
    String serviceInstanceId =
        Objects.requireNonNull(attributes.get(AttributeKey.stringKey("service.instance.id")));
    assertThatCode(() -> UUID.fromString(serviceInstanceId)).doesNotThrowAnyException();
    assertThat(EntityUtil.getEntities(resource))
        .containsExactlyInAnyOrder(
            Entity.builder("service", Attributes.of(AttributeKey.stringKey("service.name"), "test"))
                .setSchemaUrl("https://opentelemetry.io/schemas/1.40.0")
                .build(),
            Entity.builder(
                    "service.instance",
                    Attributes.of(AttributeKey.stringKey("service.instance.id"), serviceInstanceId))
                .setSchemaUrl("https://opentelemetry.io/schemas/1.40.0")
                .build());
  }

  @Test
  @ClearSystemProperty(key = "otel.service.name")
  void create_NoSystemProperty() {
    Resource resource = new ServiceResourceDetector().create(DeclarativeConfigProperties.empty());
    Attributes attributes = resource.getAttributes();
    assertThat(attributes.get(AttributeKey.stringKey("service.name"))).isNull();
    String serviceInstanceId =
        Objects.requireNonNull(attributes.get(AttributeKey.stringKey("service.instance.id")));
    assertThatCode(() -> UUID.fromString(serviceInstanceId)).doesNotThrowAnyException();
    assertThat(EntityUtil.getEntities(resource))
        .containsExactly(
            Entity.builder(
                    "service.instance",
                    Attributes.of(AttributeKey.stringKey("service.instance.id"), serviceInstanceId))
                .setSchemaUrl("https://opentelemetry.io/schemas/1.40.0")
                .build());
  }
}
