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

package io.opentelemetry.opentracingshim;

import io.opentelemetry.distributedcontext.DistributedContext;
import io.opentelemetry.distributedcontext.DistributedContextManager;
import io.opentelemetry.trace.Tracer;

/**
 * Utility class that holds a Tracer, a DistributedContextManager, and related objects that are core
 * part of the OT Shim layer.
 */
final class TelemetryInfo {
  private final Tracer tracer;
  private final DistributedContextManager contextManager;
  private final DistributedContext emptyDistributedContext;
  private final SpanContextShimTable spanContextShimTable;

  TelemetryInfo(Tracer tracer, DistributedContextManager contextManager) {
    this.tracer = tracer;
    this.contextManager = contextManager;
    this.emptyDistributedContext = contextManager.contextBuilder().build();
    this.spanContextShimTable = new SpanContextShimTable();
  }

  Tracer tracer() {
    return tracer;
  }

  DistributedContextManager contextManager() {
    return contextManager;
  }

  SpanContextShimTable spanContextShimTable() {
    return spanContextShimTable;
  }

  DistributedContext emptyDistributedContext() {
    return emptyDistributedContext;
  }
}
