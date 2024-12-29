/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.internal;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.logs.LogRecordBuilder;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.Clock;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class SdkEventBuilderTest {

  @Test
  void emit() {
    String eventName = "banana";

    LogRecordBuilder logRecordBuilder = mock(LogRecordBuilder.class);
    when(logRecordBuilder.setTimestamp(anyLong(), any())).thenReturn(logRecordBuilder);
    when(logRecordBuilder.setAttribute(any(), any())).thenReturn(logRecordBuilder);
    when(logRecordBuilder.setContext(any())).thenReturn(logRecordBuilder);
    when(logRecordBuilder.setSeverity(any())).thenReturn(logRecordBuilder);
    when(logRecordBuilder.setAllAttributes(any())).thenReturn(logRecordBuilder);

    Instant instant = Instant.now();
    Context context = Context.root();
    Attributes attributes = Attributes.builder().put("extra-attribute", "value").build();
    new SdkEventBuilder(Clock.getDefault(), logRecordBuilder, eventName)
        .setTimestamp(123456L, TimeUnit.NANOSECONDS)
        .setTimestamp(instant)
        .setContext(context)
        .setSeverity(Severity.DEBUG)
        .setAttributes(attributes)
        .emit();
    verify(logRecordBuilder).setAttribute(stringKey("event.name"), eventName);
    verify(logRecordBuilder).setTimestamp(123456L, TimeUnit.NANOSECONDS);
    verify(logRecordBuilder).setTimestamp(instant);
    verify(logRecordBuilder).setContext(context);
    verify(logRecordBuilder).setSeverity(Severity.DEBUG);
    verify(logRecordBuilder).setAllAttributes(attributes);
    verify(logRecordBuilder).emit();
  }
}
