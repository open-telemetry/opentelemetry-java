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

import io.opentelemetry.correlationcontext.spi.CorrelationContextManagerProvider;
import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public final class DefaultCorrelationContextManagerProvider
    implements CorrelationContextManagerProvider {
  private static final CorrelationContextManagerProvider instance =
      new DefaultCorrelationContextManagerProvider();

  /**
   * Returns a {@code CorrelationContextManagerProvider} singleton that is the default
   * implementation for {@link CorrelationContextManager}.
   *
   * @return a {@code CorrelationContextManagerProvider} singleton that is the default
   *     implementation for {@link CorrelationContextManager}.
   */
  public static CorrelationContextManagerProvider getInstance() {
    return instance;
  }

  @Override
  public CorrelationContextManager create() {
    return DefaultCorrelationContextManager.getInstance();
  }

  private DefaultCorrelationContextManagerProvider() {}
}
