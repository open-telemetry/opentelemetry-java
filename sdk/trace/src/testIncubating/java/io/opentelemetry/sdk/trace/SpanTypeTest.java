/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.api.incubator.trace.ExtendedSpanBuilder;
import io.opentelemetry.api.incubator.trace.ExtendedTracer;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.sdk.trace.internal.ExtendedReadableSpan;
import io.opentelemetry.sdk.trace.internal.ExtendedSpanData;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.event.Level;

class SpanTypeTest {

  private static final String LONGEST_VALID_TYPE = "a" + repeat('b', 254);

  @RegisterExtension
  LogCapturer logs = LogCapturer.create().captureForType(SdkSpanBuilder.class, Level.WARN);

  private final InMemorySpanExporter exporter = InMemorySpanExporter.create();
  private SdkTracerProvider tracerProvider;
  private ExtendedTracer tracer;

  @BeforeEach
  void setUp() {
    tracerProvider =
        SdkTracerProvider.builder().addSpanProcessor(SimpleSpanProcessor.create(exporter)).build();
    tracer = (ExtendedTracer) tracerProvider.get("test");
  }

  @AfterEach
  void tearDown() {
    tracerProvider.shutdown();
  }

  @Test
  void typeIsExposedOnReadableSpanAndSpanData() {
    Span span = tracer.spanBuilder("GET /orders").setType("http.server.request").startSpan();
    assertThat(((ExtendedReadableSpan) span).getType()).isEqualTo("http.server.request");
    span.end();

    assertThat(exportedType()).isEqualTo("http.server.request");
  }

  @Test
  void typeIsNullWhenNotSet() {
    tracer.spanBuilder("span").startSpan().end();

    assertThat(exportedType()).isNull();
  }

  @Test
  void emptyTypeIsIgnored() {
    tracer.spanBuilder("span").setType("").startSpan().end();

    assertThat(exportedType()).isNull();
    logs.assertDoesNotContain("Span type");
  }

  @Test
  void emptyTypeDoesNotOverridePreviousType() {
    tracer.spanBuilder("span").setType("db.client.call").setType("").startSpan().end();

    assertThat(exportedType()).isEqualTo("db.client.call");
  }

  @Test
  void nullTypeIsIgnored() {
    tracer.spanBuilder("span").setType("db.client.call").setType(null).startSpan().end();

    assertThat(exportedType()).isEqualTo("db.client.call");
  }

  @Test
  void lastTypeWins() {
    tracer.spanBuilder("span").setType("first").setType("second").startSpan().end();

    assertThat(exportedType()).isEqualTo("second");
  }

  @Test
  void invalidTypeIsDroppedAndLogged() {
    tracer.spanBuilder("span").setType("1invalid").startSpan().end();

    assertThat(exportedType()).isNull();
    logs.assertContains("Span type \"1invalid\" is invalid, creating span without a type.");
  }

  @Test
  void setTypeReturnsSameBuilder() {
    ExtendedSpanBuilder builder = tracer.spanBuilder("span");
    assertThat(builder.setType("http.client.request")).isSameAs(builder);
  }

  @ParameterizedTest
  @MethodSource("typeSyntaxCases")
  void typeSyntax(String type, boolean expectedValid) {
    assertThat(SdkSpanBuilder.isValidType(type)).isEqualTo(expectedValid);
  }

  private static Stream<Arguments> typeSyntaxCases() {
    return Stream.of(
        Arguments.argumentSet("empty", "", true),
        Arguments.argumentSet("single letter", "a", true),
        Arguments.argumentSet("uppercase", "HTTP.Server", true),
        Arguments.argumentSet("semconv style", "messaging.consumer.process", true),
        Arguments.argumentSet("all allowed chars", "a0_.-/Z9", true),
        Arguments.argumentSet("max length", LONGEST_VALID_TYPE, true),
        Arguments.argumentSet("over max length", LONGEST_VALID_TYPE + "c", false),
        Arguments.argumentSet("starts with digit", "1abc", false),
        Arguments.argumentSet("starts with underscore", "_abc", false),
        Arguments.argumentSet("starts with dot", ".abc", false),
        Arguments.argumentSet("starts with dash", "-abc", false),
        Arguments.argumentSet("starts with slash", "/abc", false),
        Arguments.argumentSet("space", "a b", false),
        Arguments.argumentSet("colon", "a:b", false),
        Arguments.argumentSet("non-ascii letter", "café", false),
        Arguments.argumentSet("non-ascii first char", "éa", false));
  }

  @Nullable
  private String exportedType() {
    assertThat(exporter.getFinishedSpanItems()).hasSize(1);
    return ((ExtendedSpanData) exporter.getFinishedSpanItems().get(0)).getType();
  }

  private static String repeat(char c, int count) {
    StringBuilder sb = new StringBuilder(count);
    for (int i = 0; i < count; i++) {
      sb.append(c);
    }
    return sb.toString();
  }
}
