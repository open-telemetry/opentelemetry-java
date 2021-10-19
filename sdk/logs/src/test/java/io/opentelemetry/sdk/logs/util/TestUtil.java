/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.util;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.logs.data.LogRecord;
import io.opentelemetry.sdk.logs.data.Severity;
import io.opentelemetry.sdk.resources.Resource;

public final class TestUtil {

  public static LogRecord createLog(Severity severity, String message) {
    return LogRecord.builder(
            Resource.create(Attributes.builder().put("testKey", "testValue").build()),
            InstrumentationLibraryInfo.create("instrumentation", "1"))
        .setEpochMillis(System.currentTimeMillis())
        .setTraceId(TraceId.getInvalid())
        .setSpanId(SpanId.getInvalid())
        .setFlags(TraceFlags.getDefault().asByte())
        .setSeverity(severity)
        .setSeverityText("really severe")
        .setName("log1")
        .setBody(message)
        .setAttributes(Attributes.builder().put("animal", "cat").build())
        .build();
  }

  private TestUtil() {}
}
