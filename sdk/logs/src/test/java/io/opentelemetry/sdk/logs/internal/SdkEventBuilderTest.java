/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.internal;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.opentelemetry.api.logs.LogRecordBuilder;
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

    Instant instant = Instant.now();
    new SdkEventBuilder(logRecordBuilder, eventName)
        .setTimestamp(123456L, TimeUnit.NANOSECONDS)
        .setTimestamp(instant)
        .emit();
    verify(logRecordBuilder, never()).setAttribute(eq(stringKey("event.domain")), anyString());
    verify(logRecordBuilder).setAttribute(stringKey("event.name"), eventName);
    verify(logRecordBuilder).setTimestamp(123456L, TimeUnit.NANOSECONDS);
    verify(logRecordBuilder).setTimestamp(instant);
    verify(logRecordBuilder).emit();
  }
}
