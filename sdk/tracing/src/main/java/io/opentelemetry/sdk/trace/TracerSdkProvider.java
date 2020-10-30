/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.TracerProvider;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.internal.ComponentRegistry;
import io.opentelemetry.sdk.internal.MillisClock;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.config.TraceConfig;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@code Tracer} provider implementation for {@link TracerProvider}.
 *
 * <p>This class is not intended to be used in application code and it is used only by {@link
 * OpenTelemetry}. However, if you need a custom implementation of the factory, you can create one
 * as needed.
 */
public class TracerSdkProvider implements TracerProvider, TracerSdkManagement {
  private static final Logger logger = Logger.getLogger(TracerSdkProvider.class.getName());
  private static final String defaultTracerName = "unknown";
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

  private TracerSdkProvider(Clock clock, IdGenerator idsGenerator, Resource resource) {
    this.sharedState = new TracerSharedState(clock, idsGenerator, resource);
    this.tracerSdkComponentRegistry = new TracerSdkComponentRegistry(sharedState);
  }

  @Override
  public Tracer get(String instrumentationName) {
    return get(instrumentationName, null);
  }

  @Override
  public Tracer get(String instrumentationName, @Nullable String instrumentationVersion) {
    // Per the spec, both null and empty are "invalid" and a "default" should be used. See commit
    //   message for further details.
    if (instrumentationName == null || instrumentationName.isEmpty()) {
      logger.warning("Tracer requested without instrumentation name.");
      instrumentationName = defaultTracerName;
    }
    return tracerSdkComponentRegistry.get(instrumentationName, instrumentationVersion);
  }

  @Override
  public TraceConfig getActiveTraceConfig() {
    return sharedState.getActiveTraceConfig();
  }

  @Override
  public void updateActiveTraceConfig(TraceConfig traceConfig) {
    sharedState.updateActiveTraceConfig(traceConfig);
  }

  @Override
  public void addSpanProcessor(SpanProcessor spanProcessor) {
    sharedState.addSpanProcessor(spanProcessor);
  }

  @Override
  public void shutdown() {
    if (sharedState.isStopped()) {
      logger.log(Level.WARNING, "Calling shutdown() multiple times.");
      return;
    }
    sharedState.stop();
  }

  @Override
  public CompletableResultCode forceFlush() {
    return sharedState.getActiveSpanProcessor().forceFlush();
  }

  /**
   * Builder class for the TracerSdkFactory. Has fully functional default implementations of all
   * three required interfaces.
   */
  public static class Builder {

    private Clock clock = MillisClock.getInstance();
    private IdGenerator idsGenerator = IdGenerator.random();
    private Resource resource = Resource.getDefault();

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
     * Assign an {@link IdGenerator}.
     *
     * @param idsGenerator A generator for trace and span ids. Note: this should be thread-safe and
     *     as contention free as possible.
     * @return this
     */
    public Builder setIdsGenerator(IdGenerator idsGenerator) {
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
