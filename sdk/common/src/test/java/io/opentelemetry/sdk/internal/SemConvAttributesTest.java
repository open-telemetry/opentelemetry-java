package io.opentelemetry.sdk.internal;


import io.opentelemetry.semconv.ErrorAttributes;
import io.opentelemetry.semconv.HttpAttributes;
import io.opentelemetry.semconv.ServerAttributes;
import io.opentelemetry.semconv.incubating.OtelIncubatingAttributes;
import io.opentelemetry.semconv.incubating.RpcIncubatingAttributes;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SemConvAttributesTest {

  @Test
  void testAttributeKeys() {
    assertThat(SemConvAttributes.OTEL_COMPONENT_NAME).isEqualTo(OtelIncubatingAttributes.OTEL_COMPONENT_NAME);
    assertThat(SemConvAttributes.OTEL_COMPONENT_TYPE).isEqualTo(OtelIncubatingAttributes.OTEL_COMPONENT_TYPE);

    assertThat(SemConvAttributes.ERROR_TYPE).isEqualTo(ErrorAttributes.ERROR_TYPE);

    assertThat(SemConvAttributes.SERVER_ADDRESS).isEqualTo(ServerAttributes.SERVER_ADDRESS);
    assertThat(SemConvAttributes.SERVER_PORT).isEqualTo(ServerAttributes.SERVER_PORT);
    assertThat(SemConvAttributes.RPC_GRPC_STATUS_CODE).isEqualTo(RpcIncubatingAttributes.RPC_GRPC_STATUS_CODE);
    assertThat(SemConvAttributes.HTTP_RESPONSE_STATUS_CODE).isEqualTo(HttpAttributes.HTTP_RESPONSE_STATUS_CODE);
  }

}
