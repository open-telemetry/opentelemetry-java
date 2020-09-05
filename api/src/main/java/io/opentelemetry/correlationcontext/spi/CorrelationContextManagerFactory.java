/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.correlationcontext.spi;

import io.opentelemetry.correlationcontext.CorrelationContextManager;
import javax.annotation.concurrent.ThreadSafe;

/**
 * CorrelationContextManagerFactory is a service provider for {@link CorrelationContextManager}.
 * Fully qualified class name of the implementation should be registered in {@code
 * META-INF/services/io.opentelemetry.correlationcontext.spi.CorrelationContextManagerFactory}. <br>
 * <br>
 * A specific implementation can be selected by a system property {@code
 * io.opentelemetry.correlationcontext.spi.CorrelationContextManagerFactory} with value of fully
 * qualified class name.
 *
 * @see io.opentelemetry.OpenTelemetry
 */
@ThreadSafe
public interface CorrelationContextManagerFactory {

  /**
   * Creates a new {@code CorrelationContextManager} instance.
   *
   * @return a {@code CorrelationContextManager} instance.
   * @since 0.1.0
   */
  CorrelationContextManager create();
}
