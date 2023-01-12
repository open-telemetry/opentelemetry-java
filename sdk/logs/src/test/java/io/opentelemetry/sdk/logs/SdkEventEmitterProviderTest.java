/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import static io.opentelemetry.sdk.testing.assertj.LogAssertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.resources.Resource;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class SdkEventEmitterProviderTest {

  @Test
  void emit() {
    AtomicReference<ReadWriteLogRecord> seenLog = new AtomicReference<>();
    Resource resource = Resource.builder().put("resource-key", "resource-value").build();
    SdkEventEmitterProvider eventEmitterProvider =
        SdkEventEmitterProvider.create(
            SdkLoggerProvider.builder()
                .setResource(resource)
                .addLogRecordProcessor((context, logRecord) -> seenLog.set(logRecord))
                .build());

    // Emit event
    eventEmitterProvider
        .eventEmitterBuilder("test-scope", "event-domain")
        .build()
        .emit(
            "event-name",
            Attributes.builder()
                .put("key1", "value1")
                // should be overridden by the eventName argument passed to emit
                .put("event.name", "foo")
                // should be overridden by the eventDomain argument used to obtain eventEmitter
                .put("event.domain", "foo")
                .build());

    assertThat(seenLog.get().toLogRecordData())
        .hasResource(resource)
        .hasInstrumentationScope(InstrumentationScopeInfo.create("test-scope"))
        .hasAttributes(
            Attributes.builder()
                .put("key1", "value1")
                .put("event.domain", "event-domain")
                .put("event.name", "event-name")
                .build());
  }
}
