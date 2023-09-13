/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.internal.AttributesMap;
import io.opentelemetry.sdk.logs.data.Body;
import io.opentelemetry.sdk.resources.Resource;
import org.junit.jupiter.api.Test;

class ReadWriteLogRecordTest {

  @Test
  void addAllAttributes() {
    Body body = Body.string("bod");
    AttributesMap initialAttributes = AttributesMap.create(100, 200);
    initialAttributes.put(stringKey("foo"), "aaiosjfjioasdiojfjioasojifja");
    initialAttributes.put(stringKey("untouched"), "yes");
    Attributes newAttributes = Attributes.of(stringKey("foo"), "bar", stringKey("bar"), "buzz");
    LogLimits limits = LogLimits.getDefault();
    Resource resource = Resource.empty();
    InstrumentationScopeInfo scope = InstrumentationScopeInfo.create("test");
    SpanContext spanContext = SpanContext.getInvalid();

    SdkReadWriteLogRecord logRecord =
        SdkReadWriteLogRecord.create(
            limits,
            resource,
            scope,
            0L,
            0L,
            spanContext,
            Severity.DEBUG,
            "buggin",
            body,
            initialAttributes);

    logRecord.setAllAttributes(newAttributes);

    Attributes result = logRecord.toLogRecordData().getAttributes();
    assertThat(result.get(stringKey("foo"))).isEqualTo("bar");
    assertThat(result.get(stringKey("bar"))).isEqualTo("buzz");
    assertThat(result.get(stringKey("untouched"))).isEqualTo("yes");
  }
}
