/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.context;

import java.util.concurrent.ScheduledExecutorService;

/** A {@link ScheduledExecutorService} that implements methods that don't need {@link Context}. */
abstract class ForwardingScheduledExecutorService extends ForwardingExecutorService
    implements ScheduledExecutorService {

  private final ScheduledExecutorService delegate;

  protected ForwardingScheduledExecutorService(ScheduledExecutorService delegate) {
    super(delegate);
    this.delegate = delegate;
  }

  @Override
  ScheduledExecutorService delegate() {
    return delegate;
  }
}
