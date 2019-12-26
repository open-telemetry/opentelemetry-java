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

import static java.util.Collections.unmodifiableMap;

import com.google.auto.value.AutoValue;
import io.opentelemetry.metrics.LabelSet;
import java.util.HashMap;
import java.util.Map;

@AutoValue
abstract class SdkLabelSet implements LabelSet {

  private static final LabelSet EMPTY = builder().build();

  public static LabelSet empty() {
    return EMPTY;
  }

  abstract Map<String, String> getLabels();

  static Builder builder() {
    return new Builder();
  }

  static class Builder {

    private final Map<String, String> labels = new HashMap<>();

    public Builder add(String key, String value) {
      labels.put(key, value);
      return this;
    }

    public LabelSet build() {
      return new AutoValue_SdkLabelSet(unmodifiableMap(new HashMap<>(labels)));
    }
  }
}
