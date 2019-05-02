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

package io.opentelemetry.opencensusshim.trace.samplers;

import com.google.auto.value.AutoValue;
import io.opentelemetry.opencensusshim.internal.Utils;
import io.opentelemetry.opencensusshim.trace.Sampler;
import io.opentelemetry.opencensusshim.trace.Span;
import io.opentelemetry.opencensusshim.trace.SpanContext;
import io.opentelemetry.opencensusshim.trace.SpanId;
import io.opentelemetry.opencensusshim.trace.TraceId;
import java.util.List;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * We assume the lower 64 bits of the traceId's are randomly distributed around the whole (long)
 * range. We convert an incoming probability into an upper bound on that value, such that we can
 * just compare the absolute value of the id and the bound to see if we are within the desired
 * probability range. Using the low bits of the traceId also ensures that systems that only use 64
 * bit ID's will also work with this sampler.
 */
@AutoValue
@Immutable
abstract class ProbabilitySampler extends Sampler {

  ProbabilitySampler() {}

  abstract double getProbability();

  abstract long getIdUpperBound();

  /**
   * Returns a new {@link ProbabilitySampler}. The probability of sampling a trace is equal to that
   * of the specified probability.
   *
   * @param probability The desired probability of sampling. Must be within [0.0, 1.0].
   * @return a new {@link ProbabilitySampler}.
   * @throws IllegalArgumentException if {@code probability} is out of range
   */
  static ProbabilitySampler create(double probability) {
    Utils.checkArgument(
        probability >= 0.0 && probability <= 1.0, "probability must be in range [0.0, 1.0]");
    long idUpperBound;
    // Special case the limits, to avoid any possible issues with lack of precision across
    // double/long boundaries. For probability == 0.0, we use Long.MIN_VALUE as this guarantees
    // that we will never sample a trace, even in the case where the id == Long.MIN_VALUE, since
    // Math.Abs(Long.MIN_VALUE) == Long.MIN_VALUE.
    if (probability == 0.0) {
      idUpperBound = Long.MIN_VALUE;
    } else if (probability == 1.0) {
      idUpperBound = Long.MAX_VALUE;
    } else {
      idUpperBound = (long) (probability * Long.MAX_VALUE);
    }
    return new AutoValue_ProbabilitySampler(probability, idUpperBound);
  }

  @Override
  public final boolean shouldSample(
      @Nullable SpanContext parentContext,
      @Nullable Boolean hasRemoteParent,
      TraceId traceId,
      SpanId spanId,
      String name,
      @Nullable List<Span> parentLinks) {
    // If the parent is sampled keep the sampling decision.
    if (parentContext != null && parentContext.getTraceOptions().isSampled()) {
      return true;
    }
    if (parentLinks != null) {
      // If any parent link is sampled keep the sampling decision.
      for (Span parentLink : parentLinks) {
        if (parentLink.getContext().getTraceOptions().isSampled()) {
          return true;
        }
      }
    }
    // Always sample if we are within probability range. This is true even for child spans (that
    // may have had a different sampling decision made) to allow for different sampling policies,
    // and dynamic increases to sampling probabilities for debugging purposes.
    // Note use of '<' for comparison. This ensures that we never sample for probability == 0.0,
    // while allowing for a (very) small chance of *not* sampling if the id == Long.MAX_VALUE.
    // This is considered a reasonable tradeoff for the simplicity/performance requirements (this
    // code is executed in-line for every Span creation).
    return Math.abs(traceId.getLowerLong()) < getIdUpperBound();
  }

  @Override
  public final String getDescription() {
    return String.format("ProbabilitySampler{%.6f}", getProbability());
  }
}
