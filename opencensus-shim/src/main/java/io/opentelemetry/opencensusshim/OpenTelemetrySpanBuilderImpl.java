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

import io.opencensus.common.Clock;
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
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;

public class OpenTelemetrySpanBuilderImpl extends SpanBuilder {
  private static final Tracer TRACER =
      OpenTelemetry.getGlobalTracer("io.opencensus.opentelemetry.migration");
  private static final Tracestate TRACESTATE_DEFAULT = Tracestate.builder().build();
  private static final TraceOptions SAMPLED_TRACE_OPTIONS =
      TraceOptions.builder().setIsSampled(true).build();
  private static final TraceOptions NOT_SAMPLED_TRACE_OPTIONS =
      TraceOptions.builder().setIsSampled(false).build();

  private final String name;
  private final Options options;

  private List<Span> parentLinks = Collections.emptyList();
  private final List<io.opentelemetry.api.trace.SpanContext> otelParentLinks = new ArrayList<>();
  @Nullable private final Span parent;
  @Nullable private final SpanContext remoteParentSpanContext;
  @Nullable private Sampler sampler;
  @Nullable private Boolean recordEvents;
  @Nullable private io.opentelemetry.api.trace.Span.Kind kind;

  @Override
  public SpanBuilder setSampler(Sampler sampler) {
    this.sampler = checkNotNull(sampler, "sampler");
    return this;
  }

  @Override
  public SpanBuilder setParentLinks(List<Span> parentLinks) {
    this.parentLinks = checkNotNull(parentLinks, "parentLinks");
    for (Span parent : parentLinks) {
      this.otelParentLinks.add(
          io.opentelemetry.api.trace.SpanContext.create(
              io.opentelemetry.api.trace.TraceId.bytesToHex(
                  parent.getContext().getTraceId().getBytes()),
              io.opentelemetry.api.trace.SpanId.bytesToHex(
                  parent.getContext().getSpanId().getBytes()),
              TraceFlags.getDefault(),
              TraceState.getDefault()));
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
    this.kind = mapKind(kind);
    return this;
  }

  @Override
  public Span startSpan() {
    // To determine whether to sample this span
    TraceParams activeTraceParams = options.traceConfig.getActiveTraceParams();
    Random random = options.randomHandler.current();
    TraceId traceId;
    SpanId spanId = SpanId.generateRandomId(random);
    Tracestate tracestate = TRACESTATE_DEFAULT;
    SpanContext parentContext = null;
    Boolean hasRemoteParent = null;
    if (remoteParentSpanContext != null && remoteParentSpanContext.isValid()) {
      parentContext = remoteParentSpanContext;
      hasRemoteParent = Boolean.TRUE;
      traceId = parentContext.getTraceId();
      tracestate = parentContext.getTracestate();
    } else if (parent != null && parent.getContext().isValid()) {
      parentContext = parent.getContext();
      hasRemoteParent = Boolean.FALSE;
      traceId = parentContext.getTraceId();
      tracestate = parentContext.getTracestate();
    } else {
      // New root span.
      traceId = TraceId.generateRandomId(random);
    }
    TraceOptions traceOptions =
        makeSamplingDecision(
                parentContext,
                hasRemoteParent,
                name,
                sampler,
                parentLinks,
                traceId,
                spanId,
                activeTraceParams)
            ? SAMPLED_TRACE_OPTIONS
            : NOT_SAMPLED_TRACE_OPTIONS;
    if (!traceOptions.isSampled() && !Boolean.TRUE.equals(recordEvents)) {
      return OpenTelemetryNoRecordEventsSpanImpl.create(
          SpanContext.create(traceId, spanId, traceOptions, tracestate));
    }

    // If sampled
    io.opentelemetry.api.trace.SpanBuilder otSpanBuidler =
        TRACER.spanBuilder(name).setStartTimestamp(options.clock.nowNanos(), TimeUnit.NANOSECONDS);
    if (parent != null && parent instanceof OpenTelemetrySpanImpl) {
      otSpanBuidler.setParent(Context.root().with((OpenTelemetrySpanImpl) parent));
    }
    if (remoteParentSpanContext != null) {
      otSpanBuidler.addLink(
          io.opentelemetry.api.trace.SpanContext.create(
              io.opentelemetry.api.trace.TraceId.bytesToHex(
                  remoteParentSpanContext.getTraceId().getBytes()),
              io.opentelemetry.api.trace.SpanId.bytesToHex(
                  remoteParentSpanContext.getSpanId().getBytes()),
              TraceFlags.getDefault(),
              TraceState.getDefault()));
    }
    if (kind != null) {
      otSpanBuidler.setSpanKind(kind);
    }
    if (!otelParentLinks.isEmpty()) {
      for (io.opentelemetry.api.trace.SpanContext spanContext : otelParentLinks) {
        otSpanBuidler.addLink(spanContext);
      }
    }
    io.opentelemetry.api.trace.Span otSpan = otSpanBuidler.startSpan();
    return new OpenTelemetrySpanImpl(otSpan);
  }

  private OpenTelemetrySpanBuilderImpl(
      String name,
      @Nullable SpanContext remoteParentSpanContext,
      @Nullable Span parent,
      OpenTelemetrySpanBuilderImpl.Options options) {
    this.name = checkNotNull(name, "name");
    this.parent = parent;
    this.remoteParentSpanContext = remoteParentSpanContext;
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
    private final Clock clock;
    private final TraceConfig traceConfig;

    Options(RandomHandler randomHandler, Clock clock, TraceConfig traceConfig) {
      this.randomHandler = checkNotNull(randomHandler, "randomHandler");
      this.clock = checkNotNull(clock, "clock");
      this.traceConfig = checkNotNull(traceConfig, "traceConfig");
    }
  }
}
