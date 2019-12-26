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

import io.opentelemetry.metrics.LabelSet;
import io.opentelemetry.metrics.LongCounter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class SdkLongCounterBuilder extends AbstractCounterBuilder<LongCounter.Builder, LongCounter>
    implements LongCounter.Builder {

  protected SdkLongCounterBuilder(String name) {
    super(name);
  }

  public static LongCounter.Builder builder(String name) {
    return new SdkLongCounterBuilder(name);
  }

  @Override
  SdkLongCounterBuilder getThis() {
    return this;
  }

  @Override
  public LongCounter build() {
    return new SdkLongCounter(getName(), getDescription(), getMonotonic());
  }

  private static class SdkLongCounter implements LongCounter {

    private final Map<LabelSet, BoundLongCounter> boundCounters = new HashMap<>();
    private final String description;
    private final boolean monotonic;
    private final String name;

    public SdkLongCounter(String name, String description, boolean monotonic) {
      this.description = description;
      this.monotonic = monotonic;
      this.name = name;
    }

    @Override
    public void add(long delta, LabelSet labelSet) {
      getOrCreate(labelSet).add(delta);
    }

    @Override
    public BoundLongCounter bind(LabelSet labelSet) {
      return getOrCreate(labelSet);
    }

    private BoundLongCounter getOrCreate(LabelSet labelSet) {
      BoundLongCounter boundLongCounter = boundCounters.get(labelSet);
      if (boundLongCounter == null) {
        boundLongCounter = new LongCounterImpl(labelSet);
        boundCounters.put(labelSet, boundLongCounter);
      }
      return boundLongCounter;
    }

    @Override
    public void unbind(BoundLongCounter boundInstrument) {
      boundCounters.remove(((LongCounterImpl) boundInstrument).labels);
    }

    @Override
    public String toString() {
      return "SdkLongCounter{"
          + "boundCounters="
          + boundCounters
          + ", description='"
          + description
          + '\''
          + ", monotonic="
          + monotonic
          + ", name='"
          + name
          + '\''
          + '}';
    }

    private class LongCounterImpl implements BoundLongCounter {
      private final LabelSet labels;
      private final AtomicLong value = new AtomicLong();

      private LongCounterImpl(LabelSet labels) {
        this.labels = labels;
      }

      @Override
      public void add(long delta) {
        if (monotonic && delta < 0) {
          throw new IllegalArgumentException("monotonic counters can only increase");
        }
        value.addAndGet(delta);
      }
    }
  }
}
