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
import io.opentelemetry.context.propagation.DefaultPropagators;
import io.opentelemetry.context.propagation.Propagators;
import io.opentelemetry.correlationcontext.CorrelationContext;
import io.opentelemetry.correlationcontext.CorrelationContextManager;
import io.opentelemetry.correlationcontext.DefaultCorrelationContextManager;
import io.opentelemetry.correlationcontext.spi.CorrelationContextManagerProvider;
import io.opentelemetry.metrics.BatchRecorder;
import io.opentelemetry.metrics.CounterDouble;
import io.opentelemetry.metrics.CounterLong;
import io.opentelemetry.metrics.DefaultMeterFactory;
import io.opentelemetry.metrics.GaugeDouble;
import io.opentelemetry.metrics.GaugeLong;
import io.opentelemetry.metrics.LabelSet;
import io.opentelemetry.metrics.MeasureDouble;
import io.opentelemetry.metrics.MeasureLong;
import io.opentelemetry.metrics.Meter;
import io.opentelemetry.metrics.MeterFactory;
import io.opentelemetry.metrics.ObserverDouble;
import io.opentelemetry.metrics.ObserverLong;
import io.opentelemetry.metrics.spi.MeterFactoryProvider;
import io.opentelemetry.trace.DefaultTracer;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Tracer;
import io.opentelemetry.trace.TracerFactory;
import io.opentelemetry.trace.spi.TracerFactoryProvider;
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
    System.clearProperty(TracerFactoryProvider.class.getName());
    System.clearProperty(MeterFactoryProvider.class.getName());
    System.clearProperty(CorrelationContextManagerProvider.class.getName());
  }

  @Test
  public void testDefault() {
    assertThat(OpenTelemetry.getTracerFactory().get("testTracer"))
        .isInstanceOf(DefaultTracer.getInstance().getClass());
    assertThat(OpenTelemetry.getTracerFactory().get("testTracer"))
        .isEqualTo(OpenTelemetry.getTracerFactory().get("testTracer"));
    assertThat(OpenTelemetry.getMeterFactory())
        .isInstanceOf(DefaultMeterFactory.getInstance().getClass());
    assertThat(OpenTelemetry.getMeterFactory()).isEqualTo(OpenTelemetry.getMeterFactory());
    assertThat(OpenTelemetry.getCorrelationContextManager())
        .isInstanceOf(DefaultCorrelationContextManager.getInstance().getClass());
    assertThat(OpenTelemetry.getCorrelationContextManager())
        .isEqualTo(OpenTelemetry.getCorrelationContextManager());
    assertThat(OpenTelemetry.getPropagators()).isInstanceOf(DefaultPropagators.class);
    assertThat(OpenTelemetry.getPropagators()).isEqualTo(OpenTelemetry.getPropagators());
  }

  @Test
  public void testTracerLoadArbitrary() throws IOException {
    File serviceFile =
        createService(
            TracerFactoryProvider.class, FirstTracerFactory.class, SecondTracerFactory.class);
    try {
      assertTrue(
          (OpenTelemetry.getTracerFactory() instanceof FirstTracerFactory)
              || (OpenTelemetry.getTracerFactory() instanceof SecondTracerFactory));
    } finally {
      serviceFile.delete();
    }
  }

  @Test
  public void testTracerSystemProperty() throws IOException {
    File serviceFile =
        createService(
            TracerFactoryProvider.class, FirstTracerFactory.class, SecondTracerFactory.class);
    System.setProperty(TracerFactoryProvider.class.getName(), SecondTracerFactory.class.getName());
    try {
      assertThat(OpenTelemetry.getTracerFactory()).isInstanceOf(SecondTracerFactory.class);
    } finally {
      serviceFile.delete();
    }
  }

  @Test
  public void testTracerNotFound() {
    System.setProperty(TracerFactoryProvider.class.getName(), "io.does.not.exists");
    thrown.expect(IllegalStateException.class);
    OpenTelemetry.getTracerFactory().get("testTracer");
  }

  @Test
  public void testMeterLoadArbitrary() throws IOException {
    File serviceFile =
        createService(
            MeterFactoryProvider.class, FirstMeterFactory.class, SecondMeterFactory.class);
    try {
      assertTrue(
          (OpenTelemetry.getMeterFactory() instanceof FirstMeterFactory)
              || (OpenTelemetry.getMeterFactory() instanceof SecondMeterFactory));
      assertThat(OpenTelemetry.getMeterFactory()).isEqualTo(OpenTelemetry.getMeterFactory());
    } finally {
      serviceFile.delete();
    }
  }

  @Test
  public void testMeterSystemProperty() throws IOException {
    File serviceFile =
        createService(
            MeterFactoryProvider.class, FirstMeterFactory.class, SecondMeterFactory.class);
    System.setProperty(MeterFactoryProvider.class.getName(), SecondMeterFactory.class.getName());
    try {
      assertThat(OpenTelemetry.getMeterFactory()).isInstanceOf(SecondMeterFactory.class);
      assertThat(OpenTelemetry.getMeterFactory()).isEqualTo(OpenTelemetry.getMeterFactory());
    } finally {
      serviceFile.delete();
    }
  }

  @Test
  public void testMeterNotFound() {
    System.setProperty(MeterFactoryProvider.class.getName(), "io.does.not.exists");
    thrown.expect(IllegalStateException.class);
    OpenTelemetry.getMeterFactory();
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
    Propagators propagators = DefaultPropagators.builder().build();
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

  public static class SecondTracerFactory extends FirstTracerFactory {
    @Override
    public Tracer get(String instrumentationName) {
      return new SecondTracerFactory();
    }

    @Override
    public Tracer get(String instrumentationName, String instrumentationVersion) {
      return get(instrumentationName);
    }

    @Override
    public TracerFactory create() {
      return new SecondTracerFactory();
    }
  }

  public static class FirstTracerFactory implements Tracer, TracerFactory, TracerFactoryProvider {
    @Override
    public Tracer get(String instrumentationName) {
      return new FirstTracerFactory();
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
    public TracerFactory create() {
      return new FirstTracerFactory();
    }
  }

  public static class SecondMeterFactory extends FirstMeterFactory {
    @Override
    public Meter get(String instrumentationName) {
      return new SecondMeterFactory();
    }

    @Override
    public Meter get(String instrumentationName, String instrumentationVersion) {
      return get(instrumentationName);
    }

    @Override
    public MeterFactory create() {
      return new SecondMeterFactory();
    }
  }

  public static class FirstMeterFactory implements Meter, MeterFactoryProvider, MeterFactory {
    @Override
    public MeterFactory create() {
      return new FirstMeterFactory();
    }

    @Nullable
    @Override
    public GaugeLong.Builder gaugeLongBuilder(String name) {
      return null;
    }

    @Nullable
    @Override
    public GaugeDouble.Builder gaugeDoubleBuilder(String name) {
      return null;
    }

    @Nullable
    @Override
    public CounterDouble.Builder counterDoubleBuilder(String name) {
      return null;
    }

    @Nullable
    @Override
    public CounterLong.Builder counterLongBuilder(String name) {
      return null;
    }

    @Nullable
    @Override
    public MeasureDouble.Builder measureDoubleBuilder(String name) {
      return null;
    }

    @Nullable
    @Override
    public MeasureLong.Builder measureLongBuilder(String name) {
      return null;
    }

    @Nullable
    @Override
    public ObserverDouble.Builder observerDoubleBuilder(String name) {
      return null;
    }

    @Nullable
    @Override
    public ObserverLong.Builder observerLongBuilder(String name) {
      return null;
    }

    @Nullable
    @Override
    public BatchRecorder newMeasureBatchRecorder() {
      return null;
    }

    @Nullable
    @Override
    public LabelSet createLabelSet(String k1, String v1) {
      return null;
    }

    @Nullable
    @Override
    public LabelSet createLabelSet(String k1, String v1, String k2, String v2) {
      return null;
    }

    @Nullable
    @Override
    public LabelSet createLabelSet(
        String k1, String v1, String k2, String v2, String k3, String v3) {
      return null;
    }

    @Nullable
    @Override
    public LabelSet createLabelSet(
        String k1, String v1, String k2, String v2, String k3, String v3, String k4, String v4) {
      return null;
    }

    @Override
    public Meter get(String instrumentationName) {
      return new FirstMeterFactory();
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
