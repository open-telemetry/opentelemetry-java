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

package io.opentelemetry.sdk.metrics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

// Basic registry class for metrics instruments. The current implementation allows instruments to be
// registered only once for a given name.
//
// TODO: Discuss what is the right behavior when an already registered Instrument with the same name
//  is present.
// TODO: Decide what is the identifier for an Instrument? Only name?
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
        registry.putIfAbsent(instrument.getDescriptor().getName(), instrument);
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
