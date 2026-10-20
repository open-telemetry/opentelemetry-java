/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.resources.internal.Entity;
import io.opentelemetry.sdk.resources.internal.EntityUtil;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.ClearSystemProperty;
import org.junitpioneer.jupiter.SetSystemProperty;

class EntitiesEnvResourceDetectorTest {

  @Test
  void getTypeAndName() {
    EntitiesEnvResourceDetector detector = new EntitiesEnvResourceDetector();

    assertThat(detector.getType()).isEqualTo(Resource.class);
    assertThat(detector.getName()).isEqualTo("env");
  }

  @Test
  @SetSystemProperty(
      key = "otel.entities",
      value = "process{process.pid=1234}[process.executable.name=java]@http://schema")
  void create_SystemPropertySet() {
    Resource resource =
        new EntitiesEnvResourceDetector().create(DeclarativeConfigProperties.empty());

    assertThat(EntityUtil.getEntities(resource))
        .containsExactly(
            Entity.builder("process", Attributes.of(stringKey("process.pid"), "1234"))
                .setSchemaUrl("http://schema")
                .setDescription(Attributes.of(stringKey("process.executable.name"), "java"))
                .build());
  }

  @Test
  @ClearSystemProperty(key = "otel.entities")
  void create_NoSystemProperty() {
    assertThat(new EntitiesEnvResourceDetector().create(DeclarativeConfigProperties.empty()))
        .isEqualTo(Resource.empty());
  }
}
