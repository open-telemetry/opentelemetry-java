/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.util;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.logs.data.LogData;
import io.opentelemetry.sdk.logs.data.LogDataBuilder;
import io.opentelemetry.sdk.logs.data.Severity;
import io.opentelemetry.sdk.resources.Resource;
import java.util.concurrent.TimeUnit;

public final class TestUtil {

  public static LogData createLogData(Severity severity, String message) {
    return LogDataBuilder.create(
            Resource.create(Attributes.builder().put("testKey", "testValue").build()),
            InstrumentationScopeInfo.create("instrumentation", "1", null))
        .setEpoch(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
        .setSeverity(severity)
        .setSeverityText("really severe")
        .setBody(message)
        .setAttributes(Attributes.builder().put("animal", "cat").build())
        .build();
  }

  private TestUtil() {}
}
