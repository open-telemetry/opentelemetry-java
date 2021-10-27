/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal.grpc;

import static org.assertj.core.api.Assertions.assertThatCode;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.grpc.ManagedChannelBuilder;
import io.opentelemetry.exporter.otlp.internal.RetryPolicy;
import java.util.Map;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

class ManagedChannelUtilTest {

  @Test
  void toServiceConfig() throws JSONException, JsonProcessingException {
    // Validate that the map matches the protobuf to JSON translation of the
    // grpc.service_config.ServiceConfig protobuf definition described at:
    // https://github.com/grpc/grpc/blob/master/doc/service_config.md
    Map<String, ?> serviceConfig =
        ManagedChannelUtil.toServiceConfig(
            "opentelemetry.proto.MyService", RetryPolicy.getDefault());
    String expectedServiceConfig =
        "{\n"
            + "  \"methodConfig\": [{\n"
            + "    \"retryPolicy\": {\n"
            + "      \"backoffMultiplier\": 1.5,\n"
            + "      \"maxAttempts\": 5.0,\n"
            + "      \"initialBackoff\": \"1.0s\",\n"
            + "      \"retryableStatusCodes\": [1.0, 4.0, 8.0, 10.0, 11.0, 14.0, 15.0],\n"
            + "      \"maxBackoff\": \"5.0s\"\n"
            + "    },\n"
            + "    \"name\": [{\n"
            + "      \"service\": \"opentelemetry.proto.MyService\"\n"
            + "    }]\n"
            + "  }]\n"
            + "}";
    JSONAssert.assertEquals(
        expectedServiceConfig, new ObjectMapper().writeValueAsString(serviceConfig), false);

    // Validate that the map format does not throw when passed to managed channel builder.
    // Any type mismatch will throw.
    ManagedChannelBuilder<?> builder = ManagedChannelBuilder.forTarget("localhost");
    assertThatCode(() -> builder.defaultServiceConfig(serviceConfig)).doesNotThrowAnyException();
  }
}
