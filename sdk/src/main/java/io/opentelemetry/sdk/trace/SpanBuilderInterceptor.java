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

import io.opencensus.trace.SpanBuilder;
import io.opentelemetry.trace.SpanContext;

/**
 * {@link SpanBuilderInterceptor} intercepts calls on SpanBuilder.
 *
 * @since 0.1.0
 */
public interface SpanBuilderInterceptor {

  /**
   * Intercept {@link SpanBuilder#startSpan()} calls.
   *
   * @param chain interceptor chain.
   * @return original or modified span context for newly generated span.
   * @since 0.1.0
   */
  SpanContext onStartSpan(Chain chain);

  /**
   * Chain ties together execution of {@link SpanBuilderInterceptor}s.
   *
   * @since 0.1.0
   */
  interface Chain {

    /**
     * Proceed to the next interceptor.
     *
     * @param spanBuilder span builder associated with the request.
     * @return SpanContext of the span which is being started or modified {@link SpanContext} from
     *     the previous interceptor.
     * @since 0.1.0
     */
    SpanContext proceed(SpanBuilder spanBuilder);

    /**
     * Returns a {@link SpanBuilder} associated with the chain.
     *
     * @return span builder associated with the request.
     * @since 0.1.0
     */
    SpanBuilder spanBuilder();
  }
}
