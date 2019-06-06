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

package io.opentelemetry.distributedcontext.unsafe;

import io.grpc.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.distributedcontext.DistributedContext;

/**
 * A scope that manages the {@link Context} for a {@link DistributedContext}.
 *
 * @since 0.1.0
 */
final class DistributedContextInScope implements Scope {
  private final Context orig;

  private DistributedContextInScope(DistributedContext distContext) {
    orig = ContextUtils.withValue(distContext).attach();
  }

  /**
   * Constructs a new {@link DistributedContextInScope}.
   *
   * @param distContext the {@code DistributedContext} to be added to the current {@code Context}.
   * @since 0.1.0
   */
  static DistributedContextInScope create(DistributedContext distContext) {
    return new DistributedContextInScope(distContext);
  }

  @Override
  public void close() {
    Context.current().detach(orig);
  }
}
