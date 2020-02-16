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

import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import java.util.List;
import java.util.Map;

abstract class AbstractMeasure extends AbstractInstrument {
  private final boolean absolute;
  private final InstrumentValueType instrumentValueType;

  AbstractMeasure(
      String name,
      String description,
      String unit,
      Map<String, String> constantLabels,
      List<String> labelKeys,
      InstrumentValueType instrumentValueType,
      MeterSharedState meterSharedState,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      boolean absolute) {
    super(name, description, unit, constantLabels, labelKeys);
    this.absolute = absolute;
    this.instrumentValueType = instrumentValueType;
  }

  final boolean isAbsolute() {
    return absolute;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof AbstractMeasure)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }

    AbstractMeasure that = (AbstractMeasure) o;

    return absolute == that.absolute && instrumentValueType == that.instrumentValueType;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (absolute ? 1 : 0);
    result = 31 * result + instrumentValueType.hashCode();
    return result;
  }
}
