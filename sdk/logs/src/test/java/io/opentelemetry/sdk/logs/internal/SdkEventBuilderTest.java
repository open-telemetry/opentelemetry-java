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
import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.sdk.common.Clock;
import org.junit.jupiter.api.Test;

class SdkEventBuilderTest {

  @Test
  void emit() {
    String eventDomain = "mydomain";
    String eventName = "banana";
    Attributes attributes = Attributes.of(stringKey("foo"), "bar");

    Logger logger = mock(Logger.class);
    LogRecordBuilder logRecordBuilder = mock(LogRecordBuilder.class);
    when(logger.logRecordBuilder()).thenReturn(logRecordBuilder);
    when(logRecordBuilder.setTimestamp(anyLong(), any())).thenReturn(logRecordBuilder);
    when(logRecordBuilder.setAttribute(any(), any())).thenReturn(logRecordBuilder);
    when(logRecordBuilder.setAllAttributes(any())).thenReturn(logRecordBuilder);

    new SdkEventBuilder(Clock.getDefault(), logger, eventDomain, eventName, attributes)
        .setTimestamp(123456L)
        .emit();
    verify(logRecordBuilder).setAllAttributes(attributes);
    verify(logRecordBuilder).setAttribute(stringKey("event.domain"), eventDomain);
    verify(logRecordBuilder).setAttribute(stringKey("event.name"), eventName);
    verify(logRecordBuilder).emit();
  }
}
