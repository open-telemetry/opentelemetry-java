/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import static io.opentelemetry.api.common.AttributeKey.booleanArrayKey;
import static io.opentelemetry.api.common.AttributeKey.doubleArrayKey;
import static io.opentelemetry.api.common.AttributeKey.longArrayKey;
import static io.opentelemetry.api.common.AttributeKey.stringArrayKey;
import static io.opentelemetry.sdk.testing.assertj.LogAssertions.assertThat;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.internal.StringUtils;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.logs.data.LogData;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class SdkLogEmitterTest {

  @Test
  void logBuilder() {
    LogEmitterSharedState state = mock(LogEmitterSharedState.class);
    InstrumentationScopeInfo info = InstrumentationScopeInfo.create("foo");
    AtomicReference<LogData> seenLog = new AtomicReference<>();
    LogProcessor logProcessor = seenLog::set;
    Clock clock = mock(Clock.class);
    when(clock.now()).thenReturn(5L);

    when(state.getResource()).thenReturn(Resource.getDefault());
    when(state.getLogProcessor()).thenReturn(logProcessor);
    when(state.getClock()).thenReturn(clock);

    SdkLogEmitter emitter = new SdkLogEmitter(state, info);
    LogBuilder logBuilder = emitter.logBuilder();
    logBuilder.setBody("foo");

    // Have to test through the builder
    logBuilder.emit();
    assertThat(seenLog.get()).hasBody("foo").hasEpochNanos(5);
  }

  @Test
  void logBuilder_maxAttributeLength() {
    int maxLength = 25;
    AtomicReference<LogData> seenLog = new AtomicReference<>();
    SdkLogEmitterProvider logEmitterProvider =
        SdkLogEmitterProvider.builder()
            .addLogProcessor(seenLog::set)
            .setLogLimits(() -> LogLimits.builder().setMaxAttributeValueLength(maxLength).build())
            .build();
    LogBuilder logBuilder = logEmitterProvider.get("test").logBuilder();
    String strVal = StringUtils.padLeft("", maxLength);
    String tooLongStrVal = strVal + strVal;

    logBuilder
        .setAttributes(
            Attributes.builder()
                .put("string", tooLongStrVal)
                .put("boolean", true)
                .put("long", 1L)
                .put("double", 1.0)
                .put(stringArrayKey("stringArray"), Arrays.asList(strVal, tooLongStrVal))
                .put(booleanArrayKey("booleanArray"), Arrays.asList(true, false))
                .put(longArrayKey("longArray"), Arrays.asList(1L, 2L))
                .put(doubleArrayKey("doubleArray"), Arrays.asList(1.0, 2.0))
                .build())
        .emit();

    Attributes attributes = seenLog.get().getAttributes();

    assertThat(attributes)
        .containsEntry("string", strVal)
        .containsEntry("boolean", true)
        .containsEntry("long", 1L)
        .containsEntry("double", 1.0)
        .containsEntry("stringArray", strVal, strVal)
        .containsEntry("booleanArray", true, false)
        .containsEntry("longArray", 1L, 2L)
        .containsEntry("doubleArray", 1.0, 2.0);
  }

  @Test
  void logBuilder_maxAttributes() {
    int maxNumberOfAttrs = 8;
    AtomicReference<LogData> seenLog = new AtomicReference<>();
    SdkLogEmitterProvider logEmitterProvider =
        SdkLogEmitterProvider.builder()
            .addLogProcessor(seenLog::set)
            .setLogLimits(
                () -> LogLimits.builder().setMaxNumberOfAttributes(maxNumberOfAttrs).build())
            .build();

    AttributesBuilder attributesBuilder = Attributes.builder();
    for (int i = 0; i < 2 * maxNumberOfAttrs; i++) {
      attributesBuilder.put("key" + i, i);
    }

    logEmitterProvider.get("test").logBuilder().setAttributes(attributesBuilder.build()).emit();

    // NOTE: cannot guarantee which attributes are retained, only that there are no more that the
    // max
    assertThat(seenLog.get().getAttributes()).hasSize(maxNumberOfAttrs);
  }
}
