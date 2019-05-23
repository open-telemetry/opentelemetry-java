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

package io.opentelemetry.trace.samplers;

import io.opentelemetry.trace.AttributeValue;
import io.opentelemetry.trace.Sampler;
import java.util.Collections;
import java.util.Map;

/**
 * Sampling decision without tags.
 *
 * @since 0.1.0
 */
public final class DefaultSamplingDecision implements Sampler.SamplingDecision {

  private final boolean decision;

  /**
   * Creates sampling decision without tags.
   *
   * @param decision sampling decision
   * @since 0.1.0
   */
  DefaultSamplingDecision(boolean decision) {
    this.decision = decision;
  }

  @Override
  public boolean isSampled() {
    return decision;
  }

  @Override
  public Map<String, AttributeValue> attributes() {
    return Collections.emptyMap();
  }
}
