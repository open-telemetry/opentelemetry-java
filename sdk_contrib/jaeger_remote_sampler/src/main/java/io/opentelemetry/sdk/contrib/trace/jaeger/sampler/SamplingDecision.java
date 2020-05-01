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

package io.opentelemetry.sdk.contrib.trace.jaeger.sampler;

import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.sdk.trace.Sampler.Decision;
import java.util.Map;

class SamplingDecision implements Decision {

  private final boolean sampled;
  private final Map<String, AttributeValue> attributes;

  SamplingDecision(boolean sampled, Map<String, AttributeValue> attributes) {
    this.sampled = sampled;
    this.attributes = attributes;
  }

  @Override
  public boolean isSampled() {
    return sampled;
  }

  @Override
  public Map<String, AttributeValue> getAttributes() {
    return attributes;
  }
}
