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
// is present.
// TODO: Decide what is the identifier for an Instrument? Only name?
// TODO: How do we know if an registered instrument is the same as the provided one?
final class InstrumentRegistry {
  private final ConcurrentMap<String, AbstractInstrument> registry = new ConcurrentHashMap<>();

  /**
   * Registers the given {@code instrument} to this registry.
   *
   * @param descriptor the descriptor of the {@code Instrument}.
   * @param instrument the newly created {@code Instrument}.
   * @return {@code true} if the instrument is successfully registered.
   */
  boolean register(InstrumentDescriptor descriptor, AbstractInstrument instrument) {
    AbstractInstrument oldInstrument = registry.putIfAbsent(descriptor.getName(), instrument);
    return oldInstrument == null;
  }

  /**
   * Returns a {@code Collection} view of the registered instruments.
   *
   * @return a {@code Collection} view of the registered instruments.
   */
  Collection<AbstractInstrument> getRegisteredInstruments() {
    return Collections.unmodifiableCollection(new ArrayList<>(registry.values()));
  }
}
