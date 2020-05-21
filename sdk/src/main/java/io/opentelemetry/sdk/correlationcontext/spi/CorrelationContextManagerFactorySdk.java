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

package io.opentelemetry.sdk.correlationcontext.spi;

import io.opentelemetry.correlationcontext.CorrelationContextManager;
import io.opentelemetry.correlationcontext.spi.CorrelationContextManagerFactory;
import io.opentelemetry.sdk.correlationcontext.CorrelationContextManagerSdk;

/**
 * {@code CorrelationContextManager} provider implementation for {@link
 * CorrelationContextManagerFactory}.
 *
 * <p>This class is not intended to be used in application code and it is used only by {@link
 * io.opentelemetry.OpenTelemetry}.
 */
public final class CorrelationContextManagerFactorySdk implements CorrelationContextManagerFactory {

  @Override
  public CorrelationContextManager create() {
    return new CorrelationContextManagerSdk();
  }
}
