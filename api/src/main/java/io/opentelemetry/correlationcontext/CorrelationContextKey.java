/*
 * Copyright 2020, OpenTelemetry Authors
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

package io.opentelemetry.correlationcontext;

import io.opentelemetry.context.Context;
import io.opentelemetry.context.Context.Key;

public class CorrelationContextKey {

  private static final Key<CorrelationContext> KEY = new Key<>("CorrelationContext");

  public static CorrelationContext get(Context context) {
    CorrelationContext correlationContext = context.get(KEY);
    return correlationContext == null ? EmptyCorrelationContext.getInstance() : correlationContext;
  }

  public static Context put(Context context, CorrelationContext span) {
    return context.put(KEY, span);
  }

  private CorrelationContextKey() {}
}
