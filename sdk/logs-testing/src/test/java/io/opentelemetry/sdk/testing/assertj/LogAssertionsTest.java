/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.assertj;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static io.opentelemetry.sdk.testing.assertj.LogAssertions.assertThat;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.attributeEntry;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.equalTo;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.logs.TestLogRecordData;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

public class LogAssertionsTest {
  private static final Resource RESOURCE =
      Resource.create(Attributes.of(stringKey("resource_key"), "resource_value"));
  private static final InstrumentationScopeInfo INSTRUMENTATION_SCOPE_INFO =
      InstrumentationScopeInfo.create("instrumentation_library");
  private static final String TRACE_ID = "00000000000000010000000000000002";
  private static final String SPAN_ID = "0000000000000003";
  private static final Attributes ATTRIBUTES =
      Attributes.builder()
          .put("bear", "mya")
          .put("warm", true)
          .put("temperature", 30)
          .put("length", 1.2)
          .put("colors", "red", "blue")
          .put("conditions", false, true)
          .put("scores", 0L, 1L)
          .put("coins", 0.01, 0.05, 0.1)
          .build();

  private static final LogRecordData LOG_DATA =
      TestLogRecordData.builder()
          .setResource(RESOURCE)
          .setInstrumentationScopeInfo(INSTRUMENTATION_SCOPE_INFO)
          .setEpoch(100, TimeUnit.NANOSECONDS)
          .setSpanContext(
              SpanContext.create(
                  TRACE_ID, SPAN_ID, TraceFlags.getDefault(), TraceState.getDefault()))
          .setSeverity(Severity.INFO)
          .setSeverityText("info")
          .setBody("message")
          .setAttributes(ATTRIBUTES)
          .setTotalAttributeCount(999)
          .build();

  @Test
  void passing() {
    assertThat(LOG_DATA)
        .hasResource(RESOURCE)
        .hasInstrumentationScope(INSTRUMENTATION_SCOPE_INFO)
        .hasEpochNanos(100)
        .hasSpanContext(
            SpanContext.create(TRACE_ID, SPAN_ID, TraceFlags.getDefault(), TraceState.getDefault()))
        .hasSeverity(Severity.INFO)
        .hasSeverityText("info")
        .hasBody("message")
        .hasAttributes(ATTRIBUTES)
        .hasAttributes(
            attributeEntry("bear", "mya"),
            attributeEntry("warm", true),
            attributeEntry("temperature", 30),
            attributeEntry("length", 1.2),
            attributeEntry("colors", "red", "blue"),
            attributeEntry("conditions", false, true),
            attributeEntry("scores", 0L, 1L),
            attributeEntry("coins", 0.01, 0.05, 0.1))
        .hasAttributesSatisfying(
            attributes ->
                OpenTelemetryAssertions.assertThat(attributes)
                    .hasSize(8)
                    .containsEntry(AttributeKey.stringKey("bear"), "mya")
                    .hasEntrySatisfying(
                        AttributeKey.stringKey("bear"), value -> assertThat(value).hasSize(3))
                    .containsEntry("bear", "mya")
                    .containsEntry("warm", true)
                    .containsEntry("temperature", 30)
                    .containsEntry(AttributeKey.longKey("temperature"), 30L)
                    .containsEntry(AttributeKey.longKey("temperature"), 30)
                    .containsEntry("length", 1.2)
                    .containsEntry("colors", "red", "blue")
                    .containsEntryWithStringValuesOf("colors", Arrays.asList("red", "blue"))
                    .containsEntry("conditions", false, true)
                    .containsEntryWithBooleanValuesOf("conditions", Arrays.asList(false, true))
                    .containsEntry("scores", 0L, 1L)
                    .containsEntryWithLongValuesOf("scores", Arrays.asList(0L, 1L))
                    .containsEntry("coins", 0.01, 0.05, 0.1)
                    .containsEntryWithDoubleValuesOf("coins", Arrays.asList(0.01, 0.05, 0.1))
                    .containsKey(AttributeKey.stringKey("bear"))
                    .containsKey("bear")
                    .containsOnly(
                        attributeEntry("bear", "mya"),
                        attributeEntry("warm", true),
                        attributeEntry("temperature", 30),
                        attributeEntry("length", 1.2),
                        attributeEntry("colors", "red", "blue"),
                        attributeEntry("conditions", false, true),
                        attributeEntry("scores", 0L, 1L),
                        attributeEntry("coins", 0.01, 0.05, 0.1)))
        .hasAttributesSatisfying(
            equalTo(AttributeKey.stringKey("bear"), "mya"),
            equalTo(AttributeKey.booleanArrayKey("conditions"), Arrays.asList(false, true)))
        .hasAttributesSatisfyingExactly(
            equalTo(AttributeKey.stringKey("bear"), "mya"),
            equalTo(AttributeKey.booleanKey("warm"), true),
            equalTo(AttributeKey.longKey("temperature"), 30L),
            equalTo(AttributeKey.doubleKey("length"), 1.2),
            equalTo(AttributeKey.stringArrayKey("colors"), Arrays.asList("red", "blue")),
            equalTo(AttributeKey.booleanArrayKey("conditions"), Arrays.asList(false, true)),
            equalTo(AttributeKey.longArrayKey("scores"), Arrays.asList(0L, 1L)),
            equalTo(AttributeKey.doubleArrayKey("coins"), Arrays.asList(0.01, 0.05, 0.1)))
        .hasTotalAttributeCount(999);
  }

