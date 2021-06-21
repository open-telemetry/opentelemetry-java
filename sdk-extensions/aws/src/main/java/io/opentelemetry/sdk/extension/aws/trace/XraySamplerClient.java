/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

// Includes work from:

/*
 * Copyright 2010-2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Portions copyright 2006-2009 James Murty. Please see LICENSE.txt
 * for applicable license terms and NOTICE.txt for applicable notices.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package io.opentelemetry.sdk.extension.aws.trace;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.opentelemetry.sdk.extension.aws.internal.JdkHttpClient;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

final class XraySamplerClient {

  private static final ObjectMapper OBJECT_MAPPER;

  static {
    OBJECT_MAPPER =
        new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
            // AWS APIs return timestamps as floats.
            .registerModule(
                new SimpleModule().addDeserializer(Date.class, new FloatDateDeserializer()))
            // In case API is extended with new fields.
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, /* state= */ false);
  }

  private static final Map<String, String> JSON_CONTENT_TYPE =
      Collections.singletonMap("Content-Type", "application/json");

  private final String getSamplingRulesEndpoint;
  private final String getSamplingTargetsEndpoint;
  private final JdkHttpClient httpClient;

  XraySamplerClient(String host) {
    this.getSamplingRulesEndpoint = host + "/GetSamplingRules";
    this.getSamplingTargetsEndpoint = host + "/SamplingTargets";
    httpClient = new JdkHttpClient();
  }

  GetSamplingRulesResponse getSamplingRules(GetSamplingRulesRequest request) {
    return executeJsonRequest(getSamplingRulesEndpoint, request, GetSamplingRulesResponse.class);
  }

  GetSamplingTargetsResponse getSamplingTargets(GetSamplingTargetsRequest request) {
    return executeJsonRequest(
        getSamplingTargetsEndpoint, request, GetSamplingTargetsResponse.class);
  }

  private <T> T executeJsonRequest(String endpoint, Object request, Class<T> responseType) {
    final byte[] requestBody;
    try {
      requestBody = OBJECT_MAPPER.writeValueAsBytes(request);
    } catch (JsonProcessingException e) {
      throw new UncheckedIOException("Failed to serialize request.", e);
    }

    String response =
        httpClient.fetchString("POST", endpoint, JSON_CONTENT_TYPE, null, requestBody);

    try {
      return OBJECT_MAPPER.readValue(response, responseType);
    } catch (JsonProcessingException e) {
      throw new UncheckedIOException("Failed to deserialize response.", e);
    }
  }

  @SuppressWarnings("JavaUtilDate")
  private static class FloatDateDeserializer extends StdDeserializer<Date> {

    private static final long serialVersionUID = 4446058377205025341L;

    private static final int AWS_DATE_MILLI_SECOND_PRECISION = 3;

    private FloatDateDeserializer() {
      super(Date.class);
    }

    @Override
    public Date deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
      return parseServiceSpecificDate(p.getText());
    }

    // Copied from AWS SDK
    // https://github.com/aws/aws-sdk-java/blob/7b1e5b87b0bf03456df9e77716b14731adf9a7a7/aws-java-sdk-core/src/main/java/com/amazonaws/util/DateUtils.java#L239
    /** Parses the given date string returned by the AWS service into a Date object. */
    private static Date parseServiceSpecificDate(String dateString) {
      try {
        BigDecimal dateValue = new BigDecimal(dateString);
        return new Date(dateValue.scaleByPowerOfTen(AWS_DATE_MILLI_SECOND_PRECISION).longValue());
      } catch (NumberFormatException nfe) {
        throw new IllegalArgumentException("Unable to parse date : " + dateString, nfe);
      }
    }
  }
}
