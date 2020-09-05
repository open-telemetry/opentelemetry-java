/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.correlationcontext;

import java.util.Collection;
import java.util.Collections;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * An immutable implementation of the {@link CorrelationContext} that does not contain any entries.
 */
@Immutable
public class EmptyCorrelationContext implements CorrelationContext {
  /**
   * Returns the single instance of the {@link EmptyCorrelationContext} class.
   *
   * @return the single instance of the {@code EmptyCorrelationContext} class.
   * @since 0.1.0
   */
  public static CorrelationContext getInstance() {
    return INSTANCE;
  }

  private static final CorrelationContext INSTANCE = new EmptyCorrelationContext();

  @Override
  public Collection<Entry> getEntries() {
    return Collections.emptyList();
  }

  @Nullable
  @Override
  public String getEntryValue(String entryKey) {
    return null;
  }

  private EmptyCorrelationContext() {}
}
