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

package io.opentelemetry.sdk.correlationcontext;

import io.opentelemetry.context.Scope;
import io.opentelemetry.correlationcontext.CorrelationContext;
import io.opentelemetry.correlationcontext.CorrelationContextManager;
import io.opentelemetry.correlationcontext.CorrelationsContextUtils;

/**
 * {@link CorrelationContextManagerSdk} is SDK implementation of {@link CorrelationContextManager}.
 *
 * @since 0.3.0
 */
public class CorrelationContextManagerSdk implements CorrelationContextManager {

  /** @since 0.3.0 */
  @Override
  public CorrelationContext getCurrentContext() {
    return CorrelationsContextUtils.getCurrentCorrelationContext();
  }

  /** @since 0.3.0 */
  @Override
  public CorrelationContext.Builder contextBuilder() {
    return new CorrelationContextSdk.Builder();
  }

  /** @since 0.3.0 */
  @Override
  public Scope withContext(CorrelationContext distContext) {
    return CorrelationsContextUtils.currentContextWith(distContext);
  }
}
