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
import static org.mockito.Mockito.mock;

import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.BinaryFormat;
import io.opentelemetry.context.propagation.HttpTextFormat;
import io.opentelemetry.correlationcontext.CorrelationContext;
import io.opentelemetry.correlationcontext.CorrelationContextManager;
import io.opentelemetry.correlationcontext.DefaultCorrelationContextManager;
import io.opentelemetry.correlationcontext.spi.CorrelationContextManagerProvider;
import io.opentelemetry.metrics.BatchRecorder;
import io.opentelemetry.metrics.DefaultMeterRegistry;
import io.opentelemetry.metrics.DoubleCounter;
import io.opentelemetry.metrics.DoubleGauge;
import io.opentelemetry.metrics.DoubleMeasure;
import io.opentelemetry.metrics.DoubleObserver;
import io.opentelemetry.metrics.LabelSet;
import io.opentelemetry.metrics.LongCounter;
import io.opentelemetry.metrics.LongGauge;
import io.opentelemetry.metrics.LongMeasure;
import io.opentelemetry.metrics.LongObserver;
import io.opentelemetry.metrics.Meter;
import io.opentelemetry.metrics.MeterRegistry;
import io.opentelemetry.metrics.spi.MeterRegistryProvider;
import io.opentelemetry.trace.DefaultTracer;
import io.opentelemetry.trace.DefaultTracerFactory;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.Tracer;
import io.opentelemetry.trace.TracerRegistry;
import io.opentelemetry.trace.spi.TracerRegistryProvider;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.Map;
import javax.annotation.Nonnull;
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
    System.clearProperty(TracerRegistryProvider.class.getName());
    System.clearProperty(MeterRegistryProvider.class.getName());
    System.clearProperty(CorrelationContextManagerProvider.class.getName());
  }

  @Test
  public void testDefault() {
    assertThat(OpenTelemetry.getTracerRegistry().get("testTracer"))
        .isInstanceOf(DefaultTracer.getInstance().getClass());
    assertThat(OpenTelemetry.getTracerRegistry().get("testTracer"))
        .isEqualTo(OpenTelemetry.getTracerRegistry().get("testTracer"));
    assertThat(OpenTelemetry.getMeterRegistry())
        .isInstanceOf(DefaultMeterRegistry.getInstance().getClass());
    assertThat(OpenTelemetry.getMeterRegistry()).isEqualTo(OpenTelemetry.getMeterRegistry());
    assertThat(OpenTelemetry.getCorrelationContextManager())
        .isInstanceOf(DefaultCorrelationContextManager.getInstance().getClass());
    assertThat(OpenTelemetry.getCorrelationContextManager())
        .isEqualTo(OpenTelemetry.getCorrelationContextManager());
  }

  @Test
  public void testSetInstanceBeforeSingletonAccess() {
    TracerFactory tracerFactory = mock(TracerFactory.class);
    MeterFactory meterFactory = mock(MeterFactory.class);
    DistributedContextManager contextManager = mock(DistributedContextManager.class);
    OpenTelemetry.setTracerFactory(tracerFactory);
    OpenTelemetry.setMeterFactory(meterFactory);
    OpenTelemetry.setDistributedContextManager(contextManager);
    assertThat(OpenTelemetry.getTracerFactory()).isSameInstanceAs(tracerFactory);
    assertThat(OpenTelemetry.getMeterFactory()).isSameInstanceAs(meterFactory);
    assertThat(OpenTelemetry.getDistributedContextManager()).isSameInstanceAs(contextManager);
  }

  @Test
  public void testSetInstanceOneTimeOnly() {
    TracerFactory tracerFactory = OpenTelemetry.getTracerFactory();
    assertThat(tracerFactory).isInstanceOf(DefaultTracerFactory.class);

    // this call should be ignored, since we've already accessed the singletons.
    OpenTelemetry.setTracerFactory(mock(TracerFactory.class));

    assertThat(OpenTelemetry.getTracerFactory()).isSameInstanceAs(tracerFactory);
  }

  @Test
  public void testTracerLoadArbitrary() throws IOException {
    File serviceFile =
        createService(
            TracerRegistryProvider.class, FirstTracerRegistry.class, SecondTracerRegistry.class);
    try {
      assertTrue(
          (OpenTelemetry.getTracerRegistry() instanceof FirstTracerRegistry)
              || (OpenTelemetry.getTracerRegistry() instanceof SecondTracerRegistry));
    } finally {
      serviceFile.delete();
    }
  }

  @Test
  public void testTracerSystemProperty() throws IOException {
    File serviceFile =
        createService(
            TracerRegistryProvider.class, FirstTracerRegistry.class, SecondTracerRegistry.class);
    System.setProperty(
        TracerRegistryProvider.class.getName(), SecondTracerRegistry.class.getName());
    try {
      assertThat(OpenTelemetry.getTracerRegistry()).isInstanceOf(SecondTracerRegistry.class);
    } finally {
      serviceFile.delete();
    }
  }

  @Test
  public void testTracerNotFound() {
    System.setProperty(TracerRegistryProvider.class.getName(), "io.does.not.exists");
    thrown.expect(IllegalStateException.class);
    OpenTelemetry.getTracerRegistry().get("testTracer");
  }

  @Test
  public void testMeterLoadArbitrary() throws IOException {
    File serviceFile =
        createService(
            MeterRegistryProvider.class, FirstMeterRegistry.class, SecondMeterRegistry.class);
    try {
      assertTrue(
          (OpenTelemetry.getMeterRegistry() instanceof FirstMeterRegistry)
              || (OpenTelemetry.getMeterRegistry() instanceof SecondMeterRegistry));
      assertThat(OpenTelemetry.getMeterRegistry()).isEqualTo(OpenTelemetry.getMeterRegistry());
    } finally {
      serviceFile.delete();
    }
  }

  @Test
  public void testMeterSystemProperty() throws IOException {
    File serviceFile =
        createService(
            MeterRegistryProvider.class, FirstMeterRegistry.class, SecondMeterRegistry.class);
    System.setProperty(MeterRegistryProvider.class.getName(), SecondMeterRegistry.class.getName());
    try {
      assertThat(OpenTelemetry.getMeterRegistry()).isInstanceOf(SecondMeterRegistry.class);
      assertThat(OpenTelemetry.getMeterRegistry()).isEqualTo(OpenTelemetry.getMeterRegistry());
    } finally {
      serviceFile.delete();
    }
  }

  @Test
  public void testMeterNotFound() {
    System.setProperty(MeterRegistryProvider.class.getName(), "io.does.not.exists");
    thrown.expect(IllegalStateException.class);
    OpenTelemetry.getMeterRegistry();
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

  public static class SecondTracerRegistry extends FirstTracerRegistry {
    @Override
    public Tracer get(String instrumentationName) {
      return new SecondTracerRegistry();
    }

    @Override
    public Tracer get(String instrumentationName, String instrumentationVersion) {
      return get(instrumentationName);
    }

    @Override
    public TracerRegistry create() {
      return new SecondTracerRegistry();
    }
  }

  public static class FirstTracerRegistry
      implements Tracer, TracerRegistry, TracerRegistryProvider {
    @Override
    public Tracer get(String instrumentationName) {
      return new FirstTracerRegistry();
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

    @Nullable
    @Override
    public BinaryFormat<SpanContext> getBinaryFormat() {
      return null;
    }

    @Nullable
    @Override
    public HttpTextFormat<SpanContext> getHttpTextFormat() {
      return null;
    }

    @Override
    public TracerRegistry create() {
      return new FirstTracerRegistry();
    }
  }

  public static class SecondMeterRegistry extends FirstMeterRegistry {
    @Override
    public Meter get(String instrumentationName) {
      return new SecondMeterRegistry();
    }

    @Override
    public Meter get(String instrumentationName, String instrumentationVersion) {
      return get(instrumentationName);
    }

    @Override
    public MeterRegistry create() {
      return new SecondMeterRegistry();
    }
  }

  public static class FirstMeterRegistry implements Meter, MeterRegistryProvider, MeterRegistry {
    @Override
    public MeterRegistry create() {
      return new FirstMeterRegistry();
    }

    @Nullable
    @Override
    public LongGauge.Builder longGaugeBuilder(String name) {
      return null;
    }

    @Nullable
    @Override
    public DoubleGauge.Builder doubleGaugeBuilder(String name) {
      return null;
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
    public BatchRecorder newMeasureBatchRecorder() {
      return null;
    }

    @Nullable
    @Override
    public LabelSet createLabelSet(String... keyValuePairs) {
      return null;
    }

    @Nullable
    @Override
    public LabelSet createLabelSet(@Nonnull Map<String, String> labels) {
      return null;
    }

    @Override
    public Meter get(String instrumentationName) {
      return new FirstMeterRegistry();
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

    @Nullable
    @Override
    public BinaryFormat<CorrelationContext> getBinaryFormat() {
      return null;
    }

    @Nullable
    @Override
    public HttpTextFormat<CorrelationContext> getHttpTextFormat() {
      return null;
    }
  }
}
