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

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.metrics.Observer;

abstract class AbstractObserverBuilder<B extends Observer.Builder<B, V>, V>
    extends AbstractInstrumentBuilder<B, V> implements Observer.Builder<B, V> {
  private boolean monotonic = false;

  protected AbstractObserverBuilder(String name) {
    super(name);
  }

  @Override
  public final B setMonotonic(boolean monotonic) {
    this.monotonic = monotonic;
    return getThis();
  }

  final boolean getMonotonic() {
    return this.monotonic;
  }
}
