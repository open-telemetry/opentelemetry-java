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
import com.google.auto.value.extension.memoized.Memoized;
import io.opentelemetry.common.Labels;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import javax.annotation.concurrent.Immutable;

@AutoValue
@Immutable
public abstract class InstrumentDescriptor {
  static InstrumentDescriptor create(
      String name,
      String description,
      String unit,
      Labels constantLabels,
      InstrumentType type,
      InstrumentValueType valueType) {
    return new AutoValue_InstrumentDescriptor(
        name, description, unit, constantLabels, type, valueType);
  }

  abstract String getName();

  abstract String getDescription();

  abstract String getUnit();

  abstract Labels getConstantLabels();

  public abstract InstrumentType getType();

  abstract InstrumentValueType getValueType();

  @Memoized
  @Override
  public abstract int hashCode();
}
