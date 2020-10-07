/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.baggage;

import java.util.Collection;
import java.util.Collections;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/** An immutable implementation of the {@link Baggage} that does not contain any entries. */
@Immutable
public class EmptyBaggage implements Baggage {
  /**
   * Returns the single instance of the {@link EmptyBaggage} class.
   *
   * @return the single instance of the {@code EmptyBaggage} class.
   * @since 0.9.0
   */
  public static Baggage getInstance() {
    return INSTANCE;
  }

  private static final Baggage INSTANCE = new EmptyBaggage();

  @Override
  public Collection<Entry> getEntries() {
    return Collections.emptyList();
  }

  @Nullable
  @Override
  public String getEntryValue(String entryKey) {
    return null;
  }

  private EmptyBaggage() {}
}
