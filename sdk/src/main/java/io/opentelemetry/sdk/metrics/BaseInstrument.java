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
import java.util.List;
import java.util.Map;

abstract class BaseInstrument<B> implements Instrument<B> {

  private final String name;
  private final String description;
  private final Map<String, String> constantLabels;
  private final List<String> labelKeys;

  BaseInstrument(
      String name, String description, Map<String, String> constantLabels, List<String> labelKeys) {
    this.name = name;
    this.description = description;
    this.constantLabels = constantLabels;
    this.labelKeys = labelKeys;
  }

  abstract B createBoundInstrument(LabelSet labelSet);

  @Override
  public B bind(LabelSet labelSet) {
    return createBoundInstrument(labelSet);
    // todo: associate with an aggregator/accumulator
  }

  @Override
  public void unbind(B boundInstrument) {
    // todo: pass through to an aggregator/accumulator
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
    int result = name != null ? name.hashCode() : 0;
    result = 31 * result + (description != null ? description.hashCode() : 0);
    result = 31 * result + (constantLabels != null ? constantLabels.hashCode() : 0);
    result = 31 * result + (labelKeys != null ? labelKeys.hashCode() : 0);
    return result;
  }
}
