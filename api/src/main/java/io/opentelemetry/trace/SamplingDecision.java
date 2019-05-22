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

package io.opentelemetry.trace;

import java.util.List;
import java.util.Map;

/**
 * Sampling decision returned by {@link Sampler#shouldSample(SpanContext, Boolean, TraceId, SpanId,
 * String, List)}.
 *
 * @since 0.1.0
 */
public interface SamplingDecision {

  /**
   * Return sampling decision whether span should be sampled or not.
   *
   * @return sampling decision.
   */
  boolean isSampled();

  /**
   * Return tags which will be attached to the span.
   *
   * @return tags which will be added to the span.
   */
  Map<String, Object> tags();
}
