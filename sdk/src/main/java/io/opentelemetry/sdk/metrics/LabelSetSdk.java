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
import io.opentelemetry.internal.Utils;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.concurrent.Immutable;

@AutoValue
@Immutable
abstract class LabelSetSdk {
  private static final LabelSetSdk EMPTY =
      new AutoValue_LabelSetSdk(Collections.<String, String>emptyMap());

  static LabelSetSdk create(Map<String, String> labels) {
    if (labels == null || labels.isEmpty()) {
      return EMPTY;
    }
    return new AutoValue_LabelSetSdk(unmodifiableMap(labels));
  }

  static LabelSetSdk create(String... keyValuePairs) {
    if (keyValuePairs.length == 0) {
      return EMPTY;
    }
    Utils.validateLabelPairs(keyValuePairs);
    Map<String, String> data = new HashMap<>(keyValuePairs.length / 2);
    for (int i = 0; i < keyValuePairs.length; i++) {
      String key = keyValuePairs[i];
      data.put(key, keyValuePairs[++i]);
    }
    return new AutoValue_LabelSetSdk(Collections.unmodifiableMap(data));
  }

  abstract Map<String, String> getLabels();
}
