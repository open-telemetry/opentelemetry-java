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
import io.opentelemetry.context.propagation.BinaryFormat;
import io.opentelemetry.context.propagation.HttpTextFormat;
import io.opentelemetry.distributedcontext.DefaultDistributedContextManager;
import io.opentelemetry.distributedcontext.DistributedContext;
import io.opentelemetry.distributedcontext.DistributedContextManager;
import io.opentelemetry.distributedcontext.spi.DistributedContextManagerProvider;
import io.opentelemetry.metrics.CounterDouble;
import io.opentelemetry.metrics.CounterLong;
import io.opentelemetry.metrics.DefaultMeter;
import io.opentelemetry.metrics.GaugeDouble;
import io.opentelemetry.metrics.GaugeLong;
import io.opentelemetry.metrics.MeasureBatchRecorder;
import io.opentelemetry.metrics.MeasureDouble;
import io.opentelemetry.metrics.MeasureLong;
import io.opentelemetry.metrics.Meter;
import io.opentelemetry.metrics.spi.MeterProvider;
import io.opentelemetry.trace.DefaultTracer;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.Tracer;
import io.opentelemetry.trace.spi.TracerProvider;
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
    System.clearProperty(TracerProvider.class.getName());
    System.clearProperty(MeterProvider.class.getName());
    System.clearProperty(DistributedContextManagerProvider.class.getName());
  }

  @Test
  public void testDefault() {
    assertThat(OpenTelemetry.getTracer()).isInstanceOf(DefaultTracer.getInstance().getClass());
    assertThat(OpenTelemetry.getTracer()).isEqualTo(OpenTelemetry.getTracer());
    assertThat(OpenTelemetry.getMeter()).isInstanceOf(DefaultMeter.getInstance().getClass());
    assertThat(OpenTelemetry.getMeter()).isEqualTo(OpenTelemetry.getMeter());
    assertThat(OpenTelemetry.getDistributedContextManager())
        .isInstanceOf(DefaultDistributedContextManager.getInstance().getClass());
    assertThat(OpenTelemetry.getDistributedContextManager())
        .isEqualTo(OpenTelemetry.getDistributedContextManager());
  }

  @Test
  public void testTracerLoadArbitrary() throws IOException {
    File serviceFile = createService(TracerProvider.class, FirstTracer.class, SecondTracer.class);
    try {
      assertTrue(
          (OpenTelemetry.getTracer() instanceof FirstTracer)
              || (OpenTelemetry.getTracer() instanceof SecondTracer));
      assertThat(OpenTelemetry.getTracer()).isEqualTo(OpenTelemetry.getTracer());
    } finally {
      serviceFile.delete();
    }
  }

  @Test
  public void testTracerSystemProperty() throws IOException {
    File serviceFile = createService(TracerProvider.class, FirstTracer.class, SecondTracer.class);
    System.setProperty(TracerProvider.class.getName(), SecondTracer.class.getName());
    try {
      assertThat(OpenTelemetry.getTracer()).isInstanceOf(SecondTracer.class);
      assertThat(OpenTelemetry.getTracer()).isEqualTo(OpenTelemetry.getTracer());
    } finally {
      serviceFile.delete();
    }
  }

  @Test
  public void testTracerNotFound() {
    System.setProperty(TracerProvider.class.getName(), "io.does.not.exists");
    thrown.expect(IllegalStateException.class);
    OpenTelemetry.getTracer();
  }

  @Test
  public void testMeterLoadArbitrary() throws IOException {
    File serviceFile = createService(MeterProvider.class, FirstMeter.class, SecondMeter.class);
    try {
      assertTrue(
          (OpenTelemetry.getMeter() instanceof FirstMeter)
              || (OpenTelemetry.getMeter() instanceof SecondMeter));
      assertThat(OpenTelemetry.getMeter()).isEqualTo(OpenTelemetry.getMeter());
    } finally {
      serviceFile.delete();
    }
  }

  @Test
  public void testMeterSystemProperty() throws IOException {
    File serviceFile = createService(MeterProvider.class, FirstMeter.class, SecondMeter.class);
    System.setProperty(MeterProvider.class.getName(), SecondMeter.class.getName());
    try {
      assertThat(OpenTelemetry.getMeter()).isInstanceOf(SecondMeter.class);
      assertThat(OpenTelemetry.getMeter()).isEqualTo(OpenTelemetry.getMeter());
    } finally {
      serviceFile.delete();
    }
  }

  @Test
  public void testMeterNotFound() {
    System.setProperty(MeterProvider.class.getName(), "io.does.not.exists");
    thrown.expect(IllegalStateException.class);
    OpenTelemetry.getMeter();
  }

  @Test
  public void testDistributedContextManagerLoadArbitrary() throws IOException {
    File serviceFile =
        createService(
            DistributedContextManagerProvider.class,
            FirstDistributedContextManager.class,
            SecondDistributedContextManager.class);
    try {
      System.out.println(OpenTelemetry.getDistributedContextManager());
      assertTrue(
          (OpenTelemetry.getDistributedContextManager() instanceof FirstDistributedContextManager)
              || (OpenTelemetry.getDistributedContextManager()
                  instanceof SecondDistributedContextManager));
      assertThat(OpenTelemetry.getDistributedContextManager())
          .isEqualTo(OpenTelemetry.getDistributedContextManager());
    } finally {
      serviceFile.delete();
    }
  }

  @Test
  public void testDistributedContextManagerSystemProperty() throws IOException {
    File serviceFile =
        createService(
            DistributedContextManagerProvider.class,
            FirstDistributedContextManager.class,
            SecondDistributedContextManager.class);
    System.setProperty(
        DistributedContextManagerProvider.class.getName(),
        SecondDistributedContextManager.class.getName());
    try {
      assertThat(OpenTelemetry.getDistributedContextManager())
          .isInstanceOf(SecondDistributedContextManager.class);
      assertThat(OpenTelemetry.getDistributedContextManager())
          .isEqualTo(OpenTelemetry.getDistributedContextManager());
    } finally {
      serviceFile.delete();
    }
  }

  @Test
  public void testDistributedContextManagerNotFound() {
    System.setProperty(DistributedContextManagerProvider.class.getName(), "io.does.not.exists");
    thrown.expect(IllegalStateException.class);
    OpenTelemetry.getDistributedContextManager();
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

  public static class SecondTracer extends FirstTracer {
    private static final Tracer INSTANCE = new SecondTracer();

    @Override
    public Tracer get() {
      return INSTANCE;
    }
  }

  public static class FirstTracer implements Tracer, TracerProvider {
    private static final Tracer INSTANCE = new FirstTracer();

    @Override
    public Tracer get() {
      return INSTANCE;
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
  }

  public static class SecondMeter extends FirstMeter {
    private static final Meter INSTANCE = new SecondMeter();

    @Override
    public Meter get() {
      return INSTANCE;
    }
  }

  public static class FirstMeter implements Meter, MeterProvider {
    private static final Meter INSTANCE = new FirstMeter();

    @Override
    public Meter get() {
      return INSTANCE;
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
    public MeasureBatchRecorder newMeasureBatchRecorder() {
      return null;
    }
  }

  public static class SecondDistributedContextManager extends FirstDistributedContextManager {
    private static final DistributedContextManager INSTANCE = new SecondDistributedContextManager();

    @Override
    public DistributedContextManager get() {
      return INSTANCE;
    }
  }

  public static class FirstDistributedContextManager
      implements DistributedContextManager, DistributedContextManagerProvider {
    private static final DistributedContextManager INSTANCE = new FirstDistributedContextManager();

    @Override
    public DistributedContextManager get() {
      return INSTANCE;
    }

    @Nullable
    @Override
    public DistributedContext getCurrentContext() {
      return null;
    }

    @Nullable
    @Override
    public DistributedContext.Builder contextBuilder() {
      return null;
    }

    @Nullable
    @Override
    public Scope withContext(DistributedContext distContext) {
      return null;
    }

    @Nullable
    @Override
    public BinaryFormat<DistributedContext> getBinaryFormat() {
      return null;
    }

    @Nullable
    @Override
    public HttpTextFormat<DistributedContext> getHttpTextFormat() {
      return null;
    }
  }
}
