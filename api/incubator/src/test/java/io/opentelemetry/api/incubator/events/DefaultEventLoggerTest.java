/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.events;

import static org.assertj.core.api.Assertions.assertThatCode;

import io.opentelemetry.api.common.AnyValue;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.context.Context;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class DefaultEventLoggerTest {

  @Test
  @SuppressWarnings("DoubleBraceInitialization")
  void builder() {
    EventLogger eventLogger = DefaultEventLogger.getInstance();
    assertThatCode(
            () ->
                eventLogger
                    .builder("namespace.myEvent")
                    // Helper methods to set primitive types
                    .put("stringKey", "value")
                    .put("longKey", 1L)
                    .put("doubleKey", 1.0)
                    .put("boolKey", true)
                    // Helper methods to set primitive array types
                    .put("stringArrKey", "value1", "value2")
                    .put("longArrKey", 1L, 2L)
                    .put("doubleArrKey", 1.0, 2.0)
                    .put("boolArrKey", true, false)
                    // Set AnyValue types to encode complex data
                    .put(
                        "anyValueKey",
                        AnyValue.of(
                            new HashMap<String, AnyValue<?>>() {
                              {
                                put("key", AnyValue.of("value"));
                              }
                            }))
                    // Helper methods to set AttributeKey<T> types
                    .put(AttributeKey.stringKey("attrStringKey"), "value")
                    .put(AttributeKey.longKey("attrLongKey"), 1L)
                    .put(AttributeKey.doubleKey("attrDoubleKey"), 1.0)
                    .put(AttributeKey.booleanKey("attrBoolKey"), true)
                    .put(
                        AttributeKey.stringArrayKey("attrStringArrKey"),
                        Arrays.asList("value1", "value2"))
                    .put(AttributeKey.longArrayKey("attrLongArrKey"), Arrays.asList(1L, 2L))
                    .put(AttributeKey.doubleArrayKey("attrDoubleArrKey"), Arrays.asList(1.0, 2.0))
                    .put(AttributeKey.booleanArrayKey("attrBoolArrKey"), Arrays.asList(true, false))
                    // Other setters
                    .setTimestamp(123456L, TimeUnit.NANOSECONDS)
                    .setTimestamp(Instant.now())
                    .setContext(Context.current())
                    .setSeverity(Severity.DEBUG)
                    .setAttributes(Attributes.empty())
                    .emit())
        .doesNotThrowAnyException();
  }
}
