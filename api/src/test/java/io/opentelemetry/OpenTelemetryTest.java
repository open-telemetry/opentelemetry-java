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
import io.opentelemetry.metrics.CounterDouble;
import io.opentelemetry.metrics.CounterLong;
import io.opentelemetry.metrics.GaugeDouble;
import io.opentelemetry.metrics.GaugeLong;
import io.opentelemetry.metrics.Measure;
import io.opentelemetry.metrics.Measurement;
import io.opentelemetry.metrics.Meter;
import io.opentelemetry.metrics.NoopMetrics;
import io.opentelemetry.resource.Resource;
import io.opentelemetry.spi.MeterProvider;
import io.opentelemetry.spi.TaggerProvider;
import io.opentelemetry.spi.TracerProvider;
import io.opentelemetry.tags.NoopTags;
import io.opentelemetry.tags.TagMap;
import io.opentelemetry.tags.Tagger;
import io.opentelemetry.trace.NoopTrace;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Span.Builder;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.SpanData;
import io.opentelemetry.trace.Tracer;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.List;
import javax.annotation.Nullable;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class OpenTelemetryTest {

  @BeforeClass
  public static void beforeClass() {
    OpenTelemetry.reset();
  }

  @After
  public void after() {
    OpenTelemetry.reset();
    System.clearProperty(TracerProvider.class.getName());
    System.clearProperty(MeterProvider.class.getName());
    System.clearProperty(TaggerProvider.class.getName());
  }

  @Test
  public void testDefault() {
    assertThat(OpenTelemetry.getTracer()).isInstanceOf(NoopTrace.newNoopTracer().getClass());
    assertThat(OpenTelemetry.getTracer()).isEqualTo(OpenTelemetry.getTracer());
    assertThat(OpenTelemetry.getMeter()).isInstanceOf(NoopMetrics.newNoopMeter().getClass());
    assertThat(OpenTelemetry.getMeter()).isEqualTo(OpenTelemetry.getMeter());
    assertThat(OpenTelemetry.getTagger()).isInstanceOf(NoopTags.newNoopTagger().getClass());
    assertThat(OpenTelemetry.getTagger()).isEqualTo(OpenTelemetry.getTagger());
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

  @Test(expected = IllegalStateException.class)
  public void testTracerNotFound() {
    System.setProperty(TracerProvider.class.getName(), "io.does.not.exists");
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

  @Test(expected = IllegalStateException.class)
  public void testMeterNotFound() {
    System.setProperty(MeterProvider.class.getName(), "io.does.not.exists");
    OpenTelemetry.getMeter();
  }

  @Test
  public void testTaggerLoadArbitrary() throws IOException {
    File serviceFile = createService(TaggerProvider.class, FirstTagger.class, SecondTagger.class);
    try {
      assertTrue(
          (OpenTelemetry.getTagger() instanceof FirstTagger)
              || (OpenTelemetry.getTagger() instanceof SecondTagger));
      assertThat(OpenTelemetry.getTagger()).isEqualTo(OpenTelemetry.getTagger());
    } finally {
      serviceFile.delete();
    }
  }

  @Test
  public void testTaggerSystemProperty() throws IOException {
    File serviceFile = createService(TaggerProvider.class, FirstTagger.class, SecondTagger.class);
    System.setProperty(TaggerProvider.class.getName(), SecondTagger.class.getName());
    try {
      assertThat(OpenTelemetry.getTagger()).isInstanceOf(SecondTagger.class);
      assertThat(OpenTelemetry.getTagger()).isEqualTo(OpenTelemetry.getTagger());
    } finally {
      serviceFile.delete();
    }
  }

  @Test(expected = IllegalStateException.class)
  public void testTaggerNotFound() {
    System.setProperty(TaggerProvider.class.getName(), "io.does.not.exists");
    OpenTelemetry.getTagger();
  }

  private static File createService(Class<?> service, Class<?>... impls) throws IOException {
    URL location = Tracer.class.getProtectionDomain().getCodeSource().getLocation();
    File file = new File(location.getPath() + "META-INF/services/" + service.getName());
    file.getParentFile().mkdirs();

    Writer output = new FileWriter(file);
    for (Class<?> impl : impls) {
      output.write(impl.getName());
      output.write(System.getProperty("line.separator"));
    }
    output.close();
    return file;
  }

  public static class SecondTracer extends FirstTracer {
    @Override
    public Tracer create() {
      return new SecondTracer();
    }
  }

  public static class FirstTracer implements Tracer, TracerProvider {
    @Override
    public Tracer create() {
      return new FirstTracer();
    }

    @Override
    public Span getCurrentSpan() {
      return null;
    }

    @Override
    public Scope withSpan(Span span) {
      return null;
    }

    @Override
    public Builder spanBuilder(String spanName) {
      return null;
    }

    @Override
    public Builder spanBuilderWithExplicitParent(String spanName, @Nullable Span parent) {
      return null;
    }

    @Override
    public Builder spanBuilderWithRemoteParent(
        String spanName, @Nullable SpanContext remoteParentSpanContext) {
      return null;
    }

    @Override
    public void setResource(Resource resource) {}

    @Override
    public Resource getResource() {
      return null;
    }

    @Override
    public void recordSpanData(SpanData span) {}

    @Override
    public BinaryFormat<SpanContext> getBinaryFormat() {
      return null;
    }

    @Override
    public HttpTextFormat<SpanContext> getHttpTextFormat() {
      return null;
    }
  }

  public static class SecondMeter extends FirstMeter {
    @Override
    public Meter create() {
      return new SecondMeter();
    }
  }

  public static class FirstMeter implements Meter, MeterProvider {
    @Override
    public Meter create() {
      return new FirstMeter();
    }

    @Override
    public GaugeLong.Builder gaugeLongBuilder(String name) {
      return null;
    }

    @Override
    public GaugeDouble.Builder gaugeDoubleBuilder(String name) {
      return null;
    }

    @Override
    public CounterDouble.Builder counterDoubleBuilder(String name) {
      return null;
    }

    @Override
    public CounterLong.Builder counterLongBuilder(String name) {
      return null;
    }

    @Override
    public Measure.Builder measureBuilder(String name) {
      return null;
    }

    @Override
    public void record(List<Measurement> measurements) {}

    @Override
    public void record(List<Measurement> measurements, TagMap tags) {}

    @Override
    public void record(List<Measurement> measurements, TagMap tags, SpanContext spanContext) {}
  }

  public static class SecondTagger extends FirstTagger {
    @Override
    public Tagger create() {
      return new SecondTagger();
    }
  }

  public static class FirstTagger implements Tagger, TaggerProvider {
    @Override
    public Tagger create() {
      return new FirstTagger();
    }

    @Override
    public TagMap getCurrentTagMap() {
      return null;
    }

    @Override
    public TagMap.Builder emptyBuilder() {
      return null;
    }

    @Override
    public TagMap.Builder toBuilder(TagMap tags) {
      return null;
    }

    @Override
    public TagMap.Builder currentBuilder() {
      return null;
    }

    @Override
    public Scope withTagMap(TagMap tags) {
      return null;
    }

    @Override
    public BinaryFormat<TagMap> getBinaryFormat() {
      return null;
    }

    @Override
    public HttpTextFormat<TagMap> getHttpTextFormat() {
      return null;
    }
  }
}
