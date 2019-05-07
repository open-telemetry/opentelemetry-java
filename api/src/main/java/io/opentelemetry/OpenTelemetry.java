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

import io.opentelemetry.spi.TracerProvider;
import io.opentelemetry.trace.NoopTrace;
import io.opentelemetry.trace.Trace;
import io.opentelemetry.trace.Tracer;
import java.util.ServiceLoader;

/**
 * This class provides a static global accessor for telemetry objects {@link Tracer}, {@link
 * io.opentelemetry.metrics.Meter} and {@link io.opentelemetry.tags.Tagger}.
 */
public final class OpenTelemetry {

  private static volatile OpenTelemetry instance;

  private Tracer tracer;

  /**
   * Returns an instance of a {@link Tracer}.
   *
   * @return registered tracer or {@link NoopTrace} singleton via {@link Trace#getTracer()}.
   * @throws IllegalStateException if a specified tracer (via system properties) could not be find.
   */
  public static Tracer getTracer() {
    return getInstance().tracer;
  }

  private static Tracer loadTracer() {
    String tracerClass = System.getProperty(TracerProvider.class.getName());
    ServiceLoader<TracerProvider> tracers = ServiceLoader.load(TracerProvider.class);
    for (TracerProvider provider : tracers) {
      if (tracerClass == null) {
        return provider.create();
      } else if (tracerClass.equals(provider.getClass().getName())) {
        return provider.create();
      }
    }
    if (tracerClass != null) {
      throw new IllegalStateException(String.format("Tracer %s not found", tracerClass));
    }
    return Trace.getTracer();
  }

  /** Lazy loads an instance. */
  private static OpenTelemetry getInstance() {
    if (instance == null) {
      synchronized (OpenTelemetry.class) {
        if (instance == null) {
          instance = new OpenTelemetry();
        }
      }
    }
    return instance;
  }

  private OpenTelemetry() {
    tracer = loadTracer();
  }

  // for testing
  static void initialize() {
    getInstance().tracer = loadTracer();
  }
}
