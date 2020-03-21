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

package io.opentelemetry.contrib.spring.boot;

import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.sdk.trace.Sampler;
import io.opentelemetry.trace.Link;
import io.opentelemetry.trace.Span.Kind;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.TraceId;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

/** Test only. */
public class TestOnlySampler implements Sampler {

  private int reservoir;
  private double rate;
  private boolean enabled;

  public int getReservoir() {
    return reservoir;
  }

  public void setReservoir(int reservoir) {
    this.reservoir = reservoir;
  }

  public double getRate() {
    return rate;
  }

  public void setRate(double rate) {
    this.rate = rate;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  @Override
  public Decision shouldSample(
      @Nullable SpanContext parentContext,
      TraceId traceId,
      SpanId spanId,
      String name,
      Kind spanKind,
      Map<String, AttributeValue> attributes,
      List<Link> parentLinks) {
    return new TestOnlyDecision();
  }

  @Override
  public String getDescription() {
    return "Only for unit testing";
  }

  static class TestOnlyDecision implements Decision {

    @Override
    public boolean isSampled() {
      return false;
    }

    @Override
    public Map<String, AttributeValue> attributes() {
      return Collections.emptyMap();
    }
  }
}
