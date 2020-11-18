/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
import io.opentelemetry.context.propagation.DefaultContextPropagators;
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
    DefaultOpenTelemetry.reset();
  }

  @AfterEach
  void after() {
    DefaultOpenTelemetry.reset();
    System.clearProperty(TracerProviderFactory.class.getName());
    System.clearProperty(MeterProviderFactory.class.getName());
  }

  @Test
  void testDefault() {
    assertThat(OpenTelemetry.getGlobalTracerProvider().getClass().getSimpleName())
        .isEqualTo("DefaultTracerProvider");
    assertThat(OpenTelemetry.getGlobalTracerProvider())
        .isSameAs(OpenTelemetry.getGlobalTracerProvider());
    assertThat(OpenTelemetry.getGlobalMeterProvider().getClass().getSimpleName())
        .isEqualTo("DefaultMeterProvider");
    assertThat(OpenTelemetry.getGlobalMeterProvider())
        .isSameAs(OpenTelemetry.getGlobalMeterProvider());
    assertThat(OpenTelemetry.getGlobalPropagators()).isInstanceOf(DefaultContextPropagators.class);
    assertThat(OpenTelemetry.getGlobalPropagators()).isSameAs(OpenTelemetry.getGlobalPropagators());
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
              (OpenTelemetry.getGlobalTracerProvider().get("")
                      instanceof FirstTracerProviderFactory)
                  || (OpenTelemetry.getGlobalTracerProvider().get("")
                      instanceof SecondTracerProviderFactory))
          .isTrue();
    } finally {
      serviceFile.delete();
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
      assertThat(OpenTelemetry.getGlobalTracerProvider().get(""))
          .isInstanceOf(SecondTracerProviderFactory.class);
    } finally {
      serviceFile.delete();
    }
  }

  @Test
  void testTracerNotFound() {
    System.setProperty(TracerProviderFactory.class.getName(), "io.does.not.exists");
    assertThrows(IllegalStateException.class, () -> OpenTelemetry.getGlobalTracer("testTracer"));
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
              (OpenTelemetry.getGlobalMeterProvider() instanceof FirstMeterProviderFactory)
                  || (OpenTelemetry.getGlobalMeterProvider() instanceof SecondMeterProviderFactory))
          .isTrue();
      assertThat(OpenTelemetry.getGlobalMeterProvider())
          .isEqualTo(OpenTelemetry.getGlobalMeterProvider());
    } finally {
      serviceFile.delete();
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
      assertThat(OpenTelemetry.getGlobalMeterProvider())
          .isInstanceOf(SecondMeterProviderFactory.class);
      assertThat(OpenTelemetry.getGlobalMeterProvider())
          .isEqualTo(OpenTelemetry.getGlobalMeterProvider());
    } finally {
      serviceFile.delete();
    }
  }

  @Test
  void testMeterNotFound() {
    System.setProperty(MeterProviderFactory.class.getName(), "io.does.not.exists");
    assertThrows(IllegalStateException.class, () -> OpenTelemetry.getGlobalMeterProvider());
  }

  @Test
  void testGlobalPropagatorsSet() {
    ContextPropagators propagators = DefaultContextPropagators.builder().build();
    OpenTelemetry.setGlobalPropagators(propagators);
    assertThat(OpenTelemetry.getGlobalPropagators()).isEqualTo(propagators);
  }

  @Test
  void testPropagatorsSet() {
    ContextPropagators propagators = DefaultContextPropagators.builder().build();
    OpenTelemetry instance = DefaultOpenTelemetry.builder().build();
    instance.setPropagators(propagators);
    assertThat(instance.getPropagators()).isEqualTo(propagators);
  }

  @Test
  void testPropagatorsSetNull() {
    assertThrows(NullPointerException.class, () -> OpenTelemetry.setGlobalPropagators(null));
  }

  private static File createService(Class<?> service, Class<?>... impls) throws IOException {
    URL location = Tracer.class.getProtectionDomain().getCodeSource().getLocation();
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
