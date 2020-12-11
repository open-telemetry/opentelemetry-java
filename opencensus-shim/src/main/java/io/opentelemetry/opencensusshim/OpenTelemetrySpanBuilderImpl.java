/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

// Includes work from:
/*
 * Copyright 2018, OpenCensus Authors
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

package io.opentelemetry.opencensusshim;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.opentelemetry.opencensusshim.SpanConverter.mapKind;

import io.opencensus.implcore.trace.internal.RandomHandler;
import io.opencensus.trace.Sampler;
import io.opencensus.trace.Span;
import io.opencensus.trace.Span.Kind;
import io.opencensus.trace.SpanBuilder;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.SpanId;
import io.opencensus.trace.TraceId;
import io.opencensus.trace.TraceOptions;
import io.opencensus.trace.Tracestate;
import io.opencensus.trace.config.TraceConfig;
import io.opencensus.trace.config.TraceParams;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;

class OpenTelemetrySpanBuilderImpl extends SpanBuilder {
  private static final Tracer OTEL_TRACER =
      OpenTelemetry.getGlobalTracer("io.opentelemetry.opencensusshim");
  private static final Tracestate OC_TRACESTATE_DEFAULT = Tracestate.builder().build();
  private static final TraceOptions OC_SAMPLED_TRACE_OPTIONS =
      TraceOptions.builder().setIsSampled(true).build();
  private static final TraceOptions OC_NOT_SAMPLED_TRACE_OPTIONS =
      TraceOptions.builder().setIsSampled(false).build();

  private final String name;
  private final Options options;

  private List<Span> ocParentLinks = Collections.emptyList();
  private final List<io.opentelemetry.api.trace.SpanContext> otelParentLinks = new ArrayList<>();
  @Nullable private final Span ocParent;
  @Nullable private final SpanContext ocRemoteParentSpanContext;
  @Nullable private Sampler ocSampler;
  @Nullable private Boolean recordEvents;
  @Nullable private io.opentelemetry.api.trace.Span.Kind otelKind;

  @Override
  public SpanBuilder setSampler(Sampler sampler) {
    this.ocSampler = checkNotNull(sampler, "sampler");
    return this;
  }

  @Override
  public SpanBuilder setParentLinks(List<Span> parentLinks) {
    this.ocParentLinks = checkNotNull(parentLinks, "parentLinks");
    for (Span parent : parentLinks) {
      this.otelParentLinks.add(SpanConverter.mapSpanContext(parent.getContext()));
    }
    return this;
  }

  @Override
  public SpanBuilder setRecordEvents(boolean recordEvents) {
    this.recordEvents = recordEvents;
    return this;
  }

  @Override
  public SpanBuilder setSpanKind(@Nullable Kind kind) {
    this.otelKind = mapKind(kind);
    return this;
  }

  @Override
  public Span startSpan() {
    // To determine whether to sample this span
    TraceParams ocActiveTraceParams = options.traceConfig.getActiveTraceParams();
    Random random = options.randomHandler.current();
    TraceId ocTraceId;
    SpanId ocSpanId = SpanId.generateRandomId(random);
    Tracestate ocTracestate = OC_TRACESTATE_DEFAULT;
    SpanContext ocParentContext = null;
    Boolean hasRemoteParent = null;
    if (ocRemoteParentSpanContext != null && ocRemoteParentSpanContext.isValid()) {
      ocParentContext = ocRemoteParentSpanContext;
      hasRemoteParent = Boolean.TRUE;
      ocTraceId = ocParentContext.getTraceId();
      ocTracestate = ocParentContext.getTracestate();
    } else if (ocParent != null && ocParent.getContext().isValid()) {
      ocParentContext = ocParent.getContext();
      hasRemoteParent = Boolean.FALSE;
      ocTraceId = ocParentContext.getTraceId();
      ocTracestate = ocParentContext.getTracestate();
    } else {
      // New root span.
      ocTraceId = TraceId.generateRandomId(random);
    }
    TraceOptions ocTraceOptions =
        makeSamplingDecision(
                ocParentContext,
                hasRemoteParent,
                name,
                ocSampler,
                ocParentLinks,
                ocTraceId,
                ocSpanId,
                ocActiveTraceParams)
            ? OC_SAMPLED_TRACE_OPTIONS
            : OC_NOT_SAMPLED_TRACE_OPTIONS;
    if (!ocTraceOptions.isSampled() && !Boolean.TRUE.equals(recordEvents)) {
      return OpenTelemetryNoRecordEventsSpanImpl.create(
          SpanContext.create(ocTraceId, ocSpanId, ocTraceOptions, ocTracestate));
    }

    // If sampled
    io.opentelemetry.api.trace.SpanBuilder otelSpanBuilder =
        OTEL_TRACER.spanBuilder(name);
    if (ocParent != null && ocParent instanceof OpenTelemetrySpanImpl) {
      otelSpanBuilder.setParent(Context.current().with((OpenTelemetrySpanImpl) ocParent));
    }
    if (ocRemoteParentSpanContext != null) {
      otelSpanBuilder.addLink(SpanConverter.mapSpanContext(ocRemoteParentSpanContext));
    }
    if (otelKind != null) {
      otelSpanBuilder.setSpanKind(otelKind);
    }
    if (!otelParentLinks.isEmpty()) {
      for (io.opentelemetry.api.trace.SpanContext spanContext : otelParentLinks) {
        otelSpanBuilder.addLink(spanContext);
      }
    }
    io.opentelemetry.api.trace.Span otSpan = otelSpanBuilder.startSpan();
    return new OpenTelemetrySpanImpl(otSpan);
  }

  private OpenTelemetrySpanBuilderImpl(
      String name,
      @Nullable SpanContext ocRemoteParentSpanContext,
      @Nullable Span ocParent,
      OpenTelemetrySpanBuilderImpl.Options options) {
    this.name = checkNotNull(name, "name");
    this.ocParent = ocParent;
    this.ocRemoteParentSpanContext = ocRemoteParentSpanContext;
    this.options = options;
  }

  static OpenTelemetrySpanBuilderImpl createWithParent(
      String spanName, @Nullable Span parent, OpenTelemetrySpanBuilderImpl.Options options) {
    return new OpenTelemetrySpanBuilderImpl(spanName, null, parent, options);
  }

  static OpenTelemetrySpanBuilderImpl createWithRemoteParent(
      String spanName,
      @Nullable SpanContext remoteParentSpanContext,
      OpenTelemetrySpanBuilderImpl.Options options) {
    return new OpenTelemetrySpanBuilderImpl(spanName, remoteParentSpanContext, null, options);
  }

  private static boolean makeSamplingDecision(
      @Nullable SpanContext parent,
      @Nullable Boolean hasRemoteParent,
      String name,
      @Nullable Sampler sampler,
      List<Span> parentLinks,
      TraceId traceId,
      SpanId spanId,
      TraceParams activeTraceParams) {
    // If users set a specific sampler in the SpanBuilder, use it.
    if (sampler != null) {
      return sampler.shouldSample(parent, hasRemoteParent, traceId, spanId, name, parentLinks);
    }
    // Use the default sampler if this is a root Span or this is an entry point Span (has remote
    // parent).
    if (Boolean.TRUE.equals(hasRemoteParent) || parent == null || !parent.isValid()) {
      return activeTraceParams
          .getSampler()
          .shouldSample(parent, hasRemoteParent, traceId, spanId, name, parentLinks);
    }
    // Parent is always different than null because otherwise we use the default sampler.
    return parent.getTraceOptions().isSampled() || isAnyParentLinkSampled(parentLinks);
  }

  private static boolean isAnyParentLinkSampled(List<Span> parentLinks) {
    for (Span parentLink : parentLinks) {
      if (parentLink.getContext().getTraceOptions().isSampled()) {
        return true;
      }
    }
    return false;
  }

  static final class Options {
    private final RandomHandler randomHandler;
    private final TraceConfig traceConfig;

    Options(RandomHandler randomHandler, TraceConfig traceConfig) {
      this.randomHandler = checkNotNull(randomHandler, "randomHandler");
      this.traceConfig = checkNotNull(traceConfig, "traceConfig");
    }
  }
}
