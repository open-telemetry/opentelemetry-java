/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/** Unit tests for {@link SdkTracerProvider}. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SdkTracerProviderTest {
  @Mock private SpanProcessor spanProcessor;
  private SdkTracerProvider tracerFactory;

  @BeforeEach
  void setUp() {
    tracerFactory = SdkTracerProvider.builder().addSpanProcessor(spanProcessor).build();
    when(spanProcessor.forceFlush()).thenReturn(CompletableResultCode.ofSuccess());
    when(spanProcessor.shutdown()).thenReturn(CompletableResultCode.ofSuccess());
  }

  @Test
  void builder_defaultResource() {
    Resource resourceWithDefaults = Resource.getDefault();

    SdkTracerProvider tracerProvider =
        SdkTracerProvider.builder()
            .setClock(mock(Clock.class))
            .setIdGenerator(mock(IdGenerator.class))
            .build();

    assertThat(tracerProvider).isNotNull();
    assertThat(tracerProvider)
        .extracting("sharedState")
        .hasFieldOrPropertyWithValue("resource", resourceWithDefaults);
  }

  @Test
  void builder_defaultSampler() {
    assertThat(SdkTracerProvider.builder().build().getSampler())
        .isEqualTo(Sampler.parentBased(Sampler.alwaysOn()));
  }

  @Test
  void builder_configureSampler() {
    assertThat(SdkTracerProvider.builder().setSampler(Sampler.alwaysOff()).build().getSampler())
        .isEqualTo(Sampler.alwaysOff());
  }

  @Test
  void builder_configureSampler_null() {
    assertThatThrownBy(() -> SdkTracerProvider.builder().setSampler(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("sampler");
  }

  @Test
  void builder_serviceNameProvided() {
    Resource resource =
        Resource.create(Attributes.of(ResourceAttributes.SERVICE_NAME, "mySpecialService"));

    SdkTracerProvider tracerProvider =
        SdkTracerProvider.builder()
            .setClock(mock(Clock.class))
            .setResource(resource)
            .setIdGenerator(mock(IdGenerator.class))
            .build();

    assertThat(tracerProvider).isNotNull();
    assertThat(tracerProvider)
        .extracting("sharedState")
        .hasFieldOrPropertyWithValue("resource", resource);
  }

  @Test
  void builder_NullSpanLimits() {
    assertThatThrownBy(() -> SdkTracerProvider.builder().setSpanLimits((SpanLimits) null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("spanLimits");
  }

  @Test
  void builder_NullSpanLimitsSupplier() {
    assertThatThrownBy(() -> SdkTracerProvider.builder().setSpanLimits((Supplier<SpanLimits>) null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("spanLimitsSupplier");
  }

  @Test
  void builder_NullClock() {
    assertThatThrownBy(() -> SdkTracerProvider.builder().setClock(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("clock");
  }

  @Test
  void builder_NullResource() {
    assertThatThrownBy(() -> SdkTracerProvider.builder().setResource(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("resource");
  }

  @Test
  void builder_NullIdsGenerator() {
    assertThatThrownBy(() -> SdkTracerProvider.builder().setIdGenerator(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("idGenerator");
  }

  @Test
  void defaultGet() {
    assertThat(tracerFactory.get("test")).isInstanceOf(SdkTracer.class);
  }

  @Test
  void getSameInstanceForSameName_WithoutVersion() {
    assertThat(tracerFactory.get("test")).isSameAs(tracerFactory.get("test"));
    assertThat(tracerFactory.get("test")).isSameAs(tracerFactory.get("test", null));
  }

  @Test
  void getSameInstanceForSameName_WithVersion() {
    assertThat(tracerFactory.get("test", "version")).isSameAs(tracerFactory.get("test", "version"));
  }

  @Test
  void propagatesInstrumentationLibraryInfoToTracer() {
    InstrumentationLibraryInfo expected =
        InstrumentationLibraryInfo.create("theName", "theVersion");
    Tracer tracer = tracerFactory.get(expected.getName(), expected.getVersion());
    assertThat(((SdkTracer) tracer).getInstrumentationLibraryInfo()).isEqualTo(expected);
  }

  @Test
  void build_SpanLimits() {
    SpanLimits initialSpanLimits = SpanLimits.builder().build();
    SdkTracerProvider sdkTracerProvider =
        SdkTracerProvider.builder().setSpanLimits(initialSpanLimits).build();

    assertThat(sdkTracerProvider.getSpanLimits()).isSameAs(initialSpanLimits);
  }

  @Test
  void shutdown() {
    tracerFactory.shutdown();
    Mockito.verify(spanProcessor, Mockito.times(1)).shutdown();
  }

  @Test
  void close() {
    tracerFactory.close();
    Mockito.verify(spanProcessor, Mockito.times(1)).shutdown();
  }

  @Test
  void forceFlush() {
    tracerFactory.forceFlush();
    Mockito.verify(spanProcessor, Mockito.times(1)).forceFlush();
  }

  @Test
  void shutdownTwice_OnlyFlushSpanProcessorOnce() {
    tracerFactory.shutdown();
    Mockito.verify(spanProcessor, Mockito.times(1)).shutdown();
    tracerFactory.shutdown(); // the second call will be ignored
    Mockito.verify(spanProcessor, Mockito.times(1)).shutdown();
  }

  @Test
  void returnNoopSpanAfterShutdown() {
    tracerFactory.shutdown();
    Span span = tracerFactory.get("noop").spanBuilder("span").startSpan();
    assertThat(span.getSpanContext().isValid()).isFalse();
    span.end();
  }

  @Test
  void suppliesDefaultTracerForNullName() {
    SdkTracer tracer = (SdkTracer) tracerFactory.get(null);
    assertThat(tracer.getInstrumentationLibraryInfo().getName())
        .isEqualTo(SdkTracerProvider.DEFAULT_TRACER_NAME);

    tracer = (SdkTracer) tracerFactory.get(null, null);
    assertThat(tracer.getInstrumentationLibraryInfo().getName())
        .isEqualTo(SdkTracerProvider.DEFAULT_TRACER_NAME);
  }

  @Test
  void suppliesDefaultTracerForEmptyName() {
    SdkTracer tracer = (SdkTracer) tracerFactory.get("");
    assertThat(tracer.getInstrumentationLibraryInfo().getName())
        .isEqualTo(SdkTracerProvider.DEFAULT_TRACER_NAME);

    tracer = (SdkTracer) tracerFactory.get("", "");
    assertThat(tracer.getInstrumentationLibraryInfo().getName())
        .isEqualTo(SdkTracerProvider.DEFAULT_TRACER_NAME);
  }
}
