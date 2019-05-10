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

import io.opentelemetry.tags.Tagger;
import io.opentelemetry.trace.Tracer;

/**
 * Utility class that holds a Tracer and a Tagger. This is required as OpenTracing SpanContext does
 * tag propagation along the Span information.
 */
final class TraceTagInfo {
  final Tracer tracer;
  final Tagger tagger;

  TraceTagInfo(Tracer tracer, Tagger tagger) {
    this.tracer = tracer;
    this.tagger = tagger;
  }

  Tracer tracer() {
    return tracer;
  }

  Tagger tagger() {
    return tagger;
  }
}
