/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Basic registry class for metrics instruments. The current implementation allows instruments to be
 * registered only once for a given name.
 *
 * <p>TODO: Discuss what is the right behavior when an already registered Instrument with the same
 * name is present. TODO: Decide what is the identifier for an Instrument? Only name?
 */
final class InstrumentRegistry {
  private final ConcurrentMap<String, AbstractInstrument> registry = new ConcurrentHashMap<>();

  /**
   * Registers the given {@code instrument} to this registry. Returns the registered instrument if
   * no other instrument with the same name is registered or a previously registered instrument with
   * same name and equal with the current instrument, otherwise throws an exception.
   *
   * @param instrument the newly created {@code Instrument}.
   * @return the given instrument if no instrument with same name already registered, otherwise the
   *     previous registered instrument.
   * @throws IllegalArgumentException if instrument cannot be registered.
   */
  @SuppressWarnings("unchecked")
  <I extends AbstractInstrument> I register(I instrument) {
    AbstractInstrument oldInstrument =
        registry.putIfAbsent(instrument.getDescriptor().getName().toLowerCase(), instrument);
    if (oldInstrument != null) {
      if (!instrument.getClass().isInstance(oldInstrument) || !instrument.equals(oldInstrument)) {
        throw new IllegalArgumentException(
            "Instrument with same name and different descriptor already created.");
      }
      return (I) oldInstrument;
    }
    return instrument;
  }

  /**
   * Returns a {@code Collection} view of the registered instruments.
   *
   * @return a {@code Collection} view of the registered instruments.
   */
  Collection<AbstractInstrument> getInstruments() {
    return Collections.unmodifiableCollection(new ArrayList<>(registry.values()));
  }
}
