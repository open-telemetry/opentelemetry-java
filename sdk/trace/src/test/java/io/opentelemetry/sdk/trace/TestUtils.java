/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import static io.opentelemetry.api.common.AttributeKey.booleanArrayKey;
import static io.opentelemetry.api.common.AttributeKey.booleanKey;
import static io.opentelemetry.api.common.AttributeKey.doubleArrayKey;
import static io.opentelemetry.api.common.AttributeKey.doubleKey;
import static io.opentelemetry.api.common.AttributeKey.longArrayKey;
import static io.opentelemetry.api.common.AttributeKey.longKey;
import static io.opentelemetry.api.common.AttributeKey.stringArrayKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.sdk.testing.trace.TestSpanData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.StatusData;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** Common utilities for unit tests. */
public final class TestUtils {

  private TestUtils() {}

  /**
   * Generates some random attributes used for testing.
   *
   * @return some {@link io.opentelemetry.api.common.Attributes}
   */
  static Attributes generateRandomAttributes() {
    return Attributes.of(stringKey(UUID.randomUUID().toString()), UUID.randomUUID().toString());
  }

  /**
   * Create a very basic SpanData instance, suitable for testing. It has the bare minimum viable
   * data.
   *
   * @return A SpanData instance.
   */
  public static SpanData makeBasicSpan() {
    return TestSpanData.builder()
        .setHasEnded(true)
        .setSpanContext(SpanContext.getInvalid())
        .setName("span")
        .setKind(SpanKind.SERVER)
        .setStartEpochNanos(TimeUnit.SECONDS.toNanos(100) + 100)
        .setStatus(StatusData.ok())
        .setEndEpochNanos(TimeUnit.SECONDS.toNanos(200) + 200)
        .setTotalRecordedLinks(0)
        .setTotalRecordedEvents(0)
        .build();
  }

  /**
   * Validate that attributes set via the {@code setter} have the span {@link
   * SpanLimits#getMaxAttributeLength()} applied.
   */
  static void validateAttributeLengthLimits(
      SpanLimits spanLimits, Consumer<Attributes> setter, Supplier<Attributes> getter) {
    StringBuilder strBuilder = new StringBuilder();
    while (strBuilder.length() < spanLimits.getMaxAttributeLength()) {
      strBuilder.append(UUID.randomUUID());
    }
    String strVal = strBuilder.substring(0, spanLimits.getMaxAttributeLength());

    Attributes attributes =
        Attributes.builder()
            .put("string", strVal + strVal)
            .put("boolean", true)
            .put("long", 1L)
            .put("double", 1.0)
            .put(stringArrayKey("stringArray"), Arrays.asList(strVal, strVal + strVal))
            .put(booleanArrayKey("booleanArray"), Arrays.asList(true, false))
            .put(longArrayKey("longArray"), Arrays.asList(1L, 2L))
            .put(doubleArrayKey("doubleArray"), Arrays.asList(1.0, 2.0))
            .build();
    setter.accept(attributes);

    attributes = getter.get();
    assertThat(attributes.get(stringKey("string"))).isEqualTo(strVal);
    assertThat(attributes.get(booleanKey("boolean"))).isEqualTo(true);
    assertThat(attributes.get(longKey("long"))).isEqualTo(1L);
    assertThat(attributes.get(doubleKey("double"))).isEqualTo(1.0);
    assertThat(attributes.get(stringArrayKey("stringArray")))
        .isEqualTo(Arrays.asList(strVal, strVal));
    assertThat(attributes.get(booleanArrayKey("booleanArray")))
        .isEqualTo(Arrays.asList(true, false));
    assertThat(attributes.get(longArrayKey("longArray"))).isEqualTo(Arrays.asList(1L, 2L));
    assertThat(attributes.get(doubleArrayKey("doubleArray"))).isEqualTo(Arrays.asList(1.0, 2.0));
  }
}
