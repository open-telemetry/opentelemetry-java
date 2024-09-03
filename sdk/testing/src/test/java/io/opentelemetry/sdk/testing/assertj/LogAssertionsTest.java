/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.assertj;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.attributeEntry;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.equalTo;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.satisfies;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.Value;
import io.opentelemetry.api.incubator.events.EventLogger;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.logs.export.SimpleLogRecordProcessor;
import io.opentelemetry.sdk.logs.internal.SdkEventLoggerProvider;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.exporter.InMemoryLogRecordExporter;
import io.opentelemetry.sdk.testing.logs.TestLogRecordData;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

public class LogAssertionsTest {
  private static final Resource RESOURCE =
      Resource.create(Attributes.builder().put("dog", "bark").put("dog is cute", true).build());
  private static final InstrumentationScopeInfo INSTRUMENTATION_SCOPE_INFO =
      InstrumentationScopeInfo.create("instrumentation_library");
  private static final String TRACE_ID = "00000000000000010000000000000002";
  private static final String SPAN_ID = "0000000000000003";

  private static final AttributeKey<String> DOG = AttributeKey.stringKey("dog");
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
          .setTimestamp(100, TimeUnit.NANOSECONDS)
          .setObservedTimestamp(200, TimeUnit.NANOSECONDS)
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
        .hasResourceSatisfying(
            resource ->
                resource
                    .hasSchemaUrl(null)
                    .hasAttribute(DOG, "bark")
                    .hasAttributes(
                        Attributes.of(DOG, "bark", AttributeKey.booleanKey("dog is cute"), true))
                    .hasAttributes(
                        attributeEntry("dog", "bark"), attributeEntry("dog is cute", true))
                    .hasAttributesSatisfying(
                        attributes ->
                            assertThat(attributes)
                                .hasSize(2)
                                .containsEntry(AttributeKey.stringKey("dog"), "bark")
                                .hasEntrySatisfying(DOG, value -> assertThat(value).hasSize(4))
                                .hasEntrySatisfying(
                                    AttributeKey.booleanKey("dog is cute"),
                                    value -> assertThat(value).isTrue())))
        .hasResourceSatisfying(
            resource ->
                resource.hasAttributesSatisfying(satisfies(DOG, val -> val.isEqualTo("bark"))))
        .hasResourceSatisfying(
            resource ->
                resource.hasAttributesSatisfyingExactly(
                    equalTo(DOG, "bark"), equalTo(AttributeKey.booleanKey("dog is cute"), true)))
        .hasResourceSatisfying(
            resource ->
                resource.hasAttributesSatisfyingExactly(
                    satisfies(DOG, val -> val.startsWith("bar")),
                    satisfies(AttributeKey.booleanKey("dog is cute"), val -> val.isTrue())))
        .hasInstrumentationScope(INSTRUMENTATION_SCOPE_INFO)
        .hasTimestamp(100)
        .hasObservedTimestamp(200)
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
            () ->
                assertThat(LOG_DATA)
                    .hasResourceSatisfying(resource -> resource.hasSchemaUrl("http://example.com")))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(LOG_DATA)
                    .hasResourceSatisfying(resource -> resource.hasAttribute(DOG, "meow")))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(LOG_DATA)
                    .hasResourceSatisfying(
                        resource -> resource.hasAttributes(Attributes.of(DOG, "bark"))))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(LOG_DATA)
                    .hasResourceSatisfying(
                        resource -> resource.hasAttributes(attributeEntry("dog is cute", true))))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(LOG_DATA)
                    .hasResourceSatisfying(
                        resource ->
                            resource.hasAttributesSatisfying(
                                attributes -> assertThat(attributes).hasSize(1))))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
        () -> assertThat(LOG_DATA).hasInstrumentationScope(InstrumentationScopeInfo.empty()));
    assertThatThrownBy(() -> assertThat(LOG_DATA).hasTimestamp(200));
    assertThatThrownBy(() -> assertThat(LOG_DATA).hasObservedTimestamp(100));
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

  @Test
  void eventBodyAssertions() {
    InMemoryLogRecordExporter exporter = InMemoryLogRecordExporter.create();
    SdkLoggerProvider loggerProvider =
        SdkLoggerProvider.builder()
            .addLogRecordProcessor(SimpleLogRecordProcessor.create(exporter))
            .build();
    EventLogger eventLogger = SdkEventLoggerProvider.create(loggerProvider).get("test.test");
    eventLogger
        .builder("foo")
        .put("foostr", "bar")
        .put("foobool", true)
        .put("foolong", 12)
        .put("foodbl", 12.0)
        .put("foostra", "bar", "baz", "buzz")
        .put("foolonga", 9, 0, 2, 1, 0)
        .put("foodbla", 9.1, 0.2, 2.3, 1.4, 0.5)
        .put("fooboola", true, true, true, false)
        .put("fooany", Value.of("grim"))
        .emit();
    List<LogRecordData> logs = exporter.getFinishedLogRecordItems();
    assertThat(logs).hasSize(1);
    assertThat(logs.get(0))
        .hasBodyField("foostr", "bar")
        .hasBodyField("foobool", true)
        .hasBodyField("foolong", 12)
        .hasBodyField("foodbl", 12.0)
        .hasBodyField("foostra", "bar", "baz", "buzz")
        .hasBodyField("foolonga", 9, 0, 2, 1, 0)
        .hasBodyField("foodbla", 9.1, 0.2, 2.3, 1.4, 0.5)
        .hasBodyField("fooboola", true, true, true, false)
        .hasBodyField("fooany", Value.of("grim"));
  }
}
