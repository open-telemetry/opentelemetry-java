/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opencensusshim;

import io.opencensus.common.Clock;
import io.opencensus.implcore.common.MillisClock;
import io.opencensus.implcore.trace.config.TraceConfigImpl;
import io.opencensus.implcore.trace.internal.RandomHandler;
import io.opencensus.trace.TraceComponent;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.config.TraceConfig;
import io.opencensus.trace.config.TraceParams;
import io.opencensus.trace.export.ExportComponent;
import io.opencensus.trace.propagation.PropagationComponent;
import io.opencensus.trace.samplers.Samplers;

/**
 * Implementation of the {@link TraceComponent} for OpenTelemetry migration, which uses the
 * OpenTelemetry migration StartEndHandler. This class is loaded by reflection in {@link
 * io.opencensus.trace.Tracing} and overrides the OpenCensus default implementation when present.
 */
public final class OpenTelemetryTraceComponentImpl extends TraceComponent {
  private final PropagationComponent propagationComponent =
      new OpenTelemetryPropagationComponentImpl();
  private final ExportComponent noopExportComponent = ExportComponent.newNoopExportComponent();
  private final Clock clock;
  private final TraceConfig traceConfig = makeTraceConfig();
  private final Tracer tracer;

  /** Public constructor to be used with reflection loading. */
  public OpenTelemetryTraceComponentImpl() {
    clock = MillisClock.getInstance();
    RandomHandler randomHandler = new ThreadLocalRandomHandler();
    tracer = new OpenTelemetryTracerImpl(randomHandler, traceConfig);
  }

  @Override
  public Tracer getTracer() {
    return tracer;
  }

  @Override
  public PropagationComponent getPropagationComponent() {
    return propagationComponent;
  }

  @Override
  public final Clock getClock() {
    return clock;
  }

  @Override
  public ExportComponent getExportComponent() {
    // Drop all OC spans, we will export converted OT spans instead
    return noopExportComponent;
  }

  @Override
  public TraceConfig getTraceConfig() {
    return traceConfig;
  }

  private static TraceConfig makeTraceConfig() {
    // Use the default TraceParams except use an always-on sampler. This defers the
    // sampling decision to OpenTelemetry
    TraceConfig traceConfig = new TraceConfigImpl();
    traceConfig.updateActiveTraceParams(
        TraceParams.DEFAULT.toBuilder().setSampler(Samplers.alwaysSample()).build());
    return traceConfig;
  }
}