  @Test
  void failure() {
    assertThatThrownBy(() -> assertThat(LOG_DATA).hasResource(Resource.empty()));
    assertThatThrownBy(
        () -> assertThat(LOG_DATA).hasInstrumentationScope(InstrumentationScopeInfo.empty()));
    assertThatThrownBy(() -> assertThat(LOG_DATA).hasEpochNanos(200));
    assertThatThrownBy(
        () ->
            assertThat(LOG_DATA)
                .hasSpanContext(
                    SpanContext.create(
                        TRACE_ID,
                        "0000000000000004",
                        TraceFlags.getDefault(),
                        TraceState.getDefault())));
    assertThatThrownBy(() -> assertThat(LOG_DATA).hasSeverity(Severity.DEBUG));
    assertThatThrownBy(() -> assertThat(LOG_DATA).hasSeverityText("warning"));
    assertThatThrownBy(() -> assertThat(LOG_DATA).hasBody("bar"));
    assertThatThrownBy(() -> assertThat(LOG_DATA).hasAttributes(Attributes.empty()))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(() -> assertThat(LOG_DATA).hasAttributes(attributeEntry("food", "burger")))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(LOG_DATA)
                    .hasAttributesSatisfying(
                        attributes ->
                            OpenTelemetryAssertions.assertThat(attributes)
                                .containsEntry("cat", "bark")))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(LOG_DATA)
                    .hasAttributesSatisfying(
                        attributes ->
                            OpenTelemetryAssertions.assertThat(attributes)
                                .containsKey(AttributeKey.stringKey("cat"))))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(LOG_DATA)
                    .hasAttributesSatisfying(
                        attributes ->
                            OpenTelemetryAssertions.assertThat(attributes).containsKey("cat")))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(LOG_DATA)
                    .hasAttributesSatisfying(
                        attributes -> OpenTelemetryAssertions.assertThat(attributes).isEmpty()))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(LOG_DATA)
                    .hasAttributesSatisfying(
                        attributes -> OpenTelemetryAssertions.assertThat(attributes).hasSize(33)))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(LOG_DATA)
                    .hasAttributesSatisfying(
                        attributes ->
                            OpenTelemetryAssertions.assertThat(attributes)
                                .hasEntrySatisfying(
                                    AttributeKey.stringKey("bear"),
                                    value -> assertThat(value).hasSize(2))))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(LOG_DATA)
                    .hasAttributesSatisfying(equalTo(AttributeKey.stringKey("bear"), "moo")))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(LOG_DATA)
                    .hasAttributesSatisfyingExactly(
                        equalTo(AttributeKey.stringKey("bear"), "mya"),
                        equalTo(AttributeKey.booleanKey("warm"), true),
                        equalTo(AttributeKey.longKey("temperature"), 30L),
                        equalTo(AttributeKey.doubleKey("length"), 1.2)))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(() -> assertThat(LOG_DATA).hasTotalAttributeCount(11))
        .isInstanceOf(AssertionError.class);
  }
}
