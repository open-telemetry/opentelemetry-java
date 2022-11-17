/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.event;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.logs.LogRecordBuilder;
import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;
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
    doCallRealMethod().when(logRecordBuilder).setAllAttributes(any());
    when(logRecordBuilder.setAttribute(any(), any())).thenReturn(logRecordBuilder);

    EventLogger.create(logger, "my-event-domain")
        .emitEvent(
            "my-event-name",
            Attributes.builder().put("key1", "value1").put("key2", "value2").build());

    verify(logRecordBuilder).setAttribute(AttributeKey.stringKey("key1"), "value1");
    verify(logRecordBuilder).setAttribute(AttributeKey.stringKey("key2"), "value2");
    verify(logRecordBuilder).setAttribute(SemanticAttributes.EVENT_DOMAIN, "my-event-domain");
    verify(logRecordBuilder).setAttribute(SemanticAttributes.EVENT_NAME, "my-event-name");
  }
}
