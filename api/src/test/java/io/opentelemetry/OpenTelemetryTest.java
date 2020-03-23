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

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertTrue;

import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.DefaultContextPropagators;
import io.opentelemetry.correlationcontext.CorrelationContext;
import io.opentelemetry.correlationcontext.CorrelationContextManager;
import io.opentelemetry.correlationcontext.DefaultCorrelationContextManager;
import io.opentelemetry.correlationcontext.spi.CorrelationContextManagerProvider;
import io.opentelemetry.metrics.BatchRecorder;
import io.opentelemetry.metrics.DefaultMeterProvider;
import io.opentelemetry.metrics.DoubleCounter;
import io.opentelemetry.metrics.DoubleMeasure;
import io.opentelemetry.metrics.DoubleObserver;
import io.opentelemetry.metrics.LongCounter;
import io.opentelemetry.metrics.LongMeasure;
import io.opentelemetry.metrics.LongObserver;
import io.opentelemetry.metrics.Meter;
import io.opentelemetry.metrics.MeterProvider;
import io.opentelemetry.metrics.spi.MetricsProvider;
import io.opentelemetry.trace.DefaultTracerProvider;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Tracer;
import io.opentelemetry.trace.TracerProvider;
import io.opentelemetry.trace.spi.TraceProvider;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import javax.annotation.Nullable;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class OpenTelemetryTest {

  @Rule public final ExpectedException thrown = ExpectedException.none();

  @BeforeClass
  public static void beforeClass() {
    OpenTelemetry.reset();
  }

  @After
  public void after() {
    OpenTelemetry.reset();
    System.clearProperty(TraceProvider.class.getName());
    System.clearProperty(MetricsProvider.class.getName());
    System.clearProperty(CorrelationContextManagerProvider.class.getName());
  }

  @Test
  public void testDefault() {
    assertThat(OpenTelemetry.getTracerProvider()).isInstanceOf(DefaultTracerProvider.class);
    assertThat(OpenTelemetry.getTracerProvider())
        .isSameInstanceAs(OpenTelemetry.getTracerProvider());
    assertThat(OpenTelemetry.getMeterProvider()).isInstanceOf(DefaultMeterProvider.class);
    assertThat(OpenTelemetry.getMeterProvider()).isSameInstanceAs(OpenTelemetry.getMeterProvider());
    assertThat(OpenTelemetry.getCorrelationContextManager())
        .isInstanceOf(DefaultCorrelationContextManager.class);
    assertThat(OpenTelemetry.getCorrelationContextManager())
        .isSameInstanceAs(OpenTelemetry.getCorrelationContextManager());
    assertThat(OpenTelemetry.getPropagators()).isInstanceOf(DefaultContextPropagators.class);
    assertThat(OpenTelemetry.getPropagators()).isSameInstanceAs(OpenTelemetry.getPropagators());
  }

  @Test
  public void testTracerLoadArbitrary() throws IOException {
    File serviceFile =
        createService(TraceProvider.class, FirstTraceProvider.class, SecondTraceProvider.class);
    try {
      assertTrue(
          (OpenTelemetry.getTracerProvider() instanceof FirstTraceProvider)
              || (OpenTelemetry.getTracerProvider() instanceof SecondTraceProvider));
    } finally {
      serviceFile.delete();
    }
  }

  @Test
  public void testTracerSystemProperty() throws IOException {
    File serviceFile =
        createService(TraceProvider.class, FirstTraceProvider.class, SecondTraceProvider.class);
    System.setProperty(TraceProvider.class.getName(), SecondTraceProvider.class.getName());
    try {
      assertThat(OpenTelemetry.getTracerProvider()).isInstanceOf(SecondTraceProvider.class);
    } finally {
      serviceFile.delete();
    }
  }

  @Test
  public void testTracerNotFound() {
    System.setProperty(TraceProvider.class.getName(), "io.does.not.exists");
    thrown.expect(IllegalStateException.class);
    OpenTelemetry.getTracerProvider().get("testTracer");
  }

  @Test
  public void testMeterLoadArbitrary() throws IOException {
    File serviceFile =
        createService(
            MetricsProvider.class, FirstMetricsProvider.class, SecondMetricsProvider.class);
    try {
      assertTrue(
          (OpenTelemetry.getMeterProvider() instanceof FirstMetricsProvider)
              || (OpenTelemetry.getMeterProvider() instanceof SecondMetricsProvider));
      assertThat(OpenTelemetry.getMeterProvider()).isEqualTo(OpenTelemetry.getMeterProvider());
    } finally {
      serviceFile.delete();
    }
  }

  @Test
  public void testMeterSystemProperty() throws IOException {
    File serviceFile =
        createService(
            MetricsProvider.class, FirstMetricsProvider.class, SecondMetricsProvider.class);
    System.setProperty(MetricsProvider.class.getName(), SecondMetricsProvider.class.getName());
    try {
      assertThat(OpenTelemetry.getMeterProvider()).isInstanceOf(SecondMetricsProvider.class);
      assertThat(OpenTelemetry.getMeterProvider()).isEqualTo(OpenTelemetry.getMeterProvider());
    } finally {
      serviceFile.delete();
    }
  }

  @Test
  public void testMeterNotFound() {
    System.setProperty(MetricsProvider.class.getName(), "io.does.not.exists");
    thrown.expect(IllegalStateException.class);
    OpenTelemetry.getMeterProvider();
  }

  @Test
  public void testCorrelationContextManagerLoadArbitrary() throws IOException {
    File serviceFile =
        createService(
            CorrelationContextManagerProvider.class,
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
  public void testCorrelationContextManagerSystemProperty() throws IOException {
    File serviceFile =
        createService(
            CorrelationContextManagerProvider.class,
            FirstCorrelationContextManager.class,
            SecondCorrelationContextManager.class);
    System.setProperty(
        CorrelationContextManagerProvider.class.getName(),
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
  public void testCorrelationContextManagerNotFound() {
    System.setProperty(CorrelationContextManagerProvider.class.getName(), "io.does.not.exists");
    thrown.expect(IllegalStateException.class);
    OpenTelemetry.getCorrelationContextManager();
  }

  @Test
  public void testPropagatorsSet() {
    ContextPropagators propagators = DefaultContextPropagators.builder().build();
    OpenTelemetry.setPropagators(propagators);
    assertThat(OpenTelemetry.getPropagators()).isEqualTo(propagators);
  }

  @Test
  public void testPropagatorsSetNull() {
    thrown.expect(NullPointerException.class);
    OpenTelemetry.setPropagators(null);
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

  public static class SecondTraceProvider extends FirstTraceProvider {
    @Override
    public Tracer get(String instrumentationName) {
      return new SecondTraceProvider();
    }

    @Override
    public Tracer get(String instrumentationName, String instrumentationVersion) {
      return get(instrumentationName);
    }

    @Override
    public TracerProvider create() {
      return new SecondTraceProvider();
    }
  }

  public static class FirstTraceProvider implements Tracer, TracerProvider, TraceProvider {
    @Override
    public Tracer get(String instrumentationName) {
      return new FirstTraceProvider();
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
      return new FirstTraceProvider();
    }
  }

  public static class SecondMetricsProvider extends FirstMetricsProvider {
    @Override
    public Meter get(String instrumentationName) {
      return new SecondMetricsProvider();
    }

    @Override
    public Meter get(String instrumentationName, String instrumentationVersion) {
      return get(instrumentationName);
    }

    @Override
    public MeterProvider create() {
      return new SecondMetricsProvider();
    }
  }

  public static class FirstMetricsProvider implements Meter, MetricsProvider, MeterProvider {
    @Override
    public MeterProvider create() {
      return new FirstMetricsProvider();
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
    public DoubleMeasure.Builder doubleMeasureBuilder(String name) {
      return null;
    }

    @Nullable
    @Override
    public LongMeasure.Builder longMeasureBuilder(String name) {
      return null;
    }

    @Nullable
    @Override
    public DoubleObserver.Builder doubleObserverBuilder(String name) {
      return null;
    }

    @Nullable
    @Override
    public LongObserver.Builder longObserverBuilder(String name) {
      return null;
    }

    @Nullable
    @Override
    public BatchRecorder newBatchRecorder(String... keyValuePairs) {
      return null;
    }

    @Override
    public Meter get(String instrumentationName) {
      return new FirstMetricsProvider();
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
      implements CorrelationContextManager, CorrelationContextManagerProvider {
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
