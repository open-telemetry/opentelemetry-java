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

import io.opentelemetry.metrics.Instrument;
import io.opentelemetry.metrics.LabelSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BaseInstrument<B> implements Instrument<B> {

  private final Map<LabelSet, B> boundCounters = new HashMap<>();

  private final String name;
  private final String description;
  private final Map<String, String> constantLabels;
  private final List<String> labelKeys;

  protected BaseInstrument(
      String name, String description, Map<String, String> constantLabels, List<String> labelKeys) {
    this.name = name;
    this.description = description;
    this.constantLabels = constantLabels;
    this.labelKeys = labelKeys;
  }

  @Override
  public B bind(LabelSet labelSet) {
    return getOrCreate(labelSet);
  }

  private B getOrCreate(LabelSet labelSet) {
    B boundLongCounter = boundCounters.get(labelSet);
    if (boundLongCounter == null) {
      boundLongCounter = create(labelSet);
      boundCounters.put(labelSet, boundLongCounter);
    }
    return boundLongCounter;
  }

  protected abstract B create(LabelSet labelSet);

  @SuppressWarnings("rawtypes")
  @Override
  public void unbind(B boundInstrument) {
    boundCounters.remove(((BaseBoundInstrument) boundInstrument).labels);
  }

  @Override
  public String toString() {
    return "BaseInstrument{"
        + "boundCounters="
        + boundCounters
        + ", name='"
        + name
        + '\''
        + ", description='"
        + description
        + '\''
        + ", constantLabels="
        + constantLabels
        + ", labelKeys="
        + labelKeys
        + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof BaseInstrument)) {
      return false;
    }

    BaseInstrument<?> that = (BaseInstrument<?>) o;

    if (boundCounters != null
        ? !boundCounters.equals(that.boundCounters)
        : that.boundCounters != null) {
      return false;
    }
    if (name != null ? !name.equals(that.name) : that.name != null) {
      return false;
    }
    if (description != null ? !description.equals(that.description) : that.description != null) {
      return false;
    }
    if (constantLabels != null
        ? !constantLabels.equals(that.constantLabels)
        : that.constantLabels != null) {
      return false;
    }
    return labelKeys != null ? labelKeys.equals(that.labelKeys) : that.labelKeys == null;
  }

  @Override
  public int hashCode() {
    int result = boundCounters != null ? boundCounters.hashCode() : 0;
    result = 31 * result + (name != null ? name.hashCode() : 0);
    result = 31 * result + (description != null ? description.hashCode() : 0);
    result = 31 * result + (constantLabels != null ? constantLabels.hashCode() : 0);
    result = 31 * result + (labelKeys != null ? labelKeys.hashCode() : 0);
    return result;
  }

  static class BaseBoundInstrument<I extends BaseInstrument<?>> {

    private final I instrument;
    private final LabelSet labels;

    BaseBoundInstrument(I instrument, LabelSet labels) {
      this.instrument = instrument;
      this.labels = labels;
    }

    @Override
    public String toString() {
      return "BaseBoundInstrument{" + "instrument=" + instrument + ", labels=" + labels + '}';
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof BaseBoundInstrument)) {
        return false;
      }

      BaseBoundInstrument<?> that = (BaseBoundInstrument<?>) o;

      if (instrument != null ? !instrument.equals(that.instrument) : that.instrument != null) {
        return false;
      }
      return labels != null ? labels.equals(that.labels) : that.labels == null;
    }

    @Override
    public int hashCode() {
      int result = instrument != null ? instrument.hashCode() : 0;
      result = 31 * result + (labels != null ? labels.hashCode() : 0);
      return result;
    }
  }
}
