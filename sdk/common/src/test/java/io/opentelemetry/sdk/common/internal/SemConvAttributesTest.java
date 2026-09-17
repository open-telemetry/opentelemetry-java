/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common.internal;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.semconv.ErrorAttributes;
import io.opentelemetry.semconv.HttpAttributes;
import io.opentelemetry.semconv.SchemaUrls;
import io.opentelemetry.semconv.ServerAttributes;
import io.opentelemetry.semconv.ServiceAttributes;
import io.opentelemetry.semconv.incubating.OtelIncubatingAttributes;
import io.opentelemetry.semconv.incubating.RpcIncubatingAttributes;
import org.junit.jupiter.api.Test;

class SemConvAttributesTest {

  @Test
  void testAttributeKeys() {
    assertThat(SemConvAttributes.SCHEMA_URL_V1_40_0).isEqualTo(SchemaUrls.V1_40_0);

    // TODO(jack-berg): assert against generated constants once we start generating for entities
    // types
    assertThat(SemConvAttributes.SERVICE_TYPE).isEqualTo("service");
    assertThat(SemConvAttributes.SERVICE_INSTANCE_TYPE).isEqualTo("service.instance");

    assertThat(SemConvAttributes.SERVICE_NAME).isEqualTo(ServiceAttributes.SERVICE_NAME);
    assertThat(SemConvAttributes.SERVICE_INSTANCE_ID)
        .isEqualTo(ServiceAttributes.SERVICE_INSTANCE_ID);

    assertThat(SemConvAttributes.OTEL_COMPONENT_NAME)
        .isEqualTo(OtelIncubatingAttributes.OTEL_COMPONENT_NAME);
    assertThat(SemConvAttributes.OTEL_COMPONENT_TYPE)
        .isEqualTo(OtelIncubatingAttributes.OTEL_COMPONENT_TYPE);

    assertThat(SemConvAttributes.ERROR_TYPE).isEqualTo(ErrorAttributes.ERROR_TYPE);

    assertThat(SemConvAttributes.SERVER_ADDRESS).isEqualTo(ServerAttributes.SERVER_ADDRESS);
    assertThat(SemConvAttributes.SERVER_PORT).isEqualTo(ServerAttributes.SERVER_PORT);
    assertThat(SemConvAttributes.RPC_RESPONSE_STATUS_CODE)
        .isEqualTo(RpcIncubatingAttributes.RPC_RESPONSE_STATUS_CODE);
    assertThat(SemConvAttributes.HTTP_RESPONSE_STATUS_CODE)
        .isEqualTo(HttpAttributes.HTTP_RESPONSE_STATUS_CODE);

    assertThat(SemConvAttributes.OTEL_SPAN_PARENT_ORIGIN)
        .isEqualTo(OtelIncubatingAttributes.OTEL_SPAN_PARENT_ORIGIN);
    assertThat(SemConvAttributes.OTEL_SPAN_SAMPLING_RESULT)
        .isEqualTo(OtelIncubatingAttributes.OTEL_SPAN_SAMPLING_RESULT);
  }
}
