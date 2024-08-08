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
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MetricAdviceBenchmarkTest {

  private static final Attributes ALL_ATTRIBUTES;
  private static final Attributes SOME_ATTRIBUTES;
  private static final List<AttributeKey<?>> SOME_ATTRIBUTE_KEYS;

  static {
    SOME_ATTRIBUTES =
        Attributes.builder()
            .put("http.request.method", "GET")
            .put("http.route", "/v1/users/{id}")
            .put("http.response.status_code", 200)
            .build();
    ALL_ATTRIBUTES =
        SOME_ATTRIBUTES.toBuilder().put("http.url", "http://localhost:8080/v1/users/123").build();
    SOME_ATTRIBUTE_KEYS = new ArrayList<>(SOME_ATTRIBUTES.asMap().keySet());
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
  void adviceRecordAll() {
    LongCounter counter =
        ((ExtendedLongCounterBuilder) meter.counterBuilder("counter"))
            .setAttributesAdvice(SOME_ATTRIBUTE_KEYS)
            .build();

    for (int i = 0; i < 1_000_000; i++) {
      counter.add(1, ALL_ATTRIBUTES);
    }
  }
}
