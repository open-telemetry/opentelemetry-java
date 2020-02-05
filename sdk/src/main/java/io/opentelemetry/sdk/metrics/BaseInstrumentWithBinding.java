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

import io.opentelemetry.metrics.InstrumentWithBinding;
import io.opentelemetry.metrics.LabelSet;
import java.util.List;
import java.util.Map;

abstract class BaseInstrumentWithBinding<B> extends BaseInstrument
    implements InstrumentWithBinding<B> {

  BaseInstrumentWithBinding(
      String name, String description, Map<String, String> constantLabels, List<String> labelKeys) {
    super(name, description, constantLabels, labelKeys);
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
}
