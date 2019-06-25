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
   * @param startAction actions available during {@link SpanBuilder#startSpan()} invocation.
   * @return {@link SpanContext} of newly started span. Implementations are allowed to modify {@link
   *     SpanContext} returned from {@link StartAction#generateContext(SpanBuilder)}.
   * @since 0.1.0
   */
  SpanContext onStartSpan(StartAction startAction);

  /**
   * {@link StartAction} exposes actions or steps available during {@link SpanBuilder#startSpan()}
   * call.
   *
   * @since 0.1.0
   */
  interface StartAction {

    /**
     * Generate span context. Implementation has to call this method. The rer
     *
     * @param spanBuilder span builder associated with the request. It can be obtained via {@link
     *     #spanBuilder()}.
     * @return SpanContext of the span which is being started.
     * @since 0.1.0
     */
    SpanContext generateContext(SpanBuilder spanBuilder);

    /**
     * Returns a {@link SpanBuilder} associated with the chain.
     *
     * @return span builder associated with the request.
     * @since 0.1.0
     */
    SpanBuilder spanBuilder();
  }
}
