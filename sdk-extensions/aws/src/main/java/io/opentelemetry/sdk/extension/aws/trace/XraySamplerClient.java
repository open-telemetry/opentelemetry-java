/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.aws.trace;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.sdk.extension.aws.internal.JdkHttpClient;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.Map;

final class XraySamplerClient {

  private static final ObjectMapper OBJECT_MAPPER =
      new ObjectMapper()
          .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
          // In case API is extended with new fields.
          .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, /* state= */ false);

  private static final Map<String, String> JSON_CONTENT_TYPE =
      Collections.singletonMap("Content-Type", "application/json");

  private final String getSamplingRulesEndpoint;
  private final JdkHttpClient httpClient;

  XraySamplerClient(String host) {
    this.getSamplingRulesEndpoint = host + "/GetSamplingRules";
    httpClient = new JdkHttpClient();
  }

  GetSamplingRulesResponse getSamplingRules(GetSamplingRulesRequest request) {
    final byte[] requestBody;
    try {
      requestBody = OBJECT_MAPPER.writeValueAsBytes(request);
    } catch (JsonProcessingException e) {
      throw new UncheckedIOException("Failed to serialize request.", e);
    }

    String response =
        httpClient.fetchString(
            "POST", getSamplingRulesEndpoint, JSON_CONTENT_TYPE, null, requestBody);

    try {
      return OBJECT_MAPPER.readValue(response, GetSamplingRulesResponse.class);
    } catch (JsonProcessingException e) {
      throw new UncheckedIOException("Failed to deserialize response.", e);
    }
  }
}
