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

import com.google.auto.value.AutoValue;
import java.util.Map;
import javax.annotation.concurrent.Immutable;

@AutoValue
@Immutable
abstract class InstrumentDescriptor {
  static InstrumentDescriptor create(
      String name, String description, String unit, Map<String, String> constantLabels) {
    return new AutoValue_InstrumentDescriptor(name, description, unit, constantLabels);
  }

  abstract String getName();

  abstract String getDescription();

  abstract String getUnit();

  abstract Map<String, String> getConstantLabels();
}
