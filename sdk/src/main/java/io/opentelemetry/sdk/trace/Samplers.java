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

package io.opentelemetry.sdk.trace;

import static io.opentelemetry.common.AttributeValue.doubleAttributeValue;

import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;
import io.opentelemetry.common.Attributes;
import io.opentelemetry.common.ReadableAttributes;
import io.opentelemetry.sdk.trace.Sampler.Decision;
import io.opentelemetry.trace.Link;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Span.Kind;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.TraceId;
import io.opentelemetry.trace.attributes.DoubleAttributeSetter;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Static class to access a set of pre-defined {@link Sampler Samplers}.
 *
 * @since 0.1.0
 */
@Immutable
public final class Samplers {

  private static final Decision EMPTY_SAMPLED_DECISION =
      DecisionImpl.createWithoutAttributes(/* decision= */ true);
  private static final Decision EMPTY_NOT_SAMPLED_DECISION =
      DecisionImpl.createWithoutAttributes(/* decision= */ false);

  /**
   * Probability value used by a probability-based Span sampling strategy.
   *
   * <p>Note: This will need to be updated if a specification for this value is merged which changes
   * this proposed value. Also, once it's in the spec, we should move it somewhere more visible.
   *
   * <p>See https://github.com/open-telemetry/opentelemetry-specification/pull/570
   */
  // Visible for tests.
  static final DoubleAttributeSetter SAMPLING_PROBABILITY =
      DoubleAttributeSetter.create("sampling.probability");

  // No instance of this class.
  private Samplers() {}

  /**
   * Returns a {@link Decision} with the given {@code attributes} and {@link Decision#isSampled()}
   * returning {@code isSampled}.
   *
   * <p>This is meant for use by custom {@link Sampler} implementations.
   *
   * <p>Using {@link #emptyDecision(boolean)} instead of this method is slightly faster and shorter
   * if you don't need attributes.
   *
   * @param isSampled The value to return from {@link Decision#isSampled()}.
   * @param attributes The attributes to return from {@link Decision#getAttributes()}. A different
   *     object instance with the same elements may be returned.
   * @return A {@link Decision} with the attributes equivalent to {@code attributes} and {@link
   *     Decision#isSampled()} returning {@code isSampled}.
   */
  public static Decision decision(boolean isSampled, Attributes attributes) {
    Objects.requireNonNull(attributes, "attributes");
    return attributes.isEmpty()
        ? emptyDecision(isSampled)
        : DecisionImpl.create(isSampled, attributes);
  }

  /**
   * Returns a {@link Decision} with empty attributes and {@link Decision#isSampled()} returning the
   * {@code isSampled}.
   *
   * <p>This is meant for use by custom {@link Sampler} implementations.
   *
   * <p>Use {@link #decision(boolean, Attributes)} if you need attributes.
   *
   * @param isSampled The value to return from {@link Decision#isSampled()}.
   * @return A {@link Decision} with empty attributes and {@link Decision#isSampled()} returning
   *     {@code isSampled}.
   */
  public static Decision emptyDecision(boolean isSampled) {
    return isSampled ? EMPTY_SAMPLED_DECISION : EMPTY_NOT_SAMPLED_DECISION;
  }

  /**
   * Returns a {@link Sampler} that always makes a "yes" decision on {@link Span} sampling.
   *
   * @return a {@code Sampler} that always makes a "yes" decision on {@code Span} sampling.
   * @since 0.1.0
   */
  public static Sampler alwaysOn() {
    return AlwaysOnSampler.INSTANCE;
  }

  /**
   * Returns a {@link Sampler} that always makes a "no" decision on {@link Span} sampling.
   *
   * @return a {@code Sampler} that always makes a "no" decision on {@code Span} sampling.
   * @since 0.1.0
   */
  public static Sampler alwaysOff() {
    return AlwaysOffSampler.INSTANCE;
  }

  /**
   * Returns a new Probability {@link Sampler}. The probability of sampling a trace is equal to that
   * of the specified probability.
   *
   * @param probability The desired probability of sampling. Must be within [0.0, 1.0].
   * @return a new Probability {@link Sampler}.
   * @throws IllegalArgumentException if {@code probability} is out of range
   */
  public static Sampler probability(double probability) {
    return Probability.create(probability);
  }

