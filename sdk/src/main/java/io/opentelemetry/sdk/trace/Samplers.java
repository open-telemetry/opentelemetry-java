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
import io.opentelemetry.sdk.trace.Sampler.SamplingResult;
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

  private static final SamplingResult EMPTY_RECORDED_AND_SAMPLED_SAMPLING_RESULT =
      SamplingResultImpl.createWithoutAttributes(Decision.RECORD_AND_SAMPLED);
  private static final SamplingResult EMPTY_NOT_SAMPLED_OR_RECORDED_SAMPLING_RESULT =
      SamplingResultImpl.createWithoutAttributes(Decision.NOT_RECORD);
  private static final SamplingResult EMPTY_RECORDED_SAMPLING_RESULT =
      SamplingResultImpl.createWithoutAttributes(Decision.RECORD);

  // No instance of this class.
  private Samplers() {}

  static boolean isRecording(Decision decision) {
    return Decision.RECORD.equals(decision) || Decision.RECORD_AND_SAMPLED.equals(decision);
  }

  static boolean isSampled(Decision decision) {
    return Decision.RECORD_AND_SAMPLED.equals(decision);
  }

  /**
   * Returns a {@link SamplingResult} with the given {@code attributes} and {@link
   * SamplingResult#getDecision()} returning {@code decision}.
   *
   * <p>This is meant for use by custom {@link Sampler} implementations.
   *
   * <p>Using {@link #emptySamplingResult(Decision)} instead of this method is slightly faster and
   * shorter if you don't need attributes.
   *
   * @param decision The decision made on the span.
   * @param attributes The attributes to return from {@link SamplingResult#getAttributes()}. A
   *     different object instance with the same elements may be returned.
   * @return A {@link SamplingResult} with the attributes equivalent to {@code attributes} and the
   *     provided {@code decision}.
   */
  public static SamplingResult samplingResult(Decision decision, Attributes attributes) {
    Objects.requireNonNull(attributes, "attributes");
    return attributes.isEmpty()
        ? emptySamplingResult(decision)
        : SamplingResultImpl.create(decision, attributes);
  }

  /**
   * Returns a {@link SamplingResult} with empty attributes and {@link SamplingResult#getDecision()}
   * returning {@code decision}.
   *
   * <p>This is meant for use by custom {@link Sampler} implementations.
   *
   * <p>Use {@link #samplingResult(Decision, Attributes)} if you need attributes.
   *
   * @param decision The decision made on the span.
   * @return A {@link SamplingResult} with empty attributes and the provided {@code decision}.
   */
  public static SamplingResult emptySamplingResult(Decision decision) {
    switch (decision) {
      case RECORD_AND_SAMPLED:
        return EMPTY_RECORDED_AND_SAMPLED_SAMPLING_RESULT;
      case RECORD:
        return EMPTY_RECORDED_SAMPLING_RESULT;
      case NOT_RECORD:
        return EMPTY_NOT_SAMPLED_OR_RECORDED_SAMPLING_RESULT;
    }
    throw new AssertionError("unrecognised samplingResult");
  }

  /**
   * Returns a {@link Sampler} that always makes a "yes" {@link SamplingResult} for {@link Span}
   * sampling.
   *
   * @return a {@code Sampler} that always makes a "yes" {@link SamplingResult} for {@code Span}
   *     sampling.
   * @since 0.1.0
   */
  public static Sampler alwaysOn() {
    return AlwaysOnSampler.INSTANCE;
  }

  /**
   * Returns a {@link Sampler} that always makes a "no" {@link SamplingResult} for {@link Span}
   * sampling.
   *
   * @return a {@code Sampler} that always makes a "no" {@link SamplingResult} for {@code Span}
   *     sampling.
   * @since 0.1.0
   */
  public static Sampler alwaysOff() {
    return AlwaysOffSampler.INSTANCE;
  }

  /**
   * Returns a {@link Sampler} that always makes the same decision as the parent {@link Span} to
   * whether or not to sample. If there is no parent, the Sampler uses the provided Sampler delegate
   * to determine the sampling decision.
   *
   * @param delegateSampler the {@code Sampler} which is used to make the sampling decisions if the
   *     parent does not exist.
   * @return a {@code Sampler} that follows the parent's sampling decision if one exists, otherwise
   *     following the delegate sampler's decision.
   * @since 0.7.0
   */
  public static Sampler parentOrElse(Sampler delegateSampler) {
    return new ParentOrElse(delegateSampler);
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

    // Returns a "yes" {@link SamplingResult} for {@link Span} sampling.
    @Override
    public SamplingResult shouldSample(
        @Nullable SpanContext parentContext,
        TraceId traceId,
        String name,
        Kind spanKind,
        ReadableAttributes attributes,
        List<Link> parentLinks) {
      return EMPTY_RECORDED_AND_SAMPLED_SAMPLING_RESULT;
    }

    @Override
    public String getDescription() {
      return "AlwaysOnSampler";
    }
  }

  @Immutable
  private enum AlwaysOffSampler implements Sampler {
    INSTANCE;

    // Returns a "no" {@link SamplingResult}T on {@link Span} sampling.
    @Override
    public SamplingResult shouldSample(
        @Nullable SpanContext parentContext,
        TraceId traceId,
        String name,
        Kind spanKind,
        ReadableAttributes attributes,
        List<Link> parentLinks) {
      return EMPTY_NOT_SAMPLED_OR_RECORDED_SAMPLING_RESULT;
    }

    @Override
    public String getDescription() {
      return "AlwaysOffSampler";
    }
  }

  @Immutable
  static class ParentOrElse implements Sampler {
    private final Sampler delegateSampler;

    ParentOrElse(Sampler delegateSampler) {
      this.delegateSampler = delegateSampler;
    }

    // If a parent is set, always follows the same sampling decision as the parent.
    // Otherwise, uses the delegateSampler provided at initialization to make a decision.
    @Override
    public SamplingResult shouldSample(
        @Nullable SpanContext parentContext,
        TraceId traceId,
        String name,
        Kind spanKind,
        ReadableAttributes attributes,
        List<Link> parentLinks) {
      if (parentContext != null) {
        if (parentContext.getTraceFlags().isSampled()) {
          return EMPTY_RECORDED_AND_SAMPLED_SAMPLING_RESULT;
        }
        return EMPTY_NOT_SAMPLED_OR_RECORDED_SAMPLING_RESULT;
      }
      return this.delegateSampler.shouldSample(
          parentContext, traceId, name, spanKind, attributes, parentLinks);
    }

    @Override
    public String getDescription() {
      return String.format("ParentOrElse{%s}", this.delegateSampler.getDescription());
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
          SamplingResultImpl.createWithProbability(Decision.RECORD_AND_SAMPLED, probability),
          SamplingResultImpl.createWithProbability(Decision.NOT_RECORD, probability));
    }

    abstract double getProbability();

    abstract long getIdUpperBound();

    abstract SamplingResult getPositiveSamplingResult();

    abstract SamplingResult getNegativeSamplingResult();

    @Override
    public final SamplingResult shouldSample(
        @Nullable SpanContext parentContext,
        TraceId traceId,
        String name,
        Kind spanKind,
        ReadableAttributes attributes,
        @Nullable List<Link> parentLinks) {
      // If the parent is sampled keep the sampling samplingResult.
      if (parentContext != null && parentContext.getTraceFlags().isSampled()) {
        return EMPTY_RECORDED_AND_SAMPLED_SAMPLING_RESULT;
      }
      if (parentLinks != null) {
        // If any parent link is sampled keep the sampling samplingResult.
        for (Link parentLink : parentLinks) {
          if (parentLink.getContext().getTraceFlags().isSampled()) {
            return EMPTY_RECORDED_AND_SAMPLED_SAMPLING_RESULT;
          }
        }
      }
      // Always sample if we are within probability range. This is true even for child spans (that
      // may have had a different sampling samplingResult made) to allow for different sampling
      // policies,
      // and dynamic increases to sampling probabilities for debugging purposes.
      // Note use of '<' for comparison. This ensures that we never sample for probability == 0.0,
      // while allowing for a (very) small chance of *not* sampling if the id == Long.MAX_VALUE.
      // This is considered a reasonable tradeoff for the simplicity/performance requirements (this
      // code is executed in-line for every Span creation).
      return Math.abs(traceId.getTraceRandomPart()) < getIdUpperBound()
          ? getPositiveSamplingResult()
          : getNegativeSamplingResult();
    }

    @Override
    public final String getDescription() {
      return String.format("ProbabilitySampler{%.6f}", getProbability());
    }
  }

  @Immutable
  @AutoValue
  abstract static class SamplingResultImpl implements SamplingResult {
    /**
     * Creates sampling result with probability attribute.
     *
     * @param decision the decision on sampling and recording
     * @param probability the probability that was used for the samplingResult.
     */
    static SamplingResult createWithProbability(Decision decision, double probability) {
      return new AutoValue_Samplers_SamplingResultImpl(
          decision, Attributes.of(SAMPLING_PROBABILITY.key(), doubleAttributeValue(probability)));
    }

    /**
     * Creates sampling result without attributes.
     *
     * @param decision sampling samplingResult
     */
    static SamplingResult createWithoutAttributes(Decision decision) {
      return new AutoValue_Samplers_SamplingResultImpl(decision, Attributes.empty());
    }

    /**
     * Creates sampling result with the given attributes.
     *
     * @param decision sampling decisionq
     * @param attributes attributes. Will not be copied, so do not modify afterwards.
     */
    static SamplingResult create(Decision decision, Attributes attributes) {
      return new AutoValue_Samplers_SamplingResultImpl(decision, attributes);
    }

    @Override
    public abstract Decision getDecision();

    @Override
    public abstract Attributes getAttributes();
  }
}
