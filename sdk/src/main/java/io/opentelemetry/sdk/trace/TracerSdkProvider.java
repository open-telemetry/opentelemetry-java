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
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.internal.ComponentRegistry;
import io.opentelemetry.sdk.internal.MillisClock;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.config.TraceConfig;
import io.opentelemetry.trace.Tracer;
import io.opentelemetry.trace.TracerProvider;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@code Tracer} provider implementation for {@link TracerProvider}.
 *
 * <p>This class is not intended to be used in application code and it is used only by {@link
 * io.opentelemetry.OpenTelemetry}. However, if you need a custom implementation of the factory, you
 * can create one as needed.
 */
public class TracerSdkProvider implements TracerProvider {
  private static final Logger logger = Logger.getLogger(TracerProvider.class.getName());
  private final TracerSharedState sharedState;
  private final TracerSdkComponentRegistry tracerSdkComponentRegistry;

  /**
   * Returns a new {@link Builder} for {@link TracerSdkProvider}.
   *
   * @return a new {@link Builder} for {@link TracerSdkProvider}.
   */
  public static Builder builder() {
    return new Builder();
  }

  private TracerSdkProvider(Clock clock, IdsGenerator idsGenerator, Resource resource) {
    this.sharedState = new TracerSharedState(clock, idsGenerator, resource);
    this.tracerSdkComponentRegistry = new TracerSdkComponentRegistry(sharedState);
  }

  @Override
  public TracerSdk get(String instrumentationName) {
    return tracerSdkComponentRegistry.get(instrumentationName);
  }

  @Override
  public TracerSdk get(String instrumentationName, String instrumentationVersion) {
    return tracerSdkComponentRegistry.get(instrumentationName, instrumentationVersion);
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
   * <p>Note: To update the {@link TraceConfig} associated with this instance you should use the
   * {@link TraceConfig#toBuilder()} method on the {@link TraceConfig} returned from {@link
   * #getActiveTraceConfig()}, make the changes desired to the {@link TraceConfig.Builder} instance,
   * then use this method with the resulting {@link TraceConfig} instance.
   *
   * @param traceConfig the new active {@code TraceConfig}.
   * @see TraceConfig
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

  /**
   * Requests the active span processor to process all span events that have not yet been processed.
   *
   * @see SpanProcessor#forceFlush()
   */
  public void forceFlush() {
    sharedState.getActiveSpanProcessor().forceFlush();
  }

  /**
   * Builder class for the TracerSdkFactory. Has fully functional default implementations of all
   * three required interfaces.
   *
   * @since 0.4.0
   */
  public static class Builder {

    private Clock clock = MillisClock.getInstance();
    private IdsGenerator idsGenerator = new RandomIdsGenerator();
    private Resource resource = Resource.getTelemetrySdk().merge(Resource.getDefault());

    /**
     * Assign a {@link Clock}.
     *
     * @param clock The clock to use for all temporal needs.
     * @return this
     */
    public Builder setClock(Clock clock) {
      Objects.requireNonNull(clock, "clock");
      this.clock = clock;
      return this;
    }

    /**
     * Assign an {@link IdsGenerator}.
     *
     * @param idsGenerator A generator for trace and span ids. Note: this should be thread-safe and
     *     as contention free as possible.
     * @return this
     */
    public Builder setIdsGenerator(IdsGenerator idsGenerator) {
      Objects.requireNonNull(idsGenerator, "idsGenerator");
      this.idsGenerator = idsGenerator;
      return this;
    }

    /**
     * Assign a {@link Resource} to be attached to all Spans created by Tracers.
     *
     * @param resource A Resource implementation.
     * @return this
     */
    public Builder setResource(Resource resource) {
      Objects.requireNonNull(resource, "resource");
      this.resource = resource;
      return this;
    }

    /**
     * Create a new TracerSdkFactory instance.
     *
     * @return An initialized TracerSdkFactory.
     */
    public TracerSdkProvider build() {
      return new TracerSdkProvider(clock, idsGenerator, resource);
    }

    private Builder() {}
  }

  private static final class TracerSdkComponentRegistry extends ComponentRegistry<TracerSdk> {
    private final TracerSharedState sharedState;

    private TracerSdkComponentRegistry(TracerSharedState sharedState) {
      this.sharedState = sharedState;
    }

    @Override
    public TracerSdk newComponent(InstrumentationLibraryInfo instrumentationLibraryInfo) {
      return new TracerSdk(sharedState, instrumentationLibraryInfo);
    }
  }
}