  @Immutable
  private enum AlwaysOnSampler implements Sampler {
    INSTANCE;

    // Returns always makes a "yes" decision on {@link Span} sampling.
    @Override
    public Decision shouldSample(
        @Nullable SpanContext parentContext,
        TraceId traceId,
        String name,
        Kind spanKind,
        ReadableAttributes attributes,
        List<Link> parentLinks) {
      return EMPTY_SAMPLED_DECISION;
    }

    @Override
    public String getDescription() {
      return "AlwaysOnSampler";
    }
  }

  @Immutable
  private enum AlwaysOffSampler implements Sampler {
    INSTANCE;

    // Returns always makes a "no" decision on {@link Span} sampling.
    @Override
    public Decision shouldSample(
        @Nullable SpanContext parentContext,
        TraceId traceId,
        String name,
        Kind spanKind,
        ReadableAttributes attributes,
        List<Link> parentLinks) {
      return EMPTY_NOT_SAMPLED_DECISION;
    }

    @Override
    public String getDescription() {
      return "AlwaysOffSampler";
    }
  }

  /**
   * We assume the lower 64 bits of the traceId's are randomly distributed around the whole (long)
   * range. We convert an incoming probability into an upper bound on that value, such that we can
   * just compare the absolute value of the id and the bound to see if we are within the desired
   * probability range. Using the low bits of the traceId also ensures that systems that only use 64
   * bit ID's will also work with this sampler.
   */
  @AutoValue
  @Immutable
  abstract static class Probability implements Sampler {

    Probability() {}

    static Probability create(double probability) {
      Preconditions.checkArgument(
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
      return new AutoValue_Samplers_Probability(
          probability,
          idUpperBound,
          DecisionImpl.createWithProbability(/* decision= */ true, probability),
          DecisionImpl.createWithProbability(/* decision= */ false, probability));
    }

    abstract double getProbability();

    abstract long getIdUpperBound();

    abstract Decision getPositiveDecision();

    abstract Decision getNegativeDecision();

    @Override
    public final Decision shouldSample(
        @Nullable SpanContext parentContext,
        TraceId traceId,
        String name,
        Kind spanKind,
        ReadableAttributes attributes,
        @Nullable List<Link> parentLinks) {
      // If the parent is sampled keep the sampling decision.
      if (parentContext != null && parentContext.getTraceFlags().isSampled()) {
        return EMPTY_SAMPLED_DECISION;
      }
      if (parentLinks != null) {
        // If any parent link is sampled keep the sampling decision.
        for (Link parentLink : parentLinks) {
          if (parentLink.getContext().getTraceFlags().isSampled()) {
            return EMPTY_SAMPLED_DECISION;
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
      return Math.abs(traceId.getTraceRandomPart()) < getIdUpperBound()
          ? getPositiveDecision()
          : getNegativeDecision();
    }

    @Override
    public final String getDescription() {
      return String.format("ProbabilitySampler{%.6f}", getProbability());
    }
  }

  @Immutable
  @AutoValue
  abstract static class DecisionImpl implements Decision {
    /**
     * Creates sampling decision with probability attribute.
     *
     * @param decision sampling decision
     * @param probability the probability that was used for the decision.
     */
    static Decision createWithProbability(boolean decision, double probability) {
      return new AutoValue_Samplers_DecisionImpl(
          decision, Attributes.of(SAMPLING_PROBABILITY.key(), doubleAttributeValue(probability)));
    }

    /**
     * Creates sampling decision without attributes.
     *
     * @param decision sampling decision
     */
    static Decision createWithoutAttributes(boolean decision) {
      return new AutoValue_Samplers_DecisionImpl(decision, Attributes.empty());
    }

    /**
     * Creates sampling decision with the given attributes.
     *
     * @param decision sampling decision
     * @param attributes attributes. Will not be copied, so do not modify afterwards.
     */
    static Decision create(boolean decision, Attributes attributes) {
      return new AutoValue_Samplers_DecisionImpl(decision, attributes);
    }

    @Override
    public abstract boolean isSampled();

    @Override
    public abstract Attributes getAttributes();
  }
}
