/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging;

import static io.opentelemetry.api.common.AttributeKey.longKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.logs.data.LogData;
import io.opentelemetry.sdk.logs.data.LogDataBuilder;
import io.opentelemetry.sdk.logs.data.Severity;
import io.opentelemetry.sdk.resources.Resource;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneOffset;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class SystemOutLogExporterTest {

  @Test
  void returnCodes() {
    SystemOutLogExporter exporter = new SystemOutLogExporter();
    CompletableResultCode resultCode =
        exporter.export(singletonList(sampleLog(System.currentTimeMillis())));
    assertThat(resultCode).isSameAs(CompletableResultCode.ofSuccess());
    assertThat(exporter.shutdown()).isSameAs(CompletableResultCode.ofSuccess());
  }

  @Test
  void format() {
    long timestamp =
        LocalDateTime.of(1970, Month.AUGUST, 7, 10, 0).toInstant(ZoneOffset.UTC).toEpochMilli();
    LogData log = sampleLog(timestamp);
    StringBuilder output = new StringBuilder();
    SystemOutLogExporter.formatLog(output, log);
    assertThat(output.toString())
        .isEqualTo(
            "1970-08-07T10:00:00Z ERROR3 'message' : 00000000000000010000000000000002 0000000000000003 "
                + "[libraryInfo: logTest:1.0] {amount=1, cheese=\"cheddar\"}");
  }

  private static LogData sampleLog(long timestamp) {
    return LogDataBuilder.create(
            Resource.empty(), InstrumentationLibraryInfo.create("logTest", "1.0"))
        .setAttributes(Attributes.of(stringKey("cheese"), "cheddar", longKey("amount"), 1L))
        .setBody("message")
        .setSeverity(Severity.ERROR3)
        .setEpoch(timestamp, TimeUnit.MILLISECONDS)
        .setContext(
            Context.root()
                .with(
                    Span.wrap(
                        SpanContext.create(
                            "00000000000000010000000000000002",
                            "0000000000000003",
                            TraceFlags.getDefault(),
                            TraceState.getDefault()))))
        .build();
  }
}
