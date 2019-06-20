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

package io.opentelemetry.sdk.distributedcontext;

import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.BinaryFormat;
import io.opentelemetry.context.propagation.HttpTextFormat;
import io.opentelemetry.distributedcontext.DistributedContext;
import io.opentelemetry.distributedcontext.DistributedContextManager;
import io.opentelemetry.distributedcontext.unsafe.ContextUtils;

/**
 * {@link DistributedContextManagerSdk} is SDK implementation of {@link DistributedContextManager}.
 */
public class DistributedContextManagerSdk implements DistributedContextManager {

  @Override
  public DistributedContext getCurrentContext() {
    return ContextUtils.getValue();
  }

  @Override
  public DistributedContext.Builder contextBuilder() {
    return new DistributedContextSdk.Builder();
  }

  @Override
  public Scope withContext(DistributedContext distContext) {
    return ContextUtils.withDistributedContext(distContext);
  }

  @Override
  public BinaryFormat<DistributedContext> getBinaryFormat() {
    throw new UnsupportedOperationException("to be implemented");
  }

  @Override
  public HttpTextFormat<DistributedContext> getHttpTextFormat() {
    throw new UnsupportedOperationException("to be implemented");
  }
}
