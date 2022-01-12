/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import static io.opentelemetry.api.common.AttributeKey.booleanArrayKey;
import static io.opentelemetry.api.common.AttributeKey.booleanKey;
import static io.opentelemetry.api.common.AttributeKey.doubleArrayKey;
import static io.opentelemetry.api.common.AttributeKey.doubleKey;
import static io.opentelemetry.api.common.AttributeKey.longArrayKey;
import static io.opentelemetry.api.common.AttributeKey.longKey;
import static io.opentelemetry.api.common.AttributeKey.stringArrayKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static io.opentelemetry.sdk.testing.assertj.LogAssertions.assertThat;
import static java.util.stream.Collectors.joining;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.logs.data.LogData;
import io.opentelemetry.sdk.resources.Resource;
import java.time.Instant;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

class SdkLogEmitterTest {

  @Test
  void logBuilder() {
    Instant now = Instant.now();
    LogEmitterSharedState state = mock(LogEmitterSharedState.class);
    InstrumentationLibraryInfo info = InstrumentationLibraryInfo.create("foo", "bar");
    AtomicReference<LogData> seenLog = new AtomicReference<>();
    LogProcessor logProcessor = seenLog::set;

    when(state.getResource()).thenReturn(Resource.getDefault());
    when(state.getLogProcessor()).thenReturn(logProcessor);

    SdkLogEmitter emitter = new SdkLogEmitter(state, info);
    LogBuilder logBuilder = emitter.logBuilder();
    logBuilder.setEpoch(now);
    logBuilder.setBody("foo");

    // Have to test through the builder
    logBuilder.emit();
    assertThat(seenLog.get()).hasBody("foo");
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
    String strVal = IntStream.range(0, maxLength).mapToObj(i -> "a").collect(joining());
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

    assertThat(attributes.get(stringKey("string"))).isEqualTo(strVal);
    assertThat(attributes.get(booleanKey("boolean"))).isEqualTo(true);
    assertThat(attributes.get(longKey("long"))).isEqualTo(1L);
    assertThat(attributes.get(doubleKey("double"))).isEqualTo(1.0);
    assertThat(attributes.get(stringArrayKey("stringArray")))
        .isEqualTo(Arrays.asList(strVal, strVal));
    assertThat(attributes.get(booleanArrayKey("booleanArray")))
        .isEqualTo(Arrays.asList(true, false));
    assertThat(attributes.get(longArrayKey("longArray"))).isEqualTo(Arrays.asList(1L, 2L));
    assertThat(attributes.get(doubleArrayKey("doubleArray"))).isEqualTo(Arrays.asList(1.0, 2.0));
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

    Attributes attributes = seenLog.get().getAttributes();
    assertThat(attributes.size()).isEqualTo(maxNumberOfAttrs);
    // NOTE: cannot guarantee which attributes are retained, only that there are no more that the
    // max
  }
}
