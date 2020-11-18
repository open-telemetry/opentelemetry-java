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

import io.opencensus.common.Clock;
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
      RandomHandler randomHandler, Clock clock, TraceConfig traceConfig) {
    spanBuilderOptions = new Options(randomHandler, clock, traceConfig);
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
