/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.event;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.logs.LogRecordBuilder;
import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.context.Context;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EventLoggerTest {

  @Mock private Logger logger;
  @Mock private LogRecordBuilder logRecordBuilder;

  @Test
  void createAndEmit() {
    when(logger.logRecordBuilder()).thenReturn(logRecordBuilder);
    when(logRecordBuilder.setEpoch(anyLong(), any())).thenReturn(logRecordBuilder);
    when(logRecordBuilder.setEpoch(any())).thenReturn(logRecordBuilder);
    when(logRecordBuilder.setContext(any())).thenReturn(logRecordBuilder);
    when(logRecordBuilder.setSeverity(any())).thenReturn(logRecordBuilder);
    when(logRecordBuilder.setSeverityText(any())).thenReturn(logRecordBuilder);
    when(logRecordBuilder.setBody(any())).thenReturn(logRecordBuilder);
    when(logRecordBuilder.setAttribute(any(), any())).thenReturn(logRecordBuilder);

    EventLogger.create(logger, "my-event-domain")
        .eventBuilder("my-event-name")
        .setEpoch(100, TimeUnit.SECONDS)
        .setEpoch(Instant.now())
        .setContext(Context.root())
        .setSeverity(Severity.DEBUG)
        .setSeverityText("debug")
        .setBody("body")
        .setAttribute(AttributeKey.stringKey("key1"), "value1")
        .setAllAttributes(Attributes.builder().put("key2", "value2").build())
        .emit();

    verify(logRecordBuilder).setEpoch(100, TimeUnit.SECONDS);
    verify(logRecordBuilder).setEpoch(any());
    verify(logRecordBuilder).setContext(any());
    verify(logRecordBuilder).setSeverity(Severity.DEBUG);
    verify(logRecordBuilder).setSeverityText("debug");
    verify(logRecordBuilder).setBody("body");
    verify(logRecordBuilder).setAttribute(AttributeKey.stringKey("key1"), "value1");
    verify(logRecordBuilder).setAttribute(AttributeKey.stringKey("key2"), "value2");
    verify(logRecordBuilder).setAttribute(SemanticAttributes.EVENT_DOMAIN, "my-event-domain");
    verify(logRecordBuilder).setAttribute(SemanticAttributes.EVENT_NAME, "my-event-name");
  }
}
