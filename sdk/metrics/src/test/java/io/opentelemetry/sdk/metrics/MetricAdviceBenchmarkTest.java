/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.metrics.ExtendedLongCounterBuilder;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MetricAdviceBenchmarkTest {

  static final AttributeKey<String> HTTP_REQUEST_METHOD =
      AttributeKey.stringKey("http.request.method");
  static final AttributeKey<String> URL_PATH = AttributeKey.stringKey("url.path");
  static final AttributeKey<String> URL_SCHEME = AttributeKey.stringKey("url.scheme");
  static final AttributeKey<Long> HTTP_RESPONSE_STATUS_CODE =
      AttributeKey.longKey("http.response.status_code");
  static final AttributeKey<String> HTTP_ROUTE = AttributeKey.stringKey("http.route");
  static final AttributeKey<String> NETWORK_PROTOCOL_NAME =
      AttributeKey.stringKey("network.protocol.name");
  static final AttributeKey<Long> SERVER_PORT = AttributeKey.longKey("server.port");
  static final AttributeKey<String> URL_QUERY = AttributeKey.stringKey("url.query");
  static final AttributeKey<String> CLIENT_ADDRESS = AttributeKey.stringKey("client.address");
  static final AttributeKey<String> NETWORK_PEER_ADDRESS =
      AttributeKey.stringKey("network.peer.address");
  static final AttributeKey<Long> NETWORK_PEER_PORT = AttributeKey.longKey("network.peer.port");
  static final AttributeKey<String> NETWORK_PROTOCOL_VERSION =
      AttributeKey.stringKey("network.protocol.version");
  static final AttributeKey<String> SERVER_ADDRESS = AttributeKey.stringKey("server.address");
  static final AttributeKey<String> USER_AGENT_ORIGINAL =
      AttributeKey.stringKey("user_agent.original");

  static final List<AttributeKey<?>> httpServerMetricAttributeKeys =
      Arrays.asList(
          HTTP_REQUEST_METHOD,
          URL_SCHEME,
          HTTP_RESPONSE_STATUS_CODE,
          HTTP_ROUTE,
          NETWORK_PROTOCOL_NAME,
          SERVER_PORT,
          NETWORK_PROTOCOL_VERSION,
          SERVER_ADDRESS);

  static Attributes httpServerMetricAttributes() {
    return Attributes.builder()
        .put(HTTP_REQUEST_METHOD, "GET")
        .put(URL_SCHEME, "http")
        .put(HTTP_RESPONSE_STATUS_CODE, 200)
        .put(HTTP_ROUTE, "/v1/users/{id}")
        .put(NETWORK_PROTOCOL_NAME, "http")
        .put(SERVER_PORT, 8080)
        .put(NETWORK_PROTOCOL_VERSION, "1.1")
        .put(SERVER_ADDRESS, "localhost")
        .build();
  }

  static Attributes httpServerSpanAttributes() {
    return Attributes.builder()
        .put(HTTP_REQUEST_METHOD, "GET")
        .put(URL_PATH, "/v1/users/123")
        .put(URL_SCHEME, "http")
        .put(HTTP_RESPONSE_STATUS_CODE, 200)
        .put(HTTP_ROUTE, "/v1/users/{id}")
        .put(NETWORK_PROTOCOL_NAME, "http")
        .put(SERVER_PORT, 8080)
        .put(URL_QUERY, "with=email")
        .put(CLIENT_ADDRESS, "192.168.0.17")
        .put(NETWORK_PEER_ADDRESS, "192.168.0.17")
        .put(NETWORK_PEER_PORT, 11265)
        .put(NETWORK_PROTOCOL_VERSION, "1.1")
        .put(SERVER_ADDRESS, "localhost")
        .put(USER_AGENT_ORIGINAL, "okhttp/1.27.2")
        .build();
  }

  private SdkMeterProvider meterProvider;
  private Meter meter;

  @BeforeEach
  void setup() {
    meterProvider =
        SdkMeterProvider.builder().registerMetricReader(InMemoryMetricReader.createDelta()).build();
    meter = meterProvider.get("meter");
  }

  @AfterEach
  void tearDown() {}

  @Test
  void adviceAllAttributes() {
    LongCounter counter =
        ((ExtendedLongCounterBuilder) meter.counterBuilder("counter"))
            .setAttributesAdvice(httpServerMetricAttributeKeys)
            .build();

    for (int i = 0; i < 1_000_000; i++) {
      counter.add(1, httpServerSpanAttributes());
    }
  }
}
