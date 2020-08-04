/*
 * Copyright 2019, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.DefaultContextPropagators;
import io.opentelemetry.correlationcontext.CorrelationContext;
import io.opentelemetry.correlationcontext.CorrelationContextManager;
import io.opentelemetry.correlationcontext.DefaultCorrelationContextManager;
import io.opentelemetry.correlationcontext.spi.CorrelationContextManagerFactory;
import io.opentelemetry.metrics.BatchRecorder;
import io.opentelemetry.metrics.DefaultMeterProvider;
import io.opentelemetry.metrics.DoubleCounter;
import io.opentelemetry.metrics.DoubleSumObserver;
import io.opentelemetry.metrics.DoubleUpDownCounter;
import io.opentelemetry.metrics.DoubleUpDownSumObserver;
import io.opentelemetry.metrics.DoubleValueObserver;
import io.opentelemetry.metrics.DoubleValueRecorder;
import io.opentelemetry.metrics.LongCounter;
import io.opentelemetry.metrics.LongSumObserver;
import io.opentelemetry.metrics.LongUpDownCounter;
import io.opentelemetry.metrics.LongUpDownSumObserver;
import io.opentelemetry.metrics.LongValueObserver;
import io.opentelemetry.metrics.LongValueRecorder;
import io.opentelemetry.metrics.Meter;
import io.opentelemetry.metrics.MeterProvider;
import io.opentelemetry.metrics.spi.MeterProviderFactory;
import io.opentelemetry.trace.DefaultTracerProvider;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Tracer;
import io.opentelemetry.trace.TracerProvider;
import io.opentelemetry.trace.spi.TracerProviderFactory;
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
    OpenTelemetry.reset();
  }

  @AfterEach
  void after() {
    OpenTelemetry.reset();
    System.clearProperty(TracerProviderFactory.class.getName());
    System.clearProperty(MeterProviderFactory.class.getName());
    System.clearProperty(CorrelationContextManagerFactory.class.getName());
  }

  @Test
  void testDefault() {
    assertThat(OpenTelemetry.getTracerProvider()).isInstanceOf(DefaultTracerProvider.class);
    assertThat(OpenTelemetry.getTracerProvider()).isSameAs(OpenTelemetry.getTracerProvider());
    assertThat(OpenTelemetry.getMeterProvider()).isInstanceOf(DefaultMeterProvider.class);
    assertThat(OpenTelemetry.getMeterProvider()).isSameAs(OpenTelemetry.getMeterProvider());
    assertThat(OpenTelemetry.getCorrelationContextManager())
        .isInstanceOf(DefaultCorrelationContextManager.class);
    assertThat(OpenTelemetry.getCorrelationContextManager())
        .isSameAs(OpenTelemetry.getCorrelationContextManager());
    assertThat(OpenTelemetry.getPropagators()).isInstanceOf(DefaultContextPropagators.class);
    assertThat(OpenTelemetry.getPropagators()).isSameAs(OpenTelemetry.getPropagators());
  }

  @Test
  void testTracerLoadArbitrary() throws IOException {
    File serviceFile =
        createService(
            TracerProviderFactory.class,
            FirstTracerProviderFactory.class,
            SecondTracerProviderFactory.class);
    try {
      assertTrue(
          (OpenTelemetry.getTracerProvider().get("") instanceof FirstTracerProviderFactory)
              || (OpenTelemetry.getTracerProvider().get("")
                  instanceof SecondTracerProviderFactory));
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
      assertThat(OpenTelemetry.getTracerProvider().get(""))
          .isInstanceOf(SecondTracerProviderFactory.class);
    } finally {
      serviceFile.delete();
    }
  }

  @Test
  void testTracerNotFound() {
    System.setProperty(TracerProviderFactory.class.getName(), "io.does.not.exists");
    assertThrows(IllegalStateException.class, () -> OpenTelemetry.getTracer("testTracer"));
  }

  @Test
  void testMeterLoadArbitrary() throws IOException {
    File serviceFile =
        createService(
            MeterProviderFactory.class,
            FirstMeterProviderFactory.class,
            SecondMeterProviderFactory.class);
    try {
      assertTrue(
          (OpenTelemetry.getMeterProvider() instanceof FirstMeterProviderFactory)
              || (OpenTelemetry.getMeterProvider() instanceof SecondMeterProviderFactory));
      assertThat(OpenTelemetry.getMeterProvider()).isEqualTo(OpenTelemetry.getMeterProvider());
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
      assertThat(OpenTelemetry.getMeterProvider()).isInstanceOf(SecondMeterProviderFactory.class);
      assertThat(OpenTelemetry.getMeterProvider()).isEqualTo(OpenTelemetry.getMeterProvider());
    } finally {
      serviceFile.delete();
    }
  }

  @Test
  void testMeterNotFound() {
    System.setProperty(MeterProviderFactory.class.getName(), "io.does.not.exists");
    assertThrows(IllegalStateException.class, () -> OpenTelemetry.getMeterProvider());
  }

  @Test
  void testCorrelationContextManagerLoadArbitrary() throws IOException {
    File serviceFile =
        createService(
            CorrelationContextManagerFactory.class,
            FirstCorrelationContextManager.class,
            SecondCorrelationContextManager.class);
    try {
      assertTrue(
          (OpenTelemetry.getCorrelationContextManager() instanceof FirstCorrelationContextManager)
              || (OpenTelemetry.getCorrelationContextManager()
                  instanceof SecondCorrelationContextManager));
      assertThat(OpenTelemetry.getCorrelationContextManager())
          .isEqualTo(OpenTelemetry.getCorrelationContextManager());
    } finally {
      serviceFile.delete();
    }
  }

  @Test
  void testCorrelationContextManagerSystemProperty() throws IOException {
    File serviceFile =
        createService(
            CorrelationContextManagerFactory.class,
            FirstCorrelationContextManager.class,
            SecondCorrelationContextManager.class);
    System.setProperty(
        CorrelationContextManagerFactory.class.getName(),
        SecondCorrelationContextManager.class.getName());
    try {
      assertThat(OpenTelemetry.getCorrelationContextManager())
          .isInstanceOf(SecondCorrelationContextManager.class);
      assertThat(OpenTelemetry.getCorrelationContextManager())
          .isEqualTo(OpenTelemetry.getCorrelationContextManager());
    } finally {
      serviceFile.delete();
    }
  }

  @Test
  void testCorrelationContextManagerNotFound() {
    System.setProperty(CorrelationContextManagerFactory.class.getName(), "io.does.not.exists");
    assertThrows(IllegalStateException.class, () -> OpenTelemetry.getCorrelationContextManager());
  }

  @Test
  void testPropagatorsSet() {
    ContextPropagators propagators = DefaultContextPropagators.builder().build();
    OpenTelemetry.setPropagators(propagators);
    assertThat(OpenTelemetry.getPropagators()).isEqualTo(propagators);
  }

  @Test
  void testPropagatorsSetNull() {
    assertThrows(NullPointerException.class, () -> OpenTelemetry.setPropagators(null));
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
    public Span getCurrentSpan() {
      return null;
    }

    @Nullable
    @Override
    public Scope withSpan(Span span) {
      return null;
    }

    @Nullable
    @Override
    public Span.Builder spanBuilder(String spanName) {
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

  public static class SecondCorrelationContextManager extends FirstCorrelationContextManager {
    @Override
    public CorrelationContextManager create() {
      return new SecondCorrelationContextManager();
    }
  }

  public static class FirstCorrelationContextManager
      implements CorrelationContextManager, CorrelationContextManagerFactory {
    @Override
    public CorrelationContextManager create() {
      return new FirstCorrelationContextManager();
    }

    @Nullable
    @Override
    public CorrelationContext getCurrentContext() {
      return null;
    }

    @Nullable
    @Override
    public CorrelationContext.Builder contextBuilder() {
      return null;
    }

    @Nullable
    @Override
    public Scope withContext(CorrelationContext distContext) {
      return null;
    }
  }
}
