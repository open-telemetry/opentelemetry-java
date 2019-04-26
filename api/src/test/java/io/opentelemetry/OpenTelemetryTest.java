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

import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.BinaryFormat;
import io.opentelemetry.context.propagation.HttpTextFormat;
import io.opentelemetry.resource.Resource;
import io.opentelemetry.spi.TracerProvider;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Span.Builder;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.SpanData;
import io.opentelemetry.trace.Trace;
import io.opentelemetry.trace.Tracer;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import javax.annotation.Nullable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class OpenTelemetryTest {

  @Before
  public void before() {
    OpenTelemetry.initialize();
  }

  @Test
  public void testDefault() {
    assertThat(OpenTelemetry.getTracer()).isEqualTo(Trace.getTracer());
    assertThat(OpenTelemetry.getTracer()).isEqualTo(OpenTelemetry.getTracer());
  }

  @Test
  public void testSystemProperty() throws IOException {
    File serviceFile = createService(TracerProvider.class, FirstTracer.class, SecondTracer.class);
    System.setProperty(TracerProvider.class.getName(), SecondTracer.class.getName());
    try {
      OpenTelemetry.initialize();
      assertThat(OpenTelemetry.getTracer()).isInstanceOf(SecondTracer.class);
      assertThat(OpenTelemetry.getTracer()).isEqualTo(OpenTelemetry.getTracer());
    } finally {
      serviceFile.delete();
      System.clearProperty(TracerProvider.class.getCanonicalName());
    }
  }

  @Test(expected = IllegalStateException.class)
  public void testNotFind() {
    System.setProperty(TracerProvider.class.getName(), "io.does.not.exists");
    try {
      OpenTelemetry.initialize();
    } finally {
      System.clearProperty("io.does.not.exists");
    }
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
}
