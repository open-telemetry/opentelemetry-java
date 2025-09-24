/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import static io.opentelemetry.api.common.AttributeKey.booleanArrayKey;
import static io.opentelemetry.api.common.AttributeKey.booleanKey;
import static io.opentelemetry.api.common.AttributeKey.doubleArrayKey;
import static io.opentelemetry.api.common.AttributeKey.doubleKey;
import static io.opentelemetry.api.common.AttributeKey.longArrayKey;
import static io.opentelemetry.api.common.AttributeKey.longKey;
import static io.opentelemetry.api.common.AttributeKey.stringArrayKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.equalTo;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.joining;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.internal.AttributesMap;
import io.opentelemetry.sdk.internal.ExceptionAttributeResolver;
import io.opentelemetry.sdk.internal.InstrumentationScopeUtil;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.time.TestClock;
import io.opentelemetry.sdk.trace.data.EventData;
import io.opentelemetry.sdk.trace.data.ExceptionEventData;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.StatusData;
import io.opentelemetry.sdk.trace.internal.ExtendedSpanProcessor;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@SuppressWarnings({"rawtypes", "unchecked"})
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SdkSpanTest {
  private static final String SPAN_NAME = "MySpanName";
  private static final String SPAN_NEW_NAME = "NewName";
  private static final long NANOS_PER_SECOND = TimeUnit.SECONDS.toNanos(1);
  private static final long START_EPOCH_NANOS = 1000_123_789_654L;

  private final IdGenerator idsGenerator = IdGenerator.random();
  private final String traceId = idsGenerator.generateTraceId();
  private final String spanId = idsGenerator.generateSpanId();
  private final String parentSpanId = idsGenerator.generateSpanId();
  private final SpanContext spanContext =
      SpanContext.create(traceId, spanId, TraceFlags.getDefault(), TraceState.getDefault());
  private final Resource resource = Resource.empty();
  private final InstrumentationScopeInfo instrumentationScopeInfo =
      InstrumentationScopeInfo.create("theName");
  private final Map<AttributeKey, Object> attributes = new HashMap<>();
  private Attributes expectedAttributes;
  private final LinkData link = LinkData.create(spanContext);
  @Mock private ExtendedSpanProcessor spanProcessor;

  private TestClock testClock;

  @BeforeEach
  void setUp() {
    attributes.put(stringKey("MyStringAttributeKey"), "MyStringAttributeValue");
    attributes.put(longKey("MyLongAttributeKey"), 123L);
    attributes.put(booleanKey("MyBooleanAttributeKey"), false);
    AttributesBuilder builder =
        Attributes.builder().put("MySingleStringAttributeKey", "MySingleStringAttributeValue");
    for (Map.Entry<AttributeKey, Object> entry : attributes.entrySet()) {
      builder.put(entry.getKey(), entry.getValue());
    }
    expectedAttributes = builder.build();
    testClock = TestClock.create(Instant.ofEpochSecond(0, START_EPOCH_NANOS));
    when(spanProcessor.isStartRequired()).thenReturn(true);
    when(spanProcessor.isOnEndingRequired()).thenReturn(true);
    when(spanProcessor.isEndRequired()).thenReturn(true);
  }

  @Test
  void nothingChangedAfterEnd() {
    SdkSpan span = createTestSpan(SpanKind.INTERNAL);
    span.end();
    // Check that adding trace events or update fields after Span#end() does not throw any thrown
    // and are ignored.
    spanDoWork(span, StatusCode.ERROR, "CANCELLED");
    SpanData spanData = span.toSpanData();
    verifySpanData(
        spanData,
        Attributes.empty(),
        Collections.emptyList(),
        singletonList(link),
        SPAN_NAME,
        START_EPOCH_NANOS,
        START_EPOCH_NANOS,
        StatusData.unset(),
        /* hasEnded= */ true);
  }

  @Test
  void endSpanTwice_DoNotCrash() {
    SdkSpan span = createTestSpan(SpanKind.INTERNAL);
    assertThat(span.hasEnded()).isFalse();
    span.end();
    assertThat(span.hasEnded()).isTrue();
    span.end();
    assertThat(span.hasEnded()).isTrue();
  }

  @Test
  void onEnding_spanStillMutable() {
    SdkSpan span = createTestSpan(SpanKind.INTERNAL);

    AttributeKey<String> dummyAttrib = AttributeKey.stringKey("processor_foo");

    AtomicBoolean endedStateInProcessor = new AtomicBoolean();
    doAnswer(
            invocation -> {
              ReadWriteSpan sp = invocation.getArgument(0, ReadWriteSpan.class);
              assertThat(sp.hasEnded()).isFalse();
              sp.end(); // should have no effect, nested end should be detected
              endedStateInProcessor.set(sp.hasEnded());
              sp.setAttribute(dummyAttrib, "bar");
              return null;
            })
        .when(spanProcessor)
        .onEnding(any());

    span.end();
    verify(spanProcessor).onEnding(same(span));
    assertThat(span.hasEnded()).isTrue();
    assertThat(endedStateInProcessor.get()).isFalse();
    assertThat(span.getAttribute(dummyAttrib)).isEqualTo("bar");
  }

  @Test
  void onEnding_concurrentModificationsPrevented() {
    SdkSpan span = createTestSpan(SpanKind.INTERNAL);

    AttributeKey<String> syncAttrib = AttributeKey.stringKey("sync_foo");
    AttributeKey<String> concurrentAttrib = AttributeKey.stringKey("concurrent_foo");

    doAnswer(
            invocation -> {
              ReadWriteSpan sp = invocation.getArgument(0, ReadWriteSpan.class);

              Thread concurrent =
                  new Thread(
                      () -> {
                        sp.setAttribute(concurrentAttrib, "concurrent_bar");
                      });
              concurrent.start();
              concurrent.join();

              sp.setAttribute(syncAttrib, "sync_bar");

              return null;
            })
        .when(spanProcessor)
        .onEnding(any());

    span.end();
    verify(spanProcessor).onEnding(same(span));
    assertThat(span.getAttribute(concurrentAttrib)).isNull();
    assertThat(span.getAttribute(syncAttrib)).isEqualTo("sync_bar");
  }

  @Test
  void onEnding_latencyPinned() {
    SdkSpan span = createTestSpan(SpanKind.INTERNAL);

    AtomicLong spanLatencyInProcessor = new AtomicLong();
    doAnswer(
            invocation -> {
              ReadWriteSpan sp = invocation.getArgument(0, ReadWriteSpan.class);

              testClock.advance(Duration.ofSeconds(100));
              spanLatencyInProcessor.set(sp.getLatencyNanos());
              return null;
            })
        .when(spanProcessor)
        .onEnding(any());

    testClock.advance(Duration.ofSeconds(1));
    long expectedDuration = testClock.now() - START_EPOCH_NANOS;

    assertThat(span.getLatencyNanos()).isEqualTo(expectedDuration);

    span.end();
    verify(spanProcessor).onEnding(same(span));
    assertThat(span.hasEnded()).isTrue();
    assertThat(span.getLatencyNanos()).isEqualTo(expectedDuration);
    assertThat(spanLatencyInProcessor.get()).isEqualTo(expectedDuration);
  }

  @Test
  void toSpanData_ActiveSpan() {
    SdkSpan span = createTestSpan(SpanKind.INTERNAL);
    try {
      assertThat(span.hasEnded()).isFalse();
      spanDoWork(span, null, null);
      SpanData spanData = span.toSpanData();
      EventData event =
          EventData.create(START_EPOCH_NANOS + NANOS_PER_SECOND, "event2", Attributes.empty(), 0);
      verifySpanData(
          spanData,
          expectedAttributes,
          singletonList(event),
          singletonList(link),
          SPAN_NEW_NAME,
          START_EPOCH_NANOS,
          0,
          StatusData.unset(),
          /* hasEnded= */ false);
      assertThat(span.hasEnded()).isFalse();
      assertThat(span.isRecording()).isTrue();
    } finally {
      span.end();
    }
    assertThat(span.hasEnded()).isTrue();
    assertThat(span.isRecording()).isFalse();
  }

  @Test
  void toSpanData_EndedSpan() {
    SdkSpan span = createTestSpan(SpanKind.INTERNAL);
    try {
      spanDoWork(span, StatusCode.ERROR, "CANCELLED");
    } finally {
      span.end();
    }
    Mockito.verify(spanProcessor, Mockito.times(1)).onEnd(span);
    SpanData spanData = span.toSpanData();
    EventData event =
        EventData.create(START_EPOCH_NANOS + NANOS_PER_SECOND, "event2", Attributes.empty(), 0);
    verifySpanData(
        spanData,
        expectedAttributes,
        singletonList(event),
        singletonList(link),
        SPAN_NEW_NAME,
        START_EPOCH_NANOS,
        testClock.now(),
        StatusData.create(StatusCode.ERROR, "CANCELLED"),
        /* hasEnded= */ true);
  }

  @Test
  void toSpanData_immutableLinks() {
    SdkSpan span = createTestSpan(SpanKind.INTERNAL);
    SpanData spanData = span.toSpanData();

    assertThatThrownBy(() -> spanData.getLinks().add(LinkData.create(SpanContext.getInvalid())))
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void toSpanData_immutableEvents() {
    SdkSpan span = createTestSpan(SpanKind.INTERNAL);
    SpanData spanData = span.toSpanData();

    assertThatThrownBy(
            () -> spanData.getEvents().add(EventData.create(1000, "test", Attributes.empty())))
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void toSpanData_immutableEvents_ended() {
    SdkSpan span = createTestSpan(SpanKind.INTERNAL);
    span.end();
    SpanData spanData = span.toSpanData();

    assertThatThrownBy(
            () -> spanData.getEvents().add(EventData.create(1000, "test", Attributes.empty())))
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void toSpanData_RootSpan() {
    SdkSpan span = createTestRootSpan();
    try {
      spanDoWork(span, null, null);
    } finally {
      span.end();
    }
    assertThat(span.getParentSpanContext().isValid()).isFalse();
    SpanData spanData = span.toSpanData();
    assertThat(SpanId.isValid(spanData.getParentSpanId())).isFalse();
  }

  @Test
  void toSpanData_ChildSpan() {
    SdkSpan span = createTestSpan(SpanKind.INTERNAL);
    try {
      spanDoWork(span, null, null);
    } finally {
      span.end();
    }
    assertThat(span.getParentSpanContext().isValid()).isTrue();
    assertThat(span.getParentSpanContext().getTraceId()).isEqualTo(traceId);
    assertThat(span.getParentSpanContext().getSpanId()).isEqualTo(parentSpanId);
    SpanData spanData = span.toSpanData();
    assertThat(spanData.getParentSpanId()).isEqualTo(parentSpanId);
  }

  @Test
  void toSpanData_WithInitialAttributes() {
    SdkSpan span = createTestSpanWithAttributes(attributes);
    span.setAttribute("anotherKey", "anotherValue");
    span.end();
    SpanData spanData = span.toSpanData();
    assertThat(spanData.getAttributes().size()).isEqualTo(attributes.size() + 1);
    assertThat(spanData.getTotalAttributeCount()).isEqualTo(attributes.size() + 1);
  }

  @Test
  void toSpanData_SpanDataDoesNotChangeWhenModifyingSpan() {
    // Create a span
    SdkSpan span = createTestSpanWithAttributes(attributes);

    // Convert it to a SpanData object -- this should be an immutable snapshot.
    SpanData spanData = span.toSpanData();

    // Now modify the span after creating the snapshot.
    span.setAttribute("anotherKey", "anotherValue");
    span.updateName("changedName");
    span.addEvent("newEvent");
    span.end();

    // Assert that the snapshot does not reflect the modified state, but the state of the time when
    // toSpanData was called.
    assertThat(spanData.getAttributes().size()).isEqualTo(attributes.size());
    assertThat(spanData.getAttributes().get(stringKey("anotherKey"))).isNull();
    assertThat(spanData.hasEnded()).isFalse();
    assertThat(spanData.getEndEpochNanos()).isEqualTo(0);
    assertThat(spanData.getName()).isEqualTo(SPAN_NAME);
    assertThat(spanData.getEvents()).isEmpty();

    // Sanity check: Calling toSpanData again after modifying the span should get us the modified
    // state.
    spanData = span.toSpanData();
    assertThat(spanData.getAttributes().size()).isEqualTo(attributes.size() + 1);
    assertThat(spanData.getAttributes().get(stringKey("anotherKey"))).isEqualTo("anotherValue");
    assertThat(spanData.hasEnded()).isTrue();
    assertThat(spanData.getEndEpochNanos()).isGreaterThan(0);
    assertThat(spanData.getName()).isEqualTo("changedName");
    assertThat(spanData.getEvents()).hasSize(1);
  }

  @Test
  void toSpanData_Status() {
    SdkSpan span = createTestSpan(SpanKind.CONSUMER);
    try {
      testClock.advance(Duration.ofSeconds(1));
      assertThat(span.toSpanData().getStatus()).isEqualTo(StatusData.unset());
      span.setStatus(StatusCode.ERROR, "CANCELLED");
      assertThat(span.toSpanData().getStatus())
          .isEqualTo(StatusData.create(StatusCode.ERROR, "CANCELLED"));
    } finally {
      span.end();
    }
    assertThat(span.toSpanData().getStatus())
        .isEqualTo(StatusData.create(StatusCode.ERROR, "CANCELLED"));
  }

  @Test
  void toSpanData_Kind() {
    SdkSpan span = createTestSpan(SpanKind.SERVER);
    try {
      assertThat(span.toSpanData().getKind()).isEqualTo(SpanKind.SERVER);
    } finally {
      span.end();
    }
  }

  @Test
  void getKind() {
    SdkSpan span = createTestSpan(SpanKind.SERVER);
    try {
      assertThat(span.getKind()).isEqualTo(SpanKind.SERVER);
    } finally {
      span.end();
    }
  }

  @Test
  void getAttribute() {
    SdkSpan span = createTestSpanWithAttributes(attributes);
    try {
      assertThat(span.getAttribute(longKey("MyLongAttributeKey"))).isEqualTo(123L);
    } finally {
      span.end();
    }
  }

  @Test
  void getAttributes() {
    SdkSpan span = createTestSpanWithAttributes(attributes);
    try {
      assertThat(span.getAttributes())
          .isEqualTo(
              Attributes.builder()
                  .put("MyBooleanAttributeKey", false)
                  .put("MyStringAttributeKey", "MyStringAttributeValue")
                  .put("MyLongAttributeKey", 123L)
                  .build());
    } finally {
      span.end();
    }
  }

  @Test
  void getAttributes_Empty() {
    SdkSpan span = createTestSpan(SpanKind.INTERNAL);
    try {
      assertThat(span.getAttributes()).isEqualTo(Attributes.empty());
    } finally {
      span.end();
    }
  }

  @Test
  @SuppressWarnings("deprecation")
  // Testing deprecated code
  void getInstrumentationLibraryInfo() {
    SdkSpan span = createTestSpan(SpanKind.CLIENT);
    try {
      assertThat(span.getInstrumentationLibraryInfo())
          .isEqualTo(
              InstrumentationScopeUtil.toInstrumentationLibraryInfo(instrumentationScopeInfo));
    } finally {
      span.end();
    }
  }

  @Test
  void getInstrumentationScopeInfo() {
    SdkSpan span = createTestSpan(SpanKind.CLIENT);
    try {
      assertThat(span.getInstrumentationScopeInfo()).isEqualTo(instrumentationScopeInfo);
    } finally {
      span.end();
    }
  }

  @Test
  void getAndUpdateSpanName() {
    SdkSpan span = createTestRootSpan();
    try {
      assertThat(span.getName()).isEqualTo(SPAN_NAME);
      span.updateName(SPAN_NEW_NAME);
      assertThat(span.getName()).isEqualTo(SPAN_NEW_NAME);
    } finally {
      span.end();
    }
  }

  @Test
  void getLatencyNs_ActiveSpan() {
    SdkSpan span = createTestSpan(SpanKind.INTERNAL);
    try {
      testClock.advance(Duration.ofSeconds(1));
      long elapsedTimeNanos1 = testClock.now() - START_EPOCH_NANOS;
      assertThat(span.getLatencyNanos()).isEqualTo(elapsedTimeNanos1);
      testClock.advance(Duration.ofSeconds(1));
      long elapsedTimeNanos2 = testClock.now() - START_EPOCH_NANOS;
      assertThat(span.getLatencyNanos()).isEqualTo(elapsedTimeNanos2);
    } finally {
      span.end();
    }
  }

  @Test
  void getLatencyNs_EndedSpan() {
    SdkSpan span = createTestSpan(SpanKind.INTERNAL);
    testClock.advance(Duration.ofSeconds(1));
    span.end();
    long elapsedTimeNanos = testClock.now() - START_EPOCH_NANOS;
    assertThat(span.getLatencyNanos()).isEqualTo(elapsedTimeNanos);
    testClock.advance(Duration.ofSeconds(1));
    assertThat(span.getLatencyNanos()).isEqualTo(elapsedTimeNanos);
  }

  @Test
  void setAttribute() {
    SdkSpan span = createTestRootSpan();
    try {
      span.setAttribute("StringKey", "StringVal");
      span.setAttribute("NullStringKey", null);
      span.setAttribute("EmptyStringKey", "");
      span.setAttribute(stringKey("NullStringAttributeValue"), null);
      span.setAttribute(stringKey("EmptyStringAttributeValue"), "");
      span.setAttribute("LongKey", 1000L);
      span.setAttribute(longKey("LongKey2"), 5);
      span.setAttribute(longKey("LongKey3"), 6L);
      span.setAttribute("DoubleKey", 10.0);
      span.setAttribute("BooleanKey", false);
      span.setAttribute(
          stringArrayKey("ArrayStringKey"), Arrays.asList("StringVal", null, "", "StringVal2"));
      span.setAttribute(longArrayKey("ArrayLongKey"), Arrays.asList(1L, 2L, 3L, 4L, 5L));
      span.setAttribute(doubleArrayKey("ArrayDoubleKey"), Arrays.asList(0.1, 2.3, 4.5, 6.7, 8.9));
      span.setAttribute(
          booleanArrayKey("ArrayBooleanKey"), Arrays.asList(true, false, false, true));
      // These should be dropped
      span.setAttribute(stringArrayKey("NullArrayStringKey"), null);
      span.setAttribute(longArrayKey("NullArrayLongKey"), null);
      span.setAttribute(doubleArrayKey("NullArrayDoubleKey"), null);
      span.setAttribute(booleanArrayKey("NullArrayBooleanKey"), null);
      // These should be maintained
      span.setAttribute(longArrayKey("ArrayWithNullLongKey"), singletonList(null));
      span.setAttribute(stringArrayKey("ArrayWithNullStringKey"), singletonList(null));
      span.setAttribute(doubleArrayKey("ArrayWithNullDoubleKey"), singletonList(null));
      span.setAttribute(booleanArrayKey("ArrayWithNullBooleanKey"), singletonList(null));
    } finally {
      span.end();
    }
    SpanData spanData = span.toSpanData();
    assertThat(spanData.getAttributes().size()).isEqualTo(16);
    assertThat(spanData.getAttributes().get(stringKey("StringKey"))).isNotNull();
    assertThat(spanData.getAttributes().get(stringKey("EmptyStringKey"))).isNotNull();
    assertThat(spanData.getAttributes().get(stringKey("EmptyStringAttributeValue"))).isNotNull();
    assertThat(spanData.getAttributes().get(longKey("LongKey"))).isNotNull();
    assertThat(spanData.getAttributes().get(longKey("LongKey2"))).isEqualTo(5L);
    assertThat(spanData.getAttributes().get(longKey("LongKey3"))).isEqualTo(6L);
    assertThat(spanData.getAttributes().get(doubleKey("DoubleKey"))).isNotNull();
    assertThat(spanData.getAttributes().get(booleanKey("BooleanKey"))).isNotNull();
    assertThat(spanData.getAttributes().get(stringArrayKey("ArrayStringKey"))).isNotNull();
    assertThat(spanData.getAttributes().get(longArrayKey("ArrayLongKey"))).isNotNull();
    assertThat(spanData.getAttributes().get(doubleArrayKey("ArrayDoubleKey"))).isNotNull();
    assertThat(spanData.getAttributes().get(booleanArrayKey("ArrayBooleanKey"))).isNotNull();
    assertThat(spanData.getAttributes().get(longArrayKey("ArrayWithNullLongKey"))).isNotNull();
    assertThat(spanData.getAttributes().get(stringArrayKey("ArrayWithNullStringKey"))).isNotNull();
    assertThat(spanData.getAttributes().get(doubleArrayKey("ArrayWithNullDoubleKey"))).isNotNull();
    assertThat(spanData.getAttributes().get(booleanArrayKey("ArrayWithNullBooleanKey")))
        .isNotNull();
    assertThat(spanData.getAttributes().get(stringArrayKey("ArrayStringKey")).size()).isEqualTo(4);
    assertThat(spanData.getAttributes().get(longArrayKey("ArrayLongKey")).size()).isEqualTo(5);
    assertThat(spanData.getAttributes().get(doubleArrayKey("ArrayDoubleKey")).size()).isEqualTo(5);
    assertThat(spanData.getAttributes().get(booleanArrayKey("ArrayBooleanKey")).size())
        .isEqualTo(4);
  }

  @Test
  void setAttribute_emptyKeys() {
    SdkSpan span = createTestRootSpan();
    span.setAttribute("", "");
    span.setAttribute("", 1000L);
    span.setAttribute("", 10.0);
    span.setAttribute("", false);
    span.setAttribute(stringArrayKey(""), Collections.emptyList());
    span.setAttribute(booleanArrayKey(""), Collections.emptyList());
    span.setAttribute(longArrayKey(""), Collections.emptyList());
    span.setAttribute(doubleArrayKey(""), Collections.emptyList());
    assertThat(span.toSpanData().getAttributes().size()).isEqualTo(0);
  }

  @Test
  void setAttribute_nullKeys() {
    SdkSpan span = createTestRootSpan();
    span.setAttribute(stringKey(null), "");
    span.setAttribute(null, 1000L);
    span.setAttribute(null, 10.0);
    span.setAttribute(null, false);
    span.setAttribute(null, Collections.emptyList());
    span.setAttribute(null, Collections.emptyList());
    span.setAttribute(null, Collections.emptyList());
    span.setAttribute(null, Collections.emptyList());
    assertThat(span.toSpanData().getAttributes().size()).isZero();
  }

  @Test
  void setAttribute_emptyArrayAttributeValue() {
    SdkSpan span = createTestRootSpan();
    span.setAttribute(stringArrayKey("stringArrayAttribute"), null);
    assertThat(span.toSpanData().getAttributes().size()).isZero();
    span.setAttribute(stringArrayKey("stringArrayAttribute"), Collections.emptyList());
    assertThat(span.toSpanData().getAttributes().size()).isEqualTo(1);
    span.setAttribute(booleanArrayKey("boolArrayAttribute"), null);
    assertThat(span.toSpanData().getAttributes().size()).isEqualTo(1);
    span.setAttribute(booleanArrayKey("boolArrayAttribute"), Collections.emptyList());
    assertThat(span.toSpanData().getAttributes().size()).isEqualTo(2);
    span.setAttribute(longArrayKey("longArrayAttribute"), null);
    assertThat(span.toSpanData().getAttributes().size()).isEqualTo(2);
    span.setAttribute(longArrayKey("longArrayAttribute"), Collections.emptyList());
    assertThat(span.toSpanData().getAttributes().size()).isEqualTo(3);
    span.setAttribute(doubleArrayKey("doubleArrayAttribute"), null);
    assertThat(span.toSpanData().getAttributes().size()).isEqualTo(3);
    span.setAttribute(doubleArrayKey("doubleArrayAttribute"), Collections.emptyList());
    assertThat(span.toSpanData().getAttributes().size()).isEqualTo(4);
  }

  @Test
  void setAttribute_nullStringValue() {
    SdkSpan span = createTestRootSpan();
    span.setAttribute("nullString", null);
    span.setAttribute("emptyString", "");
    span.setAttribute(stringKey("nullStringAttributeValue"), null);
    span.setAttribute(stringKey("emptyStringAttributeValue"), "");
    assertThat(span.toSpanData().getAttributes().size()).isEqualTo(2);
  }

  @Test
  void setAttribute_nullAttributeValue() {
    SdkSpan span = createTestRootSpan();
    span.setAttribute("emptyString", "");
    span.setAttribute(stringKey("nullString"), null);
    span.setAttribute(stringKey("nullStringAttributeValue"), null);
    span.setAttribute(stringKey("emptyStringAttributeValue"), "");
    span.setAttribute("longAttribute", 0L);
    span.setAttribute("boolAttribute", false);
    span.setAttribute("doubleAttribute", 0.12345f);
    span.setAttribute(stringArrayKey("stringArrayAttribute"), Arrays.asList("", null));
    span.setAttribute(booleanArrayKey("boolArrayAttribute"), Arrays.asList(true, null));
    span.setAttribute(longArrayKey("longArrayAttribute"), Arrays.asList(12345L, null));
    span.setAttribute(doubleArrayKey("doubleArrayAttribute"), Arrays.asList(1.2345, null));
    assertThat(span.toSpanData().getAttributes().size()).isEqualTo(9);
  }

  @Test
  void setAllAttributes() {
    SdkSpan span = createTestRootSpan();
    Attributes attributes =
        Attributes.builder()
            .put("StringKey", "StringVal")
            .put("NullStringKey", (String) null)
            .put("EmptyStringKey", "")
            .put(stringKey("NullStringAttributeValue"), null)
            .put(stringKey("EmptyStringAttributeValue"), "")
            .put("LongKey", 1000L)
            .put(longKey("LongKey2"), 5)
            .put(longKey("LongKey3"), 6L)
            .put("DoubleKey", 10.0)
            .put("BooleanKey", false)
            .put(
                stringArrayKey("ArrayStringKey"),
                Arrays.asList("StringVal", null, "", "StringVal2"))
            .put(longArrayKey("ArrayLongKey"), Arrays.asList(1L, 2L, 3L, 4L, 5L))
            .put(doubleArrayKey("ArrayDoubleKey"), Arrays.asList(0.1, 2.3, 4.5, 6.7, 8.9))
            .put(booleanArrayKey("ArrayBooleanKey"), Arrays.asList(true, false, false, true))
            // These should be dropped
            .put(stringArrayKey("NullArrayStringKey"), (String[]) null)
            .put(longArrayKey("NullArrayLongKey"), (Long[]) null)
            .put(doubleArrayKey("NullArrayDoubleKey"), (Double[]) null)
            .put(booleanArrayKey("NullArrayBooleanKey"), (Boolean[]) null)
            // These should be maintained
            .put(longArrayKey("ArrayWithNullLongKey"), singletonList(null))
            .put(stringArrayKey("ArrayWithNullStringKey"), singletonList(null))
            .put(doubleArrayKey("ArrayWithNullDoubleKey"), singletonList(null))
            .put(booleanArrayKey("ArrayWithNullBooleanKey"), singletonList(null))
            .build();

    try {
      span.setAllAttributes(attributes);
    } finally {
      span.end();
    }

    SpanData spanData = span.toSpanData();
    assertThat(spanData.getAttributes().size()).isEqualTo(16);
    assertThat(spanData.getAttributes().get(stringKey("StringKey"))).isNotNull();
    assertThat(spanData.getAttributes().get(stringKey("EmptyStringKey"))).isNotNull();
    assertThat(spanData.getAttributes().get(stringKey("EmptyStringAttributeValue"))).isNotNull();
    assertThat(spanData.getAttributes().get(longKey("LongKey"))).isNotNull();
    assertThat(spanData.getAttributes().get(longKey("LongKey2"))).isEqualTo(5L);
    assertThat(spanData.getAttributes().get(longKey("LongKey3"))).isEqualTo(6L);
    assertThat(spanData.getAttributes().get(doubleKey("DoubleKey"))).isNotNull();
    assertThat(spanData.getAttributes().get(booleanKey("BooleanKey"))).isNotNull();
    assertThat(spanData.getAttributes().get(stringArrayKey("ArrayStringKey"))).isNotNull();
    assertThat(spanData.getAttributes().get(longArrayKey("ArrayLongKey"))).isNotNull();
    assertThat(spanData.getAttributes().get(doubleArrayKey("ArrayDoubleKey"))).isNotNull();
    assertThat(spanData.getAttributes().get(booleanArrayKey("ArrayBooleanKey"))).isNotNull();
    assertThat(spanData.getAttributes().get(longArrayKey("ArrayWithNullLongKey"))).isNotNull();
    assertThat(spanData.getAttributes().get(stringArrayKey("ArrayWithNullStringKey"))).isNotNull();
    assertThat(spanData.getAttributes().get(doubleArrayKey("ArrayWithNullDoubleKey"))).isNotNull();
    assertThat(spanData.getAttributes().get(booleanArrayKey("ArrayWithNullBooleanKey")))
        .isNotNull();
    assertThat(spanData.getAttributes().get(stringArrayKey("ArrayStringKey")).size()).isEqualTo(4);
    assertThat(spanData.getAttributes().get(longArrayKey("ArrayLongKey")).size()).isEqualTo(5);
    assertThat(spanData.getAttributes().get(doubleArrayKey("ArrayDoubleKey")).size()).isEqualTo(5);
    assertThat(spanData.getAttributes().get(booleanArrayKey("ArrayBooleanKey")).size())
        .isEqualTo(4);
  }

  @Test
  void setAllAttributes_mergesAttributes() {
    SdkSpan span = createTestRootSpan();
    Attributes attributes =
        Attributes.builder()
            .put("StringKey", "StringVal")
            .put("LongKey", 1000L)
            .put("DoubleKey", 10.0)
            .put("BooleanKey", false)
            .build();

    try {
      span.setAttribute("StringKey", "OtherStringVal")
          .setAttribute("ExistingStringKey", "ExistingStringVal")
          .setAttribute("LongKey", 2000L)
          .setAllAttributes(attributes);
    } finally {
      span.end();
    }

    SpanData spanData = span.toSpanData();
    assertThat(spanData.getAttributes().size()).isEqualTo(5);
    assertThat(spanData.getAttributes().get(stringKey("StringKey")))
        .isNotNull()
        .isEqualTo("StringVal");
    assertThat(spanData.getAttributes().get(stringKey("ExistingStringKey")))
        .isNotNull()
        .isEqualTo("ExistingStringVal");
    assertThat(spanData.getAttributes().get(longKey("LongKey"))).isNotNull().isEqualTo(1000L);
    assertThat(spanData.getAttributes().get(doubleKey("DoubleKey"))).isNotNull().isEqualTo(10.0);
    assertThat(spanData.getAttributes().get(booleanKey("BooleanKey"))).isNotNull().isEqualTo(false);
  }

  @Test
  void setAllAttributes_nullAttributes() {
    SdkSpan span = createTestRootSpan();
    span.setAllAttributes(null);
    assertThat(span.toSpanData().getAttributes().size()).isEqualTo(0);
  }

  @Test
  void setAllAttributes_emptyAttributes() {
    SdkSpan span = createTestRootSpan();
    span.setAllAttributes(Attributes.empty());
    assertThat(span.toSpanData().getAttributes().size()).isEqualTo(0);
  }

  @Test
  void addEvent() {
    SdkSpan span = createTestRootSpan();
    try {
      span.addEvent("event1");
      span.addEvent("event2", Attributes.of(stringKey("e1key"), "e1Value"));
      span.addEvent("event3", 10, TimeUnit.SECONDS);
      span.addEvent("event4", Instant.ofEpochSecond(20));
      span.addEvent(
          "event5", Attributes.builder().put("foo", "bar").build(), 30, TimeUnit.MILLISECONDS);
      span.addEvent(
          "event6", Attributes.builder().put("foo", "bar").build(), Instant.ofEpochMilli(1000));
    } finally {
      span.end();
    }
    List<EventData> events = span.toSpanData().getEvents();
    assertThat(events).hasSize(6);
    assertThat(events.get(0))
        .satisfies(
            event -> {
              assertThat(event.getName()).isEqualTo("event1");
              assertThat(event.getAttributes()).isEqualTo(Attributes.empty());
              assertThat(event.getEpochNanos()).isEqualTo(START_EPOCH_NANOS);
            });
    assertThat(events.get(1))
        .satisfies(
            event -> {
              assertThat(event.getName()).isEqualTo("event2");
              assertThat(event.getAttributes())
                  .isEqualTo(Attributes.of(stringKey("e1key"), "e1Value"));
              assertThat(event.getEpochNanos()).isEqualTo(START_EPOCH_NANOS);
            });
    assertThat(events.get(2))
        .satisfies(
            event -> {
              assertThat(event.getName()).isEqualTo("event3");
              assertThat(event.getAttributes()).isEqualTo(Attributes.empty());
              assertThat(event.getEpochNanos()).isEqualTo(TimeUnit.SECONDS.toNanos(10));
            });
    assertThat(events.get(3))
        .satisfies(
            event -> {
              assertThat(event.getName()).isEqualTo("event4");
              assertThat(event.getAttributes()).isEqualTo(Attributes.empty());
              assertThat(event.getEpochNanos()).isEqualTo(TimeUnit.SECONDS.toNanos(20));
            });
    assertThat(events.get(4))
        .satisfies(
            event -> {
              assertThat(event.getName()).isEqualTo("event5");
              assertThat(event.getAttributes())
                  .isEqualTo(Attributes.builder().put("foo", "bar").build());
              assertThat(event.getEpochNanos()).isEqualTo(TimeUnit.MILLISECONDS.toNanos(30));
            });
    assertThat(events.get(5))
        .satisfies(
            event -> {
              assertThat(event.getName()).isEqualTo("event6");
              assertThat(event.getAttributes())
                  .isEqualTo(Attributes.builder().put("foo", "bar").build());
              assertThat(event.getEpochNanos()).isEqualTo(TimeUnit.MILLISECONDS.toNanos(1000));
            });
  }

  @Test
  void attributeLength() {
    int maxLength = 25;
    SdkSpan span =
        createTestSpan(SpanLimits.builder().setMaxAttributeValueLength(maxLength).build());
    try {
      String strVal = IntStream.range(0, maxLength).mapToObj(i -> "a").collect(joining());
      String tooLongStrVal = strVal + strVal;

      Attributes attributes =
          Attributes.builder()
              .put("string", tooLongStrVal)
              .put("boolean", true)
              .put("long", 1L)
              .put("double", 1.0)
              .put(stringArrayKey("stringArray"), Arrays.asList(strVal, tooLongStrVal))
              .put(booleanArrayKey("booleanArray"), Arrays.asList(true, false))
              .put(longArrayKey("longArray"), Arrays.asList(1L, 2L))
              .put(doubleArrayKey("doubleArray"), Arrays.asList(1.0, 2.0))
              .build();
      span.setAllAttributes(attributes);
      span.recordException(new IllegalStateException(tooLongStrVal));

      SpanData spanData = span.toSpanData();
      attributes = spanData.getAttributes();
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

      List<EventData> events = spanData.getEvents();
      assertThat(events).hasSize(1);
      EventData event = events.get(0);
      assertThat(event.getName()).isEqualTo("exception");
      assertThat(event.getAttributes().get(stringKey("exception.type")))
          .isEqualTo("java.lang.IllegalStateException".substring(0, maxLength));
      assertThat(event.getAttributes().get(stringKey("exception.message"))).isEqualTo(strVal);
      assertThat(event.getAttributes().get(stringKey("exception.stacktrace")).length())
          .isLessThanOrEqualTo(maxLength);
      assertThat(event.getAttributes().size()).isEqualTo(3);
    } finally {
      span.end();
    }
  }

  @Test
  void eventAttributeLength() {
    int maxLength = 25;
    SdkSpan span =
        createTestSpan(SpanLimits.builder().setMaxAttributeValueLength(maxLength).build());
    try {
      String strVal = IntStream.range(0, maxLength).mapToObj(i -> "a").collect(joining());
      String tooLongStrVal = strVal + strVal;

      Attributes attributes =
          Attributes.builder()
              .put("string", tooLongStrVal)
              .put("boolean", true)
              .put("long", 1L)
              .put("double", 1.0)
              .put(stringArrayKey("stringArray"), Arrays.asList(strVal, tooLongStrVal))
              .put(booleanArrayKey("booleanArray"), Arrays.asList(true, false))
              .put(longArrayKey("longArray"), Arrays.asList(1L, 2L))
              .put(doubleArrayKey("doubleArray"), Arrays.asList(1.0, 2.0))
              .build();
      span.setAllAttributes(attributes);

      attributes = span.toSpanData().getAttributes();
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
    } finally {
      span.end();
    }
  }

  @Test
  void addLink() {
    int maxLinks = 3;
    int maxNumberOfAttributes = 4;
    int maxAttributeLength = 5;
    SdkSpan span =
        createTestSpan(
            SpanKind.INTERNAL,
            SpanLimits.builder()
                .setMaxNumberOfLinks(maxLinks)
                .setMaxNumberOfAttributesPerLink(maxNumberOfAttributes)
                .setMaxAttributeValueLength(maxAttributeLength)
                .build(),
            parentSpanId,
            null,
            null,
            ExceptionAttributeResolver.getDefault());
    try {
      Span span1 = createTestSpan(SpanKind.INTERNAL);
      Span span2 = createTestSpan(SpanKind.INTERNAL);
      Span span3 = createTestSpan(SpanKind.INTERNAL);
      Span span4 = createTestSpan(SpanKind.INTERNAL);

      span.addLink(span1.getSpanContext());

      Attributes span2LinkAttributes =
          Attributes.builder()
              .put("key1", true)
              .put("key2", true)
              .put("key3", true)
              .put(
                  "key4",
                  IntStream.range(0, maxAttributeLength + 1).mapToObj(i -> "a").collect(joining()))
              .build();
      span.addLink(span2.getSpanContext(), span2LinkAttributes);

      Attributes span3LinkAttributes =
          Attributes.builder()
              .put("key1", true)
              .put("key2", true)
              .put("key3", true)
              .put("key4", true)
              .put("key5", true)
              .build();
      span.addLink(span3.getSpanContext(), span3LinkAttributes);

      span.addLink(span4.getSpanContext());

      SpanData spanData = span.toSpanData();
      // 1 link added during span construction via createTestSpan, 4 links added after span start
      assertThat(spanData.getTotalRecordedLinks()).isEqualTo(4);
      assertThat(spanData.getLinks())
          .satisfiesExactly(
              link -> {
                assertThat(link.getSpanContext()).isEqualTo(span1.getSpanContext());
                assertThat(link.getAttributes()).isEqualTo(Attributes.empty());
              },
              link -> {
                assertThat(link.getSpanContext()).isEqualTo(span2.getSpanContext());
                assertThat(link.getAttributes())
                    .isEqualTo(
                        Attributes.builder()
                            .put("key1", true)
                            .put("key2", true)
                            .put("key3", true)
                            // Should be truncated to max attribute length
                            .put(
                                "key4",
                                IntStream.range(0, maxAttributeLength)
                                    .mapToObj(i -> "a")
                                    .collect(joining()))
                            .build());
              },
              link -> {
                assertThat(link.getSpanContext()).isEqualTo(span2.getSpanContext());
                // The 5th attribute key should be omitted due to attribute limits. Can't predict
                // which of the 5 is dropped.
                assertThat(link.getAttributes().size()).isEqualTo(4);
              });
    } finally {
      span.end();
    }
  }

  @Test
  void addLink_InvalidArgs() {
    SdkSpan span = createTestSpan(SpanKind.INTERNAL);
    assertThatCode(() -> span.addLink(null)).doesNotThrowAnyException();
    assertThatCode(() -> span.addLink(SpanContext.getInvalid())).doesNotThrowAnyException();
    assertThatCode(() -> span.addLink(null, null)).doesNotThrowAnyException();
    assertThatCode(() -> span.addLink(SpanContext.getInvalid(), Attributes.empty()))
        .doesNotThrowAnyException();
  }

  @Test
  void addLink_FaultIn() {
    SdkSpan span =
        SdkSpan.startSpan(
            spanContext,
            SPAN_NAME,
            instrumentationScopeInfo,
            SpanKind.INTERNAL,
            Span.getInvalid(),
            Context.root(),
            SpanLimits.getDefault(),
            spanProcessor,
            ExceptionAttributeResolver.getDefault(),
            testClock,
            resource,
            null,
            null, // exercises the fault-in path
            0,
            0);
    SdkSpan linkedSpan = createTestSpan(SpanKind.INTERNAL);
    span.addLink(linkedSpan.getSpanContext());

    SpanData spanData = span.toSpanData();
    assertThat(spanData.getTotalRecordedLinks()).isEqualTo(1);
    assertThat(spanData.getLinks())
        .satisfiesExactly(
            link -> {
              assertThat(link.getSpanContext()).isEqualTo(linkedSpan.getSpanContext());
            });
  }

  @Test
  void droppingAttributes() {
    int maxNumberOfAttributes = 8;
    SpanLimits spanLimits =
        SpanLimits.builder().setMaxNumberOfAttributes(maxNumberOfAttributes).build();
    SdkSpan span = createTestSpan(spanLimits);
    try {
      for (int i = 0; i < 2 * maxNumberOfAttributes; i++) {
        span.setAttribute(longKey("MyStringAttributeKey" + i), (long) i);
      }
      SpanData spanData = span.toSpanData();
      assertThat(spanData.getAttributes().size()).isEqualTo(maxNumberOfAttributes);
      assertThat(spanData.getTotalAttributeCount()).isEqualTo(2 * maxNumberOfAttributes);
    } finally {
      span.end();
    }
    SpanData spanData = span.toSpanData();
    assertThat(spanData.getAttributes().size()).isEqualTo(maxNumberOfAttributes);
    assertThat(spanData.getTotalAttributeCount()).isEqualTo(2 * maxNumberOfAttributes);
  }

  @Test
  void endWithTimestamp_numeric() {
    SdkSpan span1 = createTestRootSpan();
    span1.end(10, TimeUnit.NANOSECONDS);
    assertThat(span1.toSpanData().getEndEpochNanos()).isEqualTo(10);
  }

  @Test
  void endWithTimestamp_instant() {
    SdkSpan span1 = createTestRootSpan();
    span1.end(Instant.ofEpochMilli(10));
    assertThat(span1.toSpanData().getEndEpochNanos()).isEqualTo(TimeUnit.MILLISECONDS.toNanos(10));
  }

  @Test
  void droppingAndAddingAttributes() {
    int maxNumberOfAttributes = 8;
    SpanLimits spanLimits =
        SpanLimits.builder().setMaxNumberOfAttributes(maxNumberOfAttributes).build();
    SdkSpan span = createTestSpan(spanLimits);
    try {
      for (int i = 0; i < 2 * maxNumberOfAttributes; i++) {
        span.setAttribute(longKey("MyStringAttributeKey" + i), (long) i);
      }
      SpanData spanData = span.toSpanData();
      assertThat(spanData.getAttributes().size()).isEqualTo(maxNumberOfAttributes);
      assertThat(spanData.getTotalAttributeCount()).isEqualTo(2 * maxNumberOfAttributes);

      for (int i = 0; i < maxNumberOfAttributes / 2; i++) {
        int val = i + maxNumberOfAttributes * 3 / 2;
        span.setAttribute(longKey("MyStringAttributeKey" + i), (long) val);
      }
      spanData = span.toSpanData();
      assertThat(spanData.getAttributes().size()).isEqualTo(maxNumberOfAttributes);
      // Test that we still have in the attributes map the latest maxNumberOfAttributes / 2 entries.
      for (int i = 0; i < maxNumberOfAttributes / 2; i++) {
        int val = i + maxNumberOfAttributes * 3 / 2;
        assertThat(spanData.getAttributes().get(longKey("MyStringAttributeKey" + i)))
            .isEqualTo(val);
      }
      // Test that we have the newest re-added initial entries.
      for (int i = maxNumberOfAttributes / 2; i < maxNumberOfAttributes; i++) {
        assertThat(spanData.getAttributes().get(longKey("MyStringAttributeKey" + i))).isEqualTo(i);
      }
    } finally {
      span.end();
    }
  }

  @Test
  void droppingEvents() {
    int maxNumberOfEvents = 8;
    SpanLimits spanLimits = SpanLimits.builder().setMaxNumberOfEvents(maxNumberOfEvents).build();
    SdkSpan span = createTestSpan(spanLimits);
    try {
      for (int i = 0; i < 2 * maxNumberOfEvents; i++) {
        span.addEvent("event2", Attributes.empty());
        testClock.advance(Duration.ofSeconds(1));
      }
      SpanData spanData = span.toSpanData();

      assertThat(spanData.getEvents().size()).isEqualTo(maxNumberOfEvents);
      for (int i = 0; i < maxNumberOfEvents; i++) {
        EventData expectedEvent =
            EventData.create(
                START_EPOCH_NANOS + i * NANOS_PER_SECOND, "event2", Attributes.empty(), 0);
        assertThat(spanData.getEvents().get(i)).isEqualTo(expectedEvent);
        assertThat(spanData.getTotalRecordedEvents()).isEqualTo(2 * maxNumberOfEvents);
      }
    } finally {
      span.end();
    }
    SpanData spanData = span.toSpanData();
    assertThat(spanData.getEvents().size()).isEqualTo(maxNumberOfEvents);
    for (int i = 0; i < maxNumberOfEvents; i++) {
      EventData expectedEvent =
          EventData.create(
              START_EPOCH_NANOS + i * NANOS_PER_SECOND, "event2", Attributes.empty(), 0);
      assertThat(spanData.getEvents().get(i)).isEqualTo(expectedEvent);
    }
  }

  @Test
  void recordException() {
    IllegalStateException exception = new IllegalStateException("there was an exception");
    SdkSpan span = createTestRootSpan();

    StringWriter writer = new StringWriter();
    exception.printStackTrace(new PrintWriter(writer));
    String stacktrace = writer.toString();

    testClock.advance(Duration.ofNanos(1000));
    long timestamp = testClock.now();

    // make sure that span attributes don't leak down to the exception event
    span.setAttribute("spankey", "val");

    span.recordException(exception);

    List<EventData> events = span.toSpanData().getEvents();
    assertThat(events).hasSize(1);
    EventData event = events.get(0);
    assertThat(event.getName()).isEqualTo("exception");
    assertThat(event.getEpochNanos()).isEqualTo(timestamp);
    assertThat(event.getAttributes().get(stringKey("exception.message")))
        .isEqualTo("there was an exception");
    assertThat(event.getAttributes().get(stringKey("exception.type")))
        .isEqualTo(exception.getClass().getName());
    assertThat(event.getAttributes().get(stringKey("exception.stacktrace"))).isEqualTo(stacktrace);
    assertThat(event.getAttributes().size()).isEqualTo(3);
    assertThat(event)
        .isInstanceOfSatisfying(
            ExceptionEventData.class,
            exceptionEvent -> {
              assertThat(exceptionEvent.getException()).isSameAs(exception);
            });
  }

  @Test
  void recordException_noMessage() {
    IllegalStateException exception = new IllegalStateException();
    SdkSpan span = createTestRootSpan();

    StringWriter writer = new StringWriter();
    exception.printStackTrace(new PrintWriter(writer));
    String stacktrace = writer.toString();

    span.recordException(exception);

    List<EventData> events = span.toSpanData().getEvents();
    assertThat(events).hasSize(1);
    EventData event = events.get(0);
    assertThat(event.getAttributes().get(stringKey("exception.message"))).isNull();
    assertThat(event.getAttributes().get(stringKey("exception.type")))
        .isEqualTo("java.lang.IllegalStateException");
    assertThat(event.getAttributes().get(stringKey("exception.stacktrace"))).isEqualTo(stacktrace);
    assertThat(event.getAttributes().size()).isEqualTo(2);
  }

  private static class InnerClassException extends Exception {}

  @Test
  void recordException_innerClassException() {
    InnerClassException exception = new InnerClassException();
    SdkSpan span = createTestRootSpan();

    StringWriter writer = new StringWriter();
    exception.printStackTrace(new PrintWriter(writer));
    String stacktrace = writer.toString();

    span.recordException(exception);

    List<EventData> events = span.toSpanData().getEvents();
    assertThat(events).hasSize(1);
    EventData event = events.get(0);
    assertThat(event.getAttributes().get(stringKey("exception.type")))
        .isEqualTo("io.opentelemetry.sdk.trace.SdkSpanTest.InnerClassException");
    assertThat(event.getAttributes().get(stringKey("exception.stacktrace"))).isEqualTo(stacktrace);
    assertThat(event.getAttributes().size()).isEqualTo(2);
  }

  @Test
  void recordException_additionalAttributes() {
    IllegalStateException exception = new IllegalStateException("there was an exception");
    SdkSpan span = createTestRootSpan();

    StringWriter writer = new StringWriter();
    exception.printStackTrace(new PrintWriter(writer));
    String stacktrace = writer.toString();

    testClock.advance(Duration.ofNanos(1000));
    long timestamp = testClock.now();

    // make sure that span attributes don't leak down to the exception event
    span.setAttribute("spankey", "val");

    span.recordException(
        exception,
        Attributes.of(
            stringKey("key1"),
            "this is an additional attribute",
            stringKey("exception.message"),
            "this is a precedence attribute"));

    List<EventData> events = span.toSpanData().getEvents();
    assertThat(events).hasSize(1);
    EventData event = events.get(0);
    assertThat(event.getName()).isEqualTo("exception");
    assertThat(event.getEpochNanos()).isEqualTo(timestamp);
    assertThat(event.getAttributes().get(stringKey("exception.message")))
        .isEqualTo("this is a precedence attribute");
    assertThat(event.getAttributes().get(stringKey("key1")))
        .isEqualTo("this is an additional attribute");
    assertThat(event.getAttributes().get(stringKey("exception.type")))
        .isEqualTo("java.lang.IllegalStateException");
    assertThat(event.getAttributes().get(stringKey("exception.stacktrace"))).isEqualTo(stacktrace);
    assertThat(event.getAttributes().size()).isEqualTo(4);

    assertThat(event)
        .isInstanceOfSatisfying(
            ExceptionEventData.class,
            exceptionEvent -> {
              assertThat(exceptionEvent.getException()).isSameAs(exception);
            });
  }

  @Test
  void recordException_SpanLimits() {
    SdkSpan span = createTestSpan(SpanLimits.builder().setMaxNumberOfAttributes(2).build());
    span.recordException(
        new IllegalStateException("error"),
        Attributes.builder().put("key1", "value").put("key2", "value").build());

    List<EventData> events = span.toSpanData().getEvents();
    assertThat(events.size()).isEqualTo(1);
    EventData event = events.get(0);
    assertThat(event.getAttributes().size()).isEqualTo(2);
    assertThat(event.getTotalAttributeCount()).isEqualTo(5);
    assertThat(event.getTotalAttributeCount() - event.getAttributes().size()).isPositive();
  }

  @Test
  void recordException_CustomResolver() {
    ExceptionAttributeResolver exceptionAttributeResolver =
        new ExceptionAttributeResolver() {
          @Override
          public void setExceptionAttributes(
              AttributeSetter attributeSetter, Throwable throwable, int maxAttributeLength) {
            attributeSetter.setAttribute(ExceptionAttributeResolver.EXCEPTION_TYPE, "type");
            attributeSetter.setAttribute(
                ExceptionAttributeResolver.EXCEPTION_STACKTRACE, "stacktrace");
          }
        };

    SdkSpan span =
        createTestSpan(
            SpanKind.INTERNAL,
            SpanLimits.getDefault(),
            parentSpanId,
            null,
            singletonList(link),
            exceptionAttributeResolver);

    span.recordException(new IllegalStateException("error"));

    List<EventData> events = span.toSpanData().getEvents();
    assertThat(events.size()).isEqualTo(1);
    EventData event = events.get(0);
    assertThat(event)
        .hasAttributesSatisfyingExactly(
            equalTo(ExceptionAttributeResolver.EXCEPTION_TYPE, "type"),
            equalTo(ExceptionAttributeResolver.EXCEPTION_STACKTRACE, "stacktrace"));
  }

  @Test
  void badArgsIgnored() {
    SdkSpan span = createTestRootSpan();

    // Should be no exceptions
    span.setAttribute(null, 0L);
    span.setStatus(null);
    span.setStatus(null, null);
    span.updateName(null);
    span.addEvent(null);
    span.addEvent(null, 0, null);
    span.addEvent("event", 0, null);
    span.addEvent(null, (Attributes) null);
    span.addEvent("event", (Attributes) null);
    span.addEvent(null, (Instant) null);
    span.addEvent(null, null, 0, null);
    span.addEvent("event", null, 0, TimeUnit.MILLISECONDS);
    span.addEvent("event", Attributes.empty(), 0, null);
    span.addEvent(null, null, null);
    span.recordException(null);
    span.end(0, TimeUnit.NANOSECONDS);
    span.end(1, null);
    span.end(null);

    // Ignored the bad calls
    SpanData data = span.toSpanData();
    assertThat(data.getAttributes().isEmpty()).isTrue();
    assertThat(data.getStatus()).isEqualTo(StatusData.unset());
    assertThat(data.getName()).isEqualTo(SPAN_NAME);
  }

  @Test
  void onStartOnEndNotRequired() {
    when(spanProcessor.isStartRequired()).thenReturn(false);
    when(spanProcessor.isEndRequired()).thenReturn(false);

    SpanLimits spanLimits = SpanLimits.getDefault();
    SdkSpan span =
        SdkSpan.startSpan(
            spanContext,
            SPAN_NAME,
            instrumentationScopeInfo,
            SpanKind.INTERNAL,
            parentSpanId != null
                ? Span.wrap(
                    SpanContext.create(
                        traceId, parentSpanId, TraceFlags.getDefault(), TraceState.getDefault()))
                : Span.getInvalid(),
            Context.root(),
            spanLimits,
            spanProcessor,
            ExceptionAttributeResolver.getDefault(),
            testClock,
            resource,
            AttributesMap.create(
                spanLimits.getMaxNumberOfAttributes(), spanLimits.getMaxAttributeValueLength()),
            Collections.emptyList(),
            1,
            0);
    verify(spanProcessor, never()).onStart(any(), any());

    span.end();
    verify(spanProcessor, never()).onEnd(any());
  }

  @ParameterizedTest
  @MethodSource("setStatusArgs")
  void setStatus(Consumer<Span> spanConsumer, StatusData expectedSpanData) {
    SdkSpan testSpan = createTestRootSpan();
    spanConsumer.accept(testSpan);
    assertThat(testSpan.toSpanData().getStatus()).isEqualTo(expectedSpanData);
  }

  private static Stream<Arguments> setStatusArgs() {
    return Stream.of(
        // Default status is UNSET
        Arguments.of(spanConsumer(span -> {}), StatusData.unset()),
        // Simple cases
        Arguments.of(spanConsumer(span -> span.setStatus(StatusCode.OK)), StatusData.ok()),
        Arguments.of(spanConsumer(span -> span.setStatus(StatusCode.ERROR)), StatusData.error()),
        // UNSET is ignored
        Arguments.of(
            spanConsumer(span -> span.setStatus(StatusCode.OK).setStatus(StatusCode.UNSET)),
            StatusData.ok()),
        Arguments.of(
            spanConsumer(span -> span.setStatus(StatusCode.ERROR).setStatus(StatusCode.UNSET)),
            StatusData.error()),
        // Description is ignored unless status is ERROR
        Arguments.of(
            spanConsumer(span -> span.setStatus(StatusCode.UNSET, "description")),
            StatusData.unset()),
        Arguments.of(
            spanConsumer(span -> span.setStatus(StatusCode.OK, "description")), StatusData.ok()),
        Arguments.of(
            spanConsumer(span -> span.setStatus(StatusCode.ERROR, "description")),
            StatusData.create(StatusCode.ERROR, "description")),
        // ERROR is ignored if status is OK
        Arguments.of(
            spanConsumer(
                span -> span.setStatus(StatusCode.OK).setStatus(StatusCode.ERROR, "description")),
            StatusData.ok()),
        // setStatus ignored after span is ended
        Arguments.of(
            spanConsumer(
                span -> {
                  span.end();
                  span.setStatus(StatusCode.OK);
                }),
            StatusData.unset()),
        Arguments.of(
            spanConsumer(
                span -> {
                  span.end();
                  span.setStatus(StatusCode.ERROR);
                }),
            StatusData.unset()));
  }

  private static Consumer<Span> spanConsumer(Consumer<Span> spanConsumer) {
    return spanConsumer;
  }

  private SdkSpan createTestSpanWithAttributes(Map<AttributeKey, Object> attributes) {
    SpanLimits spanLimits = SpanLimits.getDefault();
    AttributesMap attributesMap =
        AttributesMap.create(
            spanLimits.getMaxNumberOfAttributes(), spanLimits.getMaxAttributeValueLength());
    attributes.forEach(attributesMap::put);
    return createTestSpan(
        SpanKind.INTERNAL,
        SpanLimits.getDefault(),
        null,
        attributesMap,
        singletonList(link),
        ExceptionAttributeResolver.getDefault());
  }

  private SdkSpan createTestRootSpan() {
    return createTestSpan(
        SpanKind.INTERNAL,
        SpanLimits.getDefault(),
        SpanId.getInvalid(),
        null,
        singletonList(link),
        ExceptionAttributeResolver.getDefault());
  }

  private SdkSpan createTestSpan(SpanKind kind) {
    return createTestSpan(
        kind,
        SpanLimits.getDefault(),
        parentSpanId,
        null,
        singletonList(link),
        ExceptionAttributeResolver.getDefault());
  }

  private SdkSpan createTestSpan(SpanLimits config) {
    return createTestSpan(
        SpanKind.INTERNAL,
        config,
        parentSpanId,
        null,
        singletonList(link),
        ExceptionAttributeResolver.getDefault());
  }

  private SdkSpan createTestSpan(
      SpanKind kind,
      SpanLimits config,
      @Nullable String parentSpanId,
      @Nullable AttributesMap attributes,
      @Nullable List<LinkData> links,
      ExceptionAttributeResolver exceptionAttributeResolver) {
    List<LinkData> linksCopy = links == null ? new ArrayList<>() : new ArrayList<>(links);

    SdkSpan span =
        SdkSpan.startSpan(
            spanContext,
            SPAN_NAME,
            instrumentationScopeInfo,
            kind,
            parentSpanId != null
                ? Span.wrap(
                    SpanContext.create(
                        traceId, parentSpanId, TraceFlags.getDefault(), TraceState.getDefault()))
                : Span.getInvalid(),
            Context.root(),
            config,
            spanProcessor,
            exceptionAttributeResolver,
            testClock,
            resource,
            attributes,
            linksCopy,
            linksCopy.size(),
            0);
    Mockito.verify(spanProcessor, Mockito.times(1)).onStart(Context.root(), span);
    return span;
  }

  private void spanDoWork(
      SdkSpan span, @Nullable StatusCode canonicalCode, @Nullable String descriptio) {
    span.setAttribute("MySingleStringAttributeKey", "MySingleStringAttributeValue");
    attributes.forEach(span::setAttribute);
    testClock.advance(Duration.ofSeconds(1));
    span.addEvent("event2", Attributes.empty());
    testClock.advance(Duration.ofSeconds(1));
    span.updateName(SPAN_NEW_NAME);
    if (canonicalCode != null) {
      span.setStatus(canonicalCode, descriptio);
    }
  }

  private void verifySpanData(
      SpanData spanData,
      Attributes attributes,
      List<EventData> eventData,
      List<LinkData> links,
      String spanName,
      long startEpochNanos,
      long endEpochNanos,
      StatusData status,
      boolean hasEnded) {
    assertThat(spanData.getTraceId()).isEqualTo(traceId);
    assertThat(spanData.getSpanId()).isEqualTo(spanId);
    assertThat(spanData.getParentSpanId()).isEqualTo(parentSpanId);
    assertThat(spanData.getSpanContext().getTraceState()).isEqualTo(TraceState.getDefault());
    assertThat(spanData.getResource()).isEqualTo(resource);
    assertThat(spanData.getInstrumentationScopeInfo()).isEqualTo(instrumentationScopeInfo);
    assertThat(spanData.getName()).isEqualTo(spanName);
    assertThat(spanData.getEvents()).isEqualTo(eventData);
    assertThat(spanData.getLinks()).isEqualTo(links);
    assertThat(spanData.getStartEpochNanos()).isEqualTo(startEpochNanos);
    assertThat(spanData.getEndEpochNanos()).isEqualTo(endEpochNanos);
    assertThat(spanData.getStatus().getStatusCode()).isEqualTo(status.getStatusCode());
    assertThat(spanData.hasEnded()).isEqualTo(hasEnded);

    // verify equality manually, since the implementations don't all equals with each other.
    Attributes spanDataAttributes = spanData.getAttributes();
    assertThat(spanDataAttributes.size()).isEqualTo(attributes.size());
    spanDataAttributes.forEach((key, value) -> assertThat(attributes.get(key)).isEqualTo(value));
  }

  @Test
  void testAsSpanData() {
    String name = "GreatSpan";
    SpanKind kind = SpanKind.SERVER;
    String traceId = this.traceId;
    String spanId = this.spanId;
    String parentSpanId = this.parentSpanId;
    SpanLimits spanLimits = SpanLimits.getDefault();
    SpanProcessor spanProcessor = NoopSpanProcessor.getInstance();
    TestClock clock = TestClock.create();
    Resource resource = this.resource;
    Attributes attributes = TestUtils.generateRandomAttributes();
    AttributesMap attributesWithCapacity = AttributesMap.create(32, Integer.MAX_VALUE);
    attributes.forEach(attributesWithCapacity::put);
    Attributes event1Attributes = TestUtils.generateRandomAttributes();
    Attributes event2Attributes = TestUtils.generateRandomAttributes();
    SpanContext context =
        SpanContext.create(traceId, spanId, TraceFlags.getDefault(), TraceState.getDefault());
    LinkData link1 = LinkData.create(context, TestUtils.generateRandomAttributes());

    SdkSpan readableSpan =
        SdkSpan.startSpan(
            context,
            name,
            instrumentationScopeInfo,
            kind,
            parentSpanId != null
                ? Span.wrap(
                    SpanContext.create(
                        traceId, parentSpanId, TraceFlags.getDefault(), TraceState.getDefault()))
                : Span.getInvalid(),
            Context.root(),
            spanLimits,
            spanProcessor,
            ExceptionAttributeResolver.getDefault(),
            clock,
            resource,
            attributesWithCapacity,
            singletonList(link1),
            1,
            0);
    long startEpochNanos = clock.now();
    clock.advance(Duration.ofMillis(4));
    long firstEventEpochNanos = clock.now();
    readableSpan.addEvent("event1", event1Attributes);
    clock.advance(Duration.ofMillis(6));
    long secondEventTimeNanos = clock.now();
    readableSpan.addEvent("event2", event2Attributes);

    clock.advance(Duration.ofMillis(100));
    readableSpan.end();
    long endEpochNanos = clock.now();

    List<EventData> events =
        Arrays.asList(
            EventData.create(
                firstEventEpochNanos, "event1", event1Attributes, event1Attributes.size()),
            EventData.create(
                secondEventTimeNanos, "event2", event2Attributes, event2Attributes.size()));

    SpanData result = readableSpan.toSpanData();
    verifySpanData(
        result,
        attributesWithCapacity,
        events,
        singletonList(link1),
        name,
        startEpochNanos,
        endEpochNanos,
        StatusData.unset(),
        /* hasEnded= */ true);
    assertThat(result.getTotalRecordedLinks()).isEqualTo(1);
    assertThat(result.getSpanContext().isSampled()).isEqualTo(false);
  }

  @Test
  void testConcurrentModification() throws ExecutionException, InterruptedException {
    SdkSpan span = createTestSpan(SpanKind.INTERNAL);
    ExecutorService es = Executors.newSingleThreadExecutor();
    Future<?> modifierFuture =
        es.submit(
            () -> {
              for (int i = 0; i < 5096 * 5; ++i) {
                span.setAttribute("hey" + i, "");
              }
            });
    try {
      for (int i = 0; i < 5096 * 5; ++i) {
        span.toSpanData();
      }
    } catch (Throwable t) {
      modifierFuture.cancel(true);
      throw t;
    }
    modifierFuture.get();
  }
}
