/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import static io.opentelemetry.sdk.common.ScopeConfiguratorBuilder.nameEquals;
import static io.opentelemetry.sdk.common.ScopeConfiguratorBuilder.nameMatchesGlob;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static io.opentelemetry.sdk.trace.TracerConfig.defaultConfig;
import static io.opentelemetry.sdk.trace.TracerConfig.disabled;
import static io.opentelemetry.sdk.trace.TracerConfig.enabled;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.common.ScopeConfigurator;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class TracerConfigTest {

  @Test
  void disableScopes() throws InterruptedException {
    InMemorySpanExporter exporter = InMemorySpanExporter.create();
    SdkTracerProvider tracerProvider =
        SdkTracerProvider.builder()
            // Disable tracerB. Since tracers are enabled by default, tracerA and tracerC are
            // enabled.
            .addTracerConfiguratorCondition(nameEquals("tracerB"), disabled())
            .addSpanProcessor(SimpleSpanProcessor.create(exporter))
            .build();

    Tracer tracerA = tracerProvider.get("tracerA");
    Tracer tracerB = tracerProvider.get("tracerB");
    Tracer tracerC = tracerProvider.get("tracerC");

    Span parent;
    Span child;
    Span grandchild;

    parent = tracerA.spanBuilder("parent").startSpan();
    try (Scope parentScope = parent.makeCurrent()) {
      parent.setAttribute("a", "1");
      child = tracerB.spanBuilder("child").startSpan();
      // tracerB is disabled and should behave the same as noop tracer
      assertThat(child.getSpanContext()).isEqualTo(parent.getSpanContext());
      assertThat(child.isRecording()).isFalse();
      try (Scope childScope = child.makeCurrent()) {
        child.setAttribute("b", "1");
        grandchild = tracerC.spanBuilder("grandchild").startSpan();
        try (Scope grandchildScope = grandchild.makeCurrent()) {
          grandchild.setAttribute("c", "1");
          Thread.sleep(100);
        } finally {
          grandchild.end();
        }
      } finally {
        child.end();
      }
    } finally {
      parent.end();
    }

    // Only contain tracerA:parent and tracerC:child should be seen
    // tracerC:grandchild should list tracerA:parent as its parent
    assertThat(exporter.getFinishedSpanItems())
        .satisfiesExactlyInAnyOrder(
            spanData ->
                assertThat(spanData)
                    .hasInstrumentationScopeInfo(InstrumentationScopeInfo.create("tracerA"))
                    .hasName("parent")
                    .hasSpanId(parent.getSpanContext().getSpanId())
                    .hasParentSpanId(SpanId.getInvalid())
                    .hasAttributes(Attributes.builder().put("a", "1").build()),
            spanData ->
                assertThat(spanData)
                    .hasInstrumentationScopeInfo(InstrumentationScopeInfo.create("tracerC"))
                    .hasName("grandchild")
                    .hasSpanId(grandchild.getSpanContext().getSpanId())
                    .hasParentSpanId(parent.getSpanContext().getSpanId())
                    .hasAttributes(Attributes.builder().put("c", "1").build()));
  }

  @ParameterizedTest
  @MethodSource("tracerConfiguratorArgs")
  void tracerConfigurator(
      ScopeConfigurator<TracerConfig> tracerConfigurator,
      InstrumentationScopeInfo scope,
      TracerConfig expectedTracerConfig) {
    TracerConfig tracerConfig = tracerConfigurator.apply(scope);
    tracerConfig = tracerConfig == null ? defaultConfig() : tracerConfig;
    assertThat(tracerConfig).isEqualTo(expectedTracerConfig);
  }

  private static final InstrumentationScopeInfo scopeCat = InstrumentationScopeInfo.create("cat");
  private static final InstrumentationScopeInfo scopeDog = InstrumentationScopeInfo.create("dog");
  private static final InstrumentationScopeInfo scopeDuck = InstrumentationScopeInfo.create("duck");

  private static Stream<Arguments> tracerConfiguratorArgs() {
    ScopeConfigurator<TracerConfig> defaultConfigurator =
        TracerConfig.configuratorBuilder().build();
    ScopeConfigurator<TracerConfig> disableCat =
        TracerConfig.configuratorBuilder()
            .addCondition(nameEquals("cat"), disabled())
            // Second matching rule for cat should be ignored
            .addCondition(nameEquals("cat"), enabled())
            .build();
    ScopeConfigurator<TracerConfig> disableStartsWithD =
        TracerConfig.configuratorBuilder().addCondition(nameMatchesGlob("d*"), disabled()).build();
    ScopeConfigurator<TracerConfig> enableCat =
        TracerConfig.configuratorBuilder()
            .setDefault(disabled())
            .addCondition(nameEquals("cat"), enabled())
            // Second matching rule for cat should be ignored
            .addCondition(nameEquals("cat"), disabled())
            .build();
    ScopeConfigurator<TracerConfig> enableStartsWithD =
        TracerConfig.configuratorBuilder()
            .setDefault(disabled())
            .addCondition(nameMatchesGlob("d*"), TracerConfig.enabled())
            .build();

    return Stream.of(
        // default
        Arguments.of(defaultConfigurator, scopeCat, defaultConfig()),
        Arguments.of(defaultConfigurator, scopeDog, defaultConfig()),
        Arguments.of(defaultConfigurator, scopeDuck, defaultConfig()),
        // default enabled, disable cat
        Arguments.of(disableCat, scopeCat, disabled()),
        Arguments.of(disableCat, scopeDog, enabled()),
        Arguments.of(disableCat, scopeDuck, enabled()),
        // default enabled, disable pattern
        Arguments.of(disableStartsWithD, scopeCat, enabled()),
        Arguments.of(disableStartsWithD, scopeDog, disabled()),
        Arguments.of(disableStartsWithD, scopeDuck, disabled()),
        // default disabled, enable cat
        Arguments.of(enableCat, scopeCat, enabled()),
        Arguments.of(enableCat, scopeDog, disabled()),
        Arguments.of(enableCat, scopeDuck, disabled()),
        // default disabled, enable pattern
        Arguments.of(enableStartsWithD, scopeCat, disabled()),
        Arguments.of(enableStartsWithD, scopeDog, enabled()),
        Arguments.of(enableStartsWithD, scopeDuck, enabled()));
  }
}
