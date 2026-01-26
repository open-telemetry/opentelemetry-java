/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.common.internal.ExceptionAttributeResolver;
import io.opentelemetry.sdk.common.internal.ScopeConfigurator;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.internal.SdkTracerProviderUtil;
import io.opentelemetry.sdk.trace.internal.TracerConfig;
import io.opentelemetry.sdk.trace.samplers.Sampler;
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
  private SdkTracerProvider tracerProvider;

  @BeforeEach
  void setUp() {
    tracerProvider = SdkTracerProvider.builder().addSpanProcessor(spanProcessor).build();
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
        Resource.create(Attributes.of(stringKey("service.name"), "mySpecialService"));

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
    assertThat(tracerProvider.get("test")).isInstanceOf(SdkTracer.class);
  }

  @Test
  void getSameInstanceForSameName_WithoutVersion() {
    assertThat(tracerProvider.get("test")).isSameAs(tracerProvider.get("test"));
    assertThat(tracerProvider.get("test"))
        .isSameAs(tracerProvider.get("test", null))
        .isSameAs(tracerProvider.tracerBuilder("test").build());
  }

  @Test
  void getSameInstanceForSameName_WithVersion() {
    assertThat(tracerProvider.get("test", "version"))
        .isSameAs(tracerProvider.get("test", "version"))
        .isSameAs(
            tracerProvider.tracerBuilder("test").setInstrumentationVersion("version").build());
  }

  @Test
  void getSameInstanceForSameName_WithVersionAndSchema() {
    assertThat(
            tracerProvider
                .tracerBuilder("test")
                .setInstrumentationVersion("version")
                .setSchemaUrl("http://url")
                .build())
        .isSameAs(
            tracerProvider
                .tracerBuilder("test")
                .setInstrumentationVersion("version")
                .setSchemaUrl("http://url")
                .build());
  }

  @Test
  void propagatesInstrumentationScopeInfoToTracer() {
    InstrumentationScopeInfo expected =
        InstrumentationScopeInfo.builder("theName")
            .setVersion("theVersion")
            .setSchemaUrl("http://url")
            .build();
    Tracer tracer =
        tracerProvider
            .tracerBuilder(expected.getName())
            .setInstrumentationVersion(expected.getVersion())
            .setSchemaUrl(expected.getSchemaUrl())
            .build();
    assertThat(((SdkTracer) tracer).getInstrumentationScopeInfo()).isEqualTo(expected);
  }

  @Test
  void propagatesEnablementToTracerDirectly() {
    propagatesEnablementToTracer(true);
  }

  @Test
  void propagatesEnablementToTracerByUtil() {
    propagatesEnablementToTracer(false);
  }

  void propagatesEnablementToTracer(boolean directly) {
    SdkTracer tracer = (SdkTracer) tracerProvider.get("test");
    boolean isEnabled = tracer.isEnabled();
    ScopeConfigurator<TracerConfig> flipConfigurator =
        new ScopeConfigurator<TracerConfig>() {
          @Override
          public TracerConfig apply(InstrumentationScopeInfo scopeInfo) {
            return isEnabled ? TracerConfig.disabled() : TracerConfig.enabled();
          }
        };
    // all in the same thread, so should see enablement change immediately
    if (directly) {
      tracerProvider.setTracerConfigurator(flipConfigurator);
    } else {
      SdkTracerProviderUtil.setTracerConfigurator(tracerProvider, flipConfigurator);
    }
    assertThat(tracer.isEnabled()).isEqualTo(!isEnabled);
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
    tracerProvider.shutdown();
    Mockito.verify(spanProcessor, Mockito.times(1)).shutdown();
  }

  @Test
  void close() {
    tracerProvider.close();
    Mockito.verify(spanProcessor, Mockito.times(1)).shutdown();
  }

  @Test
  void forceFlush() {
    tracerProvider.forceFlush();
    Mockito.verify(spanProcessor, Mockito.times(1)).forceFlush();
  }

  @Test
  @SuppressLogger(SdkTracerProvider.class)
  void shutdownTwice_OnlyFlushSpanProcessorOnce() {
    tracerProvider.shutdown();
    Mockito.verify(spanProcessor, Mockito.times(1)).shutdown();
    tracerProvider.shutdown(); // the second call will be ignored
    Mockito.verify(spanProcessor, Mockito.times(1)).shutdown();
  }

  @Test
  void returnNoopSpanAfterShutdown() {
    tracerProvider.shutdown();
    Span span = tracerProvider.get("noop").spanBuilder("span").startSpan();
    assertThat(span.getSpanContext().isValid()).isFalse();
    span.end();
  }

  @Test
  void suppliesDefaultTracerForNullName() {
    SdkTracer tracer = (SdkTracer) tracerProvider.get(null);
    assertThat(tracer.getInstrumentationScopeInfo().getName())
        .isEqualTo(SdkTracerProvider.DEFAULT_TRACER_NAME);

    tracer = (SdkTracer) tracerProvider.get(null, null);
    assertThat(tracer.getInstrumentationScopeInfo().getName())
        .isEqualTo(SdkTracerProvider.DEFAULT_TRACER_NAME);
  }

  @Test
  void suppliesDefaultTracerForEmptyName() {
    SdkTracer tracer = (SdkTracer) tracerProvider.get("");
    assertThat(tracer.getInstrumentationScopeInfo().getName())
        .isEqualTo(SdkTracerProvider.DEFAULT_TRACER_NAME);

    tracer = (SdkTracer) tracerProvider.get("", "");
    assertThat(tracer.getInstrumentationScopeInfo().getName())
        .isEqualTo(SdkTracerProvider.DEFAULT_TRACER_NAME);
  }

  @Test
  void exceptionAttributeResolver() {
    int maxAttributeLength = 5;
    SdkTracerProviderBuilder builder =
        SdkTracerProvider.builder()
            .addSpanProcessor(spanProcessor)
            .setSpanLimits(
                SpanLimits.builder().setMaxAttributeValueLength(maxAttributeLength).build());
    ExceptionAttributeResolver exceptionAttributeResolver =
        spy(ExceptionAttributeResolver.getDefault());
    SdkTracerProviderUtil.setExceptionAttributeResolver(builder, exceptionAttributeResolver);

    Exception exception = new Exception("error");
    builder.build().get("tracer").spanBuilder("span").startSpan().recordException(exception).end();

    verify(exceptionAttributeResolver).setExceptionAttributes(any(), any(), eq(maxAttributeLength));
  }
}
