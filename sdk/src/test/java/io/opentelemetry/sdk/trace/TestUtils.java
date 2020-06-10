/*
 * Copyright 2019, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.sdk.trace;

import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.common.Attributes;
import io.opentelemetry.common.KeyValueConsumer;
import io.opentelemetry.sdk.trace.config.TraceConfig;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.test.TestSpanData;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Span.Kind;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.Status;
import io.opentelemetry.trace.TraceId;
import io.opentelemetry.trace.Tracer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/** Common utilities for unit tests. */
public final class TestUtils {

  private TestUtils() {}

  /**
   * Generates some random attributes used for testing.
   *
   * @return a map of String to AttributeValues
   */
  static Map<String, AttributeValue> generateRandomMapAttributes() {
    Map<String, AttributeValue> result = new HashMap<>();
    AttributeValue attribute = AttributeValue.stringAttributeValue(UUID.randomUUID().toString());
    result.put(UUID.randomUUID().toString(), attribute);
    return result;
  }

  /**
   * Generates some random attributes used for testing.
   *
   * @return some {@link io.opentelemetry.common.Attributes}
   */
  static Attributes generateRandomAttributes() {
    return Attributes.of(
        UUID.randomUUID().toString(),
        AttributeValue.stringAttributeValue(UUID.randomUUID().toString()));
  }

  /**
   * Create a very basic SpanData instance, suitable for testing. It has the bare minimum viable
   * data.
   *
   * @return A SpanData instance.
   */
  public static SpanData makeBasicSpan() {
    return TestSpanData.newBuilder()
        .setHasEnded(true)
        .setTraceId(TraceId.getInvalid())
        .setSpanId(SpanId.getInvalid())
        .setName("span")
        .setKind(Kind.SERVER)
        .setStartEpochNanos(TimeUnit.SECONDS.toNanos(100) + 100)
        .setStatus(Status.OK)
        .setEndEpochNanos(TimeUnit.SECONDS.toNanos(200) + 200)
        .setTotalRecordedLinks(0)
        .setTotalRecordedEvents(0)
        .build();
  }

  /**
   * Create a very basic SpanData instance, suitable for testing. It has the bare minimum viable
   * data.
   *
   * @return A SpanData instance.
   */
  public static Span.Builder startSpanWithSampler(
      TracerSdkProvider tracerSdkFactory, Tracer tracer, String spanName, Sampler sampler) {
    return startSpanWithSampler(
        tracerSdkFactory,
        tracer,
        spanName,
        sampler,
        Collections.<String, AttributeValue>emptyMap());
  }

  /**
   * Create a very basic SpanData instance, suitable for testing. It has the bare minimum viable
   * data.
   *
   * @return A SpanData instance.
   */
  public static Span.Builder startSpanWithSampler(
      TracerSdkProvider tracerSdkFactory,
      Tracer tracer,
      String spanName,
      Sampler sampler,
      Map<String, AttributeValue> attributes) {
    TraceConfig originalConfig = tracerSdkFactory.getActiveTraceConfig();
    tracerSdkFactory.updateActiveTraceConfig(
        originalConfig.toBuilder().setSampler(sampler).build());
    try {
      Span.Builder builder = tracer.spanBuilder(spanName);
      for (Map.Entry<String, AttributeValue> entry : attributes.entrySet()) {
        builder.setAttribute(entry.getKey(), entry.getValue());
      }

      return builder;
    } finally {
      tracerSdkFactory.updateActiveTraceConfig(originalConfig);
    }
  }

  /** Find an AttributeValue by the key. */
  public static AttributeValue findByKey(Attributes attributes, String key) {
    KeyFindingConsumer consumer = new KeyFindingConsumer(key);
    attributes.forEach(consumer);
    return consumer.getResult();
  }

  private static class KeyFindingConsumer implements KeyValueConsumer<AttributeValue> {
    private final String key;

    private AttributeValue result;

    public KeyFindingConsumer(String key) {
      this.key = key;
    }

    @Override
    public void consume(String k, AttributeValue value) {
      if (key.equals(k)) {
        result = value;
      }
    }

    public AttributeValue getResult() {
      return result;
    }
  }
}
