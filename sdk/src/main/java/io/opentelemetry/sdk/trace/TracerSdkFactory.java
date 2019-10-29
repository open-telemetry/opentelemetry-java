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

package io.opentelemetry.sdk.trace;

import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.internal.MillisClock;
import io.opentelemetry.sdk.resources.EnvVarResource;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.config.TraceConfig;
import io.opentelemetry.trace.Tracer;
import io.opentelemetry.trace.TracerFactory;
import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@code Tracer} provider implementation for {@link TracerFactory}.
 *
 * <p>This class is not intended to be used in application code and it is used only by {@link
 * io.opentelemetry.OpenTelemetry}.
 */
public class TracerSdkFactory implements TracerFactory {
  private static final Logger logger = Logger.getLogger(TracerFactory.class.getName());
  private final Map<String, TracerSdk> tracersByKey = new ConcurrentHashMap<>();
  private final TracerSharedState sharedState;

  /**
   * Returns a new {@link TracerSdkFactory} with default {@link Clock}, {@link IdsGenerator} and
   * {@link Resource}.
   *
   * @return a new {@link TracerSdkFactory} with default configs.
   */
  public static TracerSdkFactory create() {
    return new TracerSdkFactory(
        MillisClock.getInstance(),
        new RandomIdsGenerator(new SecureRandom()),
        EnvVarResource.getResource());
  }

  private TracerSdkFactory(Clock clock, IdsGenerator idsGenerator, Resource resource) {
    this.sharedState = new TracerSharedState(clock, idsGenerator, resource);
  }

  @Override
  public TracerSdk get(String instrumentationName) {
    return get(instrumentationName, null);
  }

  @Override
  public TracerSdk get(String instrumentationName, String instrumentationVersion) {
    String key = instrumentationName + "/" + instrumentationVersion;
    TracerSdk tracer = tracersByKey.get(key);
    if (tracer == null) {
      // todo: pass in the name & version here to the implementation to be used for purposes.
      tracer = new TracerSdk(sharedState);
      tracersByKey.put(key, tracer);
    }
    return tracer;
  }

  /**
   * Returns the active {@code TraceConfig}.
   *
   * @return the active {@code TraceConfig}.
   */
  public TraceConfig getActiveTraceConfig() {
    return sharedState.getActiveTraceConfig();
  }

  /**
   * Updates the active {@link TraceConfig}.
   *
   * @param traceConfig the new active {@code TraceConfig}.
   */
  public void updateActiveTraceConfig(TraceConfig traceConfig) {
    sharedState.updateActiveTraceConfig(traceConfig);
  }

  /**
   * Adds a new {@code SpanProcessor} to this {@code Tracer}.
   *
   * <p>Any registered processor cause overhead, consider to use an async/batch processor especially
   * for span exporting, and export to multiple backends using the {@link
   * io.opentelemetry.sdk.trace.export.MultiSpanExporter}.
   *
   * @param spanProcessor the new {@code SpanProcessor} to be added.
   */
  public void addSpanProcessor(SpanProcessor spanProcessor) {
    sharedState.addSpanProcessor(spanProcessor);
  }

  /**
   * Attempts to stop all the activity for this {@link Tracer}. Calls {@link
   * SpanProcessor#shutdown()} for all registered {@link SpanProcessor}s.
   *
   * <p>This operation may block until all the Spans are processed. Must be called before turning
   * off the main application to ensure all data are processed and exported.
   *
   * <p>After this is called all the newly created {@code Span}s will be no-op.
   */
  public void shutdown() {
    if (sharedState.isStopped()) {
      logger.log(Level.WARNING, "Calling shutdown() multiple times.");
      return;
    }
    sharedState.stop();
  }
}
