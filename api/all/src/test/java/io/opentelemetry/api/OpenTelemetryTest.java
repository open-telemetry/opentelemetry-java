/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.opentelemetry.api.metrics.BatchRecorder;
import io.opentelemetry.api.metrics.DoubleCounter;
import io.opentelemetry.api.metrics.DoubleSumObserver;
import io.opentelemetry.api.metrics.DoubleUpDownCounter;
import io.opentelemetry.api.metrics.DoubleUpDownSumObserver;
import io.opentelemetry.api.metrics.DoubleValueObserver;
import io.opentelemetry.api.metrics.DoubleValueRecorder;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.LongSumObserver;
import io.opentelemetry.api.metrics.LongUpDownCounter;
import io.opentelemetry.api.metrics.LongUpDownSumObserver;
import io.opentelemetry.api.metrics.LongValueObserver;
import io.opentelemetry.api.metrics.LongValueRecorder;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.TracerProvider;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.spi.metrics.MeterProviderFactory;
import io.opentelemetry.spi.trace.TracerProviderFactory;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import javax.annotation.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class OpenTelemetryTest {

  @BeforeAll
  static void beforeClass() {
    GlobalOpenTelemetry.reset();
  }

  @AfterEach
  void after() {
    GlobalOpenTelemetry.reset();
    System.clearProperty(TracerProviderFactory.class.getName());
    System.clearProperty(MeterProviderFactory.class.getName());
  }

  @Test
  void testDefault() {
    assertThat(GlobalOpenTelemetry.getTracerProvider().getClass().getSimpleName())
        .isEqualTo("DefaultTracerProvider");
    assertThat(GlobalOpenTelemetry.getTracerProvider())
        .isSameAs(GlobalOpenTelemetry.getTracerProvider());
    assertThat(GlobalOpenTelemetry.getMeterProvider().getClass().getSimpleName())
        .isEqualTo("DefaultMeterProvider");
    assertThat(GlobalOpenTelemetry.getMeterProvider())
        .isSameAs(GlobalOpenTelemetry.getMeterProvider());
    assertThat(GlobalOpenTelemetry.getPropagators()).isSameAs(GlobalOpenTelemetry.getPropagators());
  }

  @Test
  void builder() {
    MeterProvider meterProvider = mock(MeterProvider.class);
    TracerProvider tracerProvider = mock(TracerProvider.class);
    ContextPropagators contextPropagators = mock(ContextPropagators.class);
    OpenTelemetry openTelemetry =
        DefaultOpenTelemetry.builder()
            .setMeterProvider(meterProvider)
            .setTracerProvider(tracerProvider)
            .setPropagators(contextPropagators)
            .build();

    assertThat(openTelemetry).isNotNull();
    assertThat(openTelemetry.getMeterProvider()).isSameAs(meterProvider);
    assertThat(openTelemetry.getTracerProvider()).isSameAs(tracerProvider);
    assertThat(openTelemetry.getPropagators()).isSameAs(contextPropagators);
  }

  @Test
  void testTracerLoadArbitrary() throws IOException {
    File serviceFile =
        createService(
            TracerProviderFactory.class,
            FirstTracerProviderFactory.class,
            SecondTracerProviderFactory.class);
    try {
      assertThat(
              (GlobalOpenTelemetry.getTracerProvider().get("")
                      instanceof FirstTracerProviderFactory)
                  || (GlobalOpenTelemetry.getTracerProvider().get("")
                      instanceof SecondTracerProviderFactory))
          .isTrue();
    } finally {
      assertThat(serviceFile.delete()).isTrue();
    }
  }

  @Test
  void testTracerSystemProperty() throws IOException {
    File serviceFile =
        createService(
            TracerProviderFactory.class,
            FirstTracerProviderFactory.class,
            SecondTracerProviderFactory.class);
    System.setProperty(
        TracerProviderFactory.class.getName(), SecondTracerProviderFactory.class.getName());
    try {
      assertThat(GlobalOpenTelemetry.getTracerProvider().get(""))
          .isInstanceOf(SecondTracerProviderFactory.class);
    } finally {
      assertThat(serviceFile.delete()).isTrue();
    }
  }

  @Test
  void testTracerNotFound() {
    System.setProperty(TracerProviderFactory.class.getName(), "io.does.not.exists");
    assertThrows(IllegalStateException.class, () -> GlobalOpenTelemetry.getTracer("testTracer"));
  }

  @Test
  void testMeterLoadArbitrary() throws IOException {
    File serviceFile =
        createService(
            MeterProviderFactory.class,
            FirstMeterProviderFactory.class,
            SecondMeterProviderFactory.class);
    try {
      assertThat(
              (GlobalOpenTelemetry.getMeterProvider() instanceof FirstMeterProviderFactory)
                  || (GlobalOpenTelemetry.getMeterProvider() instanceof SecondMeterProviderFactory))
          .isTrue();
      assertThat(GlobalOpenTelemetry.getMeterProvider())
          .isEqualTo(GlobalOpenTelemetry.getMeterProvider());
    } finally {
      assertThat(serviceFile.delete()).isTrue();
    }
  }

  @Test
  void testMeterSystemProperty() throws IOException {
    File serviceFile =
        createService(
            MeterProviderFactory.class,
            FirstMeterProviderFactory.class,
            SecondMeterProviderFactory.class);
    System.setProperty(
        MeterProviderFactory.class.getName(), SecondMeterProviderFactory.class.getName());
    try {
      assertThat(GlobalOpenTelemetry.getMeterProvider())
          .isInstanceOf(SecondMeterProviderFactory.class);
      assertThat(GlobalOpenTelemetry.getMeterProvider())
          .isEqualTo(GlobalOpenTelemetry.getMeterProvider());
    } finally {
      assertThat(serviceFile.delete()).isTrue();
    }
  }

  @Test
  void testMeterNotFound() {
    System.setProperty(MeterProviderFactory.class.getName(), "io.does.not.exists");
    assertThrows(IllegalStateException.class, GlobalOpenTelemetry::getMeterProvider);
  }

  @Test
  @SuppressWarnings("deprecation") // tested deprecated code
  void testGlobalPropagatorsSet() {
    ContextPropagators propagators = ContextPropagators.noop();
    GlobalOpenTelemetry.setPropagators(propagators);
    assertThat(GlobalOpenTelemetry.getPropagators()).isEqualTo(propagators);
  }

  @Test
  @SuppressWarnings("deprecation") // tested deprecated code
  void testPropagatorsSet() {
    ContextPropagators propagators = ContextPropagators.noop();
    OpenTelemetry instance = DefaultOpenTelemetry.builder().build();
    instance.setPropagators(propagators);
    assertThat(instance.getPropagators()).isEqualTo(propagators);
  }

  @Test
  void independentNonGlobalTracers() {
    TracerProvider provider1 = mock(TracerProvider.class);
    Tracer tracer1 = mock(Tracer.class);
    when(provider1.get("foo")).thenReturn(tracer1);
    when(provider1.get("foo", "1.0")).thenReturn(tracer1);
    OpenTelemetry otel1 = DefaultOpenTelemetry.builder().setTracerProvider(provider1).build();
    TracerProvider provider2 = mock(TracerProvider.class);
    Tracer tracer2 = mock(Tracer.class);
    when(provider2.get("foo")).thenReturn(tracer2);
    when(provider2.get("foo", "1.0")).thenReturn(tracer2);
    OpenTelemetry otel2 = DefaultOpenTelemetry.builder().setTracerProvider(provider2).build();

    assertThat(otel1.getTracer("foo")).isSameAs(tracer1);
    assertThat(otel1.getTracer("foo", "1.0")).isSameAs(tracer1);
    assertThat(otel2.getTracer("foo")).isSameAs(tracer2);
    assertThat(otel2.getTracer("foo", "1.0")).isSameAs(tracer2);
  }

  @Test
  void independentNonGlobalMeters() {
    MeterProvider provider1 = mock(MeterProvider.class);
    Meter meter1 = mock(Meter.class);
    when(provider1.get("foo")).thenReturn(meter1);
    when(provider1.get("foo", "1.0")).thenReturn(meter1);
    OpenTelemetry otel1 = DefaultOpenTelemetry.builder().setMeterProvider(provider1).build();
    MeterProvider provider2 = mock(MeterProvider.class);
    Meter meter2 = mock(Meter.class);
    when(provider2.get("foo")).thenReturn(meter2);
    when(provider2.get("foo", "1.0")).thenReturn(meter2);
    OpenTelemetry otel2 = DefaultOpenTelemetry.builder().setMeterProvider(provider2).build();

    assertThat(otel1.getMeter("foo")).isSameAs(meter1);
    assertThat(otel1.getMeter("foo", "1.0")).isSameAs(meter1);
    assertThat(otel2.getMeter("foo")).isSameAs(meter2);
    assertThat(otel2.getMeter("foo", "1.0")).isSameAs(meter2);
  }

  @Test
  void independentNonGlobalPropagators() {
    ContextPropagators propagators1 = mock(ContextPropagators.class);
    OpenTelemetry otel1 = DefaultOpenTelemetry.builder().setPropagators(propagators1).build();
    ContextPropagators propagators2 = mock(ContextPropagators.class);
    OpenTelemetry otel2 = DefaultOpenTelemetry.builder().setPropagators(propagators2).build();

    assertThat(otel1.getPropagators()).isSameAs(propagators1);
    assertThat(otel2.getPropagators()).isSameAs(propagators2);
  }

  @Test
  @SuppressWarnings("deprecation") // tested deprecated code
  void testPropagatorsSetNull() {
    assertThrows(NullPointerException.class, () -> GlobalOpenTelemetry.setPropagators(null));
  }

  private static File createService(Class<?> service, Class<?>... impls) throws IOException {
    URL location = OpenTelemetryTest.class.getProtectionDomain().getCodeSource().getLocation();
    File file = new File(location.getPath() + "META-INF/services/" + service.getName());
    file.getParentFile().mkdirs();

    @SuppressWarnings("DefaultCharset")
    Writer output = new FileWriter(file);
    for (Class<?> impl : impls) {
      output.write(impl.getName());
      output.write(System.getProperty("line.separator"));
    }
    output.close();
    return file;
  }

  public static class SecondTracerProviderFactory extends FirstTracerProviderFactory {

    @Override
    public Tracer get(String instrumentationName) {
      return new SecondTracerProviderFactory();
    }

    @Override
    public Tracer get(String instrumentationName, String instrumentationVersion) {
      return get(instrumentationName);
    }

    @Override
    public TracerProvider create() {
      return new SecondTracerProviderFactory();
    }
  }

  public static class FirstTracerProviderFactory
      implements Tracer, TracerProvider, TracerProviderFactory {

    @Override
    public Tracer get(String instrumentationName) {
      return new FirstTracerProviderFactory();
    }

    @Override
    public Tracer get(String instrumentationName, String instrumentationVersion) {
      return get(instrumentationName);
    }

    @Nullable
    @Override
    public SpanBuilder spanBuilder(String spanName) {
      return null;
    }

    @Override
    public TracerProvider create() {
      return new FirstTracerProviderFactory();
    }
  }

  public static class SecondMeterProviderFactory extends FirstMeterProviderFactory {

    @Override
    public Meter get(String instrumentationName) {
      return new SecondMeterProviderFactory();
    }

    @Override
    public Meter get(String instrumentationName, String instrumentationVersion) {
      return get(instrumentationName);
    }

    @Override
    public MeterProvider create() {
      return new SecondMeterProviderFactory();
    }
  }

  public static class FirstMeterProviderFactory
      implements Meter, MeterProviderFactory, MeterProvider {

    @Override
    public MeterProvider create() {
      return new FirstMeterProviderFactory();
    }

    @Nullable
    @Override
    public DoubleCounter.Builder doubleCounterBuilder(String name) {
      return null;
    }

    @Nullable
    @Override
    public LongCounter.Builder longCounterBuilder(String name) {
      return null;
    }

    @Nullable
    @Override
    public DoubleUpDownCounter.Builder doubleUpDownCounterBuilder(String name) {
      return null;
    }

    @Nullable
    @Override
    public LongUpDownCounter.Builder longUpDownCounterBuilder(String name) {
      return null;
    }

    @Nullable
    @Override
    public DoubleValueRecorder.Builder doubleValueRecorderBuilder(String name) {
      return null;
    }

    @Nullable
    @Override
    public LongValueRecorder.Builder longValueRecorderBuilder(String name) {
      return null;
    }

    @Nullable
    @Override
    public DoubleSumObserver.Builder doubleSumObserverBuilder(String name) {
      return null;
    }

    @Nullable
    @Override
    public LongSumObserver.Builder longSumObserverBuilder(String name) {
      return null;
    }

    @Nullable
    @Override
    public DoubleUpDownSumObserver.Builder doubleUpDownSumObserverBuilder(String name) {
      return null;
    }

    @Nullable
    @Override
    public LongUpDownSumObserver.Builder longUpDownSumObserverBuilder(String name) {
      return null;
    }

    @Nullable
    @Override
    public DoubleValueObserver.Builder doubleValueObserverBuilder(String name) {
      return null;
    }

    @Nullable
    @Override
    public LongValueObserver.Builder longValueObserverBuilder(String name) {
      return null;
    }

    @Nullable
    @Override
    public BatchRecorder newBatchRecorder(String... keyValuePairs) {
      return null;
    }

    @Override
    public Meter get(String instrumentationName) {
      return new FirstMeterProviderFactory();
    }

    @Override
    public Meter get(String instrumentationName, String instrumentationVersion) {
      return get(instrumentationName);
    }
  }
}
