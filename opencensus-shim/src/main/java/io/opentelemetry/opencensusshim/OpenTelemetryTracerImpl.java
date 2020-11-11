package io.opentelemetry.opencensusshim;

import io.opencensus.common.Clock;
import io.opencensus.implcore.trace.RecordEventsSpanImpl;
import io.opencensus.implcore.trace.internal.RandomHandler;
import io.opencensus.trace.Span;
import io.opencensus.trace.SpanBuilder;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.config.TraceConfig;
import io.opentelemetry.opencensusshim.OpenTelemetrySpanBuilderImpl.Options;
import javax.annotation.Nullable;

public class OpenTelemetryTracerImpl extends Tracer {
  private final OpenTelemetrySpanBuilderImpl.Options spanBuilderOptions;

  public OpenTelemetryTracerImpl(
      RandomHandler randomHandler,
      RecordEventsSpanImpl.StartEndHandler startEndHandler,
      Clock clock,
      TraceConfig traceConfig) {
    spanBuilderOptions = new Options(randomHandler, startEndHandler, clock, traceConfig);
  }

  @Override
  public SpanBuilder spanBuilderWithExplicitParent(String spanName, @Nullable Span parent) {
    return OpenTelemetrySpanBuilderImpl.createWithParent(spanName, parent, spanBuilderOptions);
  }

  @Override
  public SpanBuilder spanBuilderWithRemoteParent(
      String spanName, @Nullable SpanContext remoteParentSpanContext) {
    return OpenTelemetrySpanBuilderImpl.createWithRemoteParent(
        spanName, remoteParentSpanContext, spanBuilderOptions);
  }
}
