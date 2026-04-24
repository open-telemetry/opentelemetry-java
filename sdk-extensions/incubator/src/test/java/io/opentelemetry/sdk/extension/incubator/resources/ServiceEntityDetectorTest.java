/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.resources;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.extension.incubator.resources.internal.SdkEntity;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class ServiceEntityDetectorTest {

  @Test
  void testDetect() {
    ServiceEntityDetector detector = new ServiceEntityDetector();
    Collection<Entity> entities =
        detector.detect(
            DefaultConfigProperties.createFromMap(
                Collections.singletonMap("otel.service.name", "my-service")));

    assertThat(entities).hasSize(2);
    List<Entity> list = new ArrayList<>(entities);

    SdkEntity serviceEntity = (SdkEntity) list.get(0);
    assertThat(serviceEntity.getType()).isEqualTo("service");
    assertThat(serviceEntity.getIdentity())
        .isEqualTo(Attributes.builder().put("service.name", "my-service").build());
    assertThat(serviceEntity.getSchemaUrl()).isEqualTo("https://opentelemetry.io/schemas/1.40.0");

    SdkEntity serviceInstanceEntity = (SdkEntity) list.get(1);
    assertThat(serviceInstanceEntity.getType()).isEqualTo("service.instance");
    assertThat(serviceInstanceEntity.getIdentity().get(ServiceEntityDetector.SERVICE_INSTANCE_ID))
        .isNotNull()
        .isNotEmpty();
    assertThat(serviceInstanceEntity.getSchemaUrl())
        .isEqualTo("https://opentelemetry.io/schemas/1.40.0");

    // Verify that another call returns the same instance ID (static final RANDOM)
    Collection<Entity> entities2 =
        detector.detect(
            DefaultConfigProperties.createFromMap(
                Collections.singletonMap("otel.service.name", "my-service")));
    List<Entity> list2 = new ArrayList<>(entities2);
    SdkEntity serviceInstanceEntity2 = (SdkEntity) list2.get(1);
    assertThat(serviceInstanceEntity2.getIdentity().get(ServiceEntityDetector.SERVICE_INSTANCE_ID))
        .isEqualTo(
            serviceInstanceEntity.getIdentity().get(ServiceEntityDetector.SERVICE_INSTANCE_ID));
  }
}
