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
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.logs.data.Body;
import io.opentelemetry.sdk.logs.data.LogData;
import io.opentelemetry.sdk.logs.data.LogRecord;
import io.opentelemetry.sdk.logs.data.Severity;
import io.opentelemetry.sdk.resources.Resource;
import org.junit.jupiter.api.Test;

class StdOutLogExporterTest {

  @Test
  void returnCodes() {
    StdOutLogExporter exporter = new StdOutLogExporter();
    CompletableResultCode resultCode = exporter.export(singletonList(sampleLog()));
    assertThat(resultCode).isSameAs(CompletableResultCode.ofSuccess());
    assertThat(exporter.shutdown()).isSameAs(CompletableResultCode.ofSuccess());
  }

  @Test
  void format() {
    LogData log = sampleLog();
    StringBuilder output = new StringBuilder();
    StdOutLogExporter.formatLog(output, log);
    assertThat(output.toString())
        .isEqualTo(
            "1000000000000 ERROR3 'message' : 00000000000000010000000000000002 0000000000000003 "
                + "[libraryInfo: logTest:1.0] {amount=1, cheese=\"cheddar\"}");
  }

  private static LogData sampleLog() {
    return LogRecord.builder(Resource.empty(), InstrumentationLibraryInfo.create("logTest", "1.0"))
        .setAttributes(Attributes.of(stringKey("cheese"), "cheddar", longKey("amount"), 1L))
        .setBody(Body.stringBody("message"))
        .setSeverity(Severity.ERROR3)
        .setEpochMillis(1_000_000)
        .setTraceId(TraceId.fromLongs(1, 2))
        .setSpanId(SpanId.fromLong(3))
        .build();
  }
}
