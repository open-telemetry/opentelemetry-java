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

class BaseBoundInstrument {
  private final LabelSet labels;

  BaseBoundInstrument(LabelSet labels) {
    this.labels = labels;
    // todo: associate with an aggregator/accumulator
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof BaseBoundInstrument)) {
      return false;
    }

    BaseBoundInstrument that = (BaseBoundInstrument) o;

    return labels.equals(that.labels);
  }

  @Override
  public int hashCode() {
    return labels.hashCode();
  }
}
