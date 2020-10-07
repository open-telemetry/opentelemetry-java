/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import static io.opentelemetry.common.AttributeKey.doubleKey;

import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;
import io.opentelemetry.common.AttributeKey;
import io.opentelemetry.common.Attributes;
import io.opentelemetry.common.ReadableAttributes;
import io.opentelemetry.sdk.trace.Sampler.Decision;
import io.opentelemetry.sdk.trace.Sampler.SamplingResult;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Span.Kind;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.TraceId;
import java.util.List;
import java.util.Objects;
import javax.annotation.concurrent.Immutable;

/** Static class to access a set of pre-defined {@link Sampler Samplers}. */
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
  static final AttributeKey<Double> SAMPLING_PROBABILITY = doubleKey("sampling.probability");

  private static final SamplingResult EMPTY_RECORDED_AND_SAMPLED_SAMPLING_RESULT =
      SamplingResultImpl.createWithoutAttributes(Decision.RECORD_AND_SAMPLE);
  private static final SamplingResult EMPTY_NOT_SAMPLED_OR_RECORDED_SAMPLING_RESULT =
      SamplingResultImpl.createWithoutAttributes(Decision.DROP);
  private static final SamplingResult EMPTY_RECORDED_SAMPLING_RESULT =
      SamplingResultImpl.createWithoutAttributes(Decision.RECORD_ONLY);

  // No instance of this class.
  private Samplers() {}

  static boolean isRecording(Decision decision) {
    return Decision.RECORD_ONLY.equals(decision) || Decision.RECORD_AND_SAMPLE.equals(decision);
  }

  static boolean isSampled(Decision decision) {
    return Decision.RECORD_AND_SAMPLE.equals(decision);
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
      case RECORD_AND_SAMPLE:
        return EMPTY_RECORDED_AND_SAMPLED_SAMPLING_RESULT;
      case RECORD_ONLY:
        return EMPTY_RECORDED_SAMPLING_RESULT;
      case DROP:
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
   */
  public static Sampler alwaysOff() {
    return AlwaysOffSampler.INSTANCE;
  }

  /**
   * Returns a {@link Sampler} that always makes the same decision as the parent {@link Span} to
   * whether or not to sample. If there is no parent, the Sampler uses the provided Sampler delegate
   * to determine the sampling decision.
   *
   * @param root the {@code Sampler} which is used to make the sampling decisions if the parent does
   *     not exist.
   * @return a {@code Sampler} that follows the parent's sampling decision if one exists, otherwise
   *     following the root sampler's decision.
   */
  public static Sampler parentBased(Sampler root) {
    return parentBasedBuilder(root).build();
  }

  /**
   * Returns a {@link ParentBased.Builder} that follows the parent's sampling decision if one
   * exists, otherwise following the root sampler and other optional sampler's decision.
   *
   * @param root the required {@code Sampler} which is used to make the sampling decisions if the
   *     parent does not exist.
   * @return a {@code ParentBased.Builder}
   */
  public static ParentBased.Builder parentBasedBuilder(Sampler root) {
    return new ParentBased.Builder(root);
  }

  /**
   * Returns a new TraceIdRatioBased {@link Sampler}. The ratio of sampling a trace is equal to that
   * of the specified ratio.
   *
   * @param ratio The desired ratio of sampling. Must be within [0.0, 1.0].
   * @return a new TraceIdRatioBased {@link Sampler}.
   * @throws IllegalArgumentException if {@code ratio} is out of range
   */
  public static Sampler traceIdRatioBased(double ratio) {
    return TraceIdRatioBased.create(ratio);
  }

  @Immutable
  private enum AlwaysOnSampler implements Sampler {
    INSTANCE;

    // Returns a "yes" {@link SamplingResult} for {@link Span} sampling.
    @Override
    public SamplingResult shouldSample(
        SpanContext parentContext,
        String traceId,
        String name,
        Kind spanKind,
        ReadableAttributes attributes,
        List<SpanData.Link> parentLinks) {
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
        SpanContext parentContext,
        String traceId,
        String name,
        Kind spanKind,
        ReadableAttributes attributes,
        List<SpanData.Link> parentLinks) {
      return EMPTY_NOT_SAMPLED_OR_RECORDED_SAMPLING_RESULT;
    }

    @Override
    public String getDescription() {
      return "AlwaysOffSampler";
    }
  }

  @Immutable
  static class ParentBased implements Sampler {
    private final Sampler root;
    private final Sampler remoteParentSampled;
    private final Sampler remoteParentNotSampled;
    private final Sampler localParentSampled;
    private final Sampler localParentNotSampled;

    private ParentBased(
        Sampler root,
        Sampler remoteParentSampled,
        Sampler remoteParentNotSampled,
        Sampler localParentSampled,
        Sampler localParentNotSampled) {
      this.root = root;
      this.remoteParentSampled = remoteParentSampled == null ? alwaysOn() : remoteParentSampled;
      this.remoteParentNotSampled =
          remoteParentNotSampled == null ? alwaysOff() : remoteParentNotSampled;
      this.localParentSampled = localParentSampled == null ? alwaysOn() : localParentSampled;
      this.localParentNotSampled =
          localParentNotSampled == null ? alwaysOff() : localParentNotSampled;
    }

    // If a parent is set, always follows the same sampling decision as the parent.
    // Otherwise, uses the delegateSampler provided at initialization to make a decision.
    @Override
    public SamplingResult shouldSample(
        SpanContext parentContext,
        String traceId,
        String name,
        Kind spanKind,
        ReadableAttributes attributes,
        List<SpanData.Link> parentLinks) {
      if (!parentContext.isValid()) {
        return this.root.shouldSample(
            parentContext, traceId, name, spanKind, attributes, parentLinks);
      }

      if (parentContext.isRemote()) {
        return parentContext.isSampled()
            ? this.remoteParentSampled.shouldSample(
                parentContext, traceId, name, spanKind, attributes, parentLinks)
            : this.remoteParentNotSampled.shouldSample(
                parentContext, traceId, name, spanKind, attributes, parentLinks);
      }
      return parentContext.isSampled()
          ? this.localParentSampled.shouldSample(
              parentContext, traceId, name, spanKind, attributes, parentLinks)
          : this.localParentNotSampled.shouldSample(
              parentContext, traceId, name, spanKind, attributes, parentLinks);
    }

    @Override
    public String getDescription() {
      return String.format(
          "ParentBased{root:%s,remoteParentSampled:%s,remoteParentNotSampled:%s,"
              + "localParentSampled:%s,localParentNotSampled:%s}",
          this.root.getDescription(),
          this.remoteParentSampled.getDescription(),
          this.remoteParentNotSampled.getDescription(),
          this.localParentSampled.getDescription(),
          this.localParentNotSampled.getDescription());
    }

    static class Builder {
      private final Sampler root;
      private Sampler remoteParentSampled;
      private Sampler remoteParentNotSampled;
      private Sampler localParentSampled;
      private Sampler localParentNotSampled;

      /**
       * Sets the {@link Sampler} to use when there is a remote parent that was sampled. If not set,
       * defaults to always sampling if the remote parent was sampled.
       *
       * @return this Builder
       */
      public Builder setRemoteParentSampled(Sampler remoteParentSampled) {
        this.remoteParentSampled = remoteParentSampled;
        return this;
      }

      /**
       * Sets the {@link Sampler} to use when there is a remote parent that was not sampled. If not
       * set, defaults to never sampling when the remote parent isn't sampled.
       *
       * @return this Builder
       */
      public Builder setRemoteParentNotSampled(Sampler remoteParentNotSampled) {
        this.remoteParentNotSampled = remoteParentNotSampled;
        return this;
      }

      /**
       * Sets the {@link Sampler} to use when there is a local parent that was sampled. If not set,
       * defaults to always sampling if the local parent was sampled.
       *
       * @return this Builder
       */
      public Builder setLocalParentSampled(Sampler localParentSampled) {
        this.localParentSampled = localParentSampled;
        return this;
      }

      /**
       * Sets the {@link Sampler} to use when there is a local parent that was not sampled. If not
       * set, defaults to never sampling when the local parent isn't sampled.
       *
       * @return this Builder
       */
      public Builder setLocalParentNotSampled(Sampler localParentNotSampled) {
        this.localParentNotSampled = localParentNotSampled;
        return this;
      }

      /**
       * Builds the {@link ParentBased}.
       *
       * @return the ParentBased sampler.
       */
      public ParentBased build() {
        return new ParentBased(
            this.root,
            this.remoteParentSampled,
            this.remoteParentNotSampled,
            this.localParentSampled,
            this.localParentNotSampled);
      }

      private Builder(Sampler root) {
        this.root = root;
      }
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof ParentBased)) {
        return false;
      }

      ParentBased that = (ParentBased) o;

      if (root != null ? !root.equals(that.root) : that.root != null) {
        return false;
      }
      if (remoteParentSampled != null
          ? !remoteParentSampled.equals(that.remoteParentSampled)
          : that.remoteParentSampled != null) {
        return false;
      }
      if (remoteParentNotSampled != null
          ? !remoteParentNotSampled.equals(that.remoteParentNotSampled)
          : that.remoteParentNotSampled != null) {
        return false;
      }
      if (localParentSampled != null
          ? !localParentSampled.equals(that.localParentSampled)
          : that.localParentSampled != null) {
        return false;
      }
      return localParentNotSampled != null
          ? localParentNotSampled.equals(that.localParentNotSampled)
          : that.localParentNotSampled == null;
    }

    @Override
    public int hashCode() {
      int result = root != null ? root.hashCode() : 0;
      result = 31 * result + (remoteParentSampled != null ? remoteParentSampled.hashCode() : 0);
      result =
          31 * result + (remoteParentNotSampled != null ? remoteParentNotSampled.hashCode() : 0);
      result = 31 * result + (localParentSampled != null ? localParentSampled.hashCode() : 0);
      result = 31 * result + (localParentNotSampled != null ? localParentNotSampled.hashCode() : 0);
      return result;
    }

    @Override
    public String toString() {
      return "ParentBased{"
          + "root="
          + root
          + ", remoteParentSampled="
          + remoteParentSampled
          + ", remoteParentNotSampled="
          + remoteParentNotSampled
          + ", localParentSampled="
          + localParentSampled
          + ", localParentNotSampled="
          + localParentNotSampled
          + '}';
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
  abstract static class TraceIdRatioBased implements Sampler {

    TraceIdRatioBased() {}

    static TraceIdRatioBased create(double ratio) {
      Preconditions.checkArgument(
          ratio >= 0.0 && ratio <= 1.0, "ratio must be in range [0.0, 1.0]");
      long idUpperBound;
      // Special case the limits, to avoid any possible issues with lack of precision across
      // double/long boundaries. For probability == 0.0, we use Long.MIN_VALUE as this guarantees
      // that we will never sample a trace, even in the case where the id == Long.MIN_VALUE, since
      // Math.Abs(Long.MIN_VALUE) == Long.MIN_VALUE.
      if (ratio == 0.0) {
        idUpperBound = Long.MIN_VALUE;
      } else if (ratio == 1.0) {
        idUpperBound = Long.MAX_VALUE;
      } else {
        idUpperBound = (long) (ratio * Long.MAX_VALUE);
      }
      return new AutoValue_Samplers_TraceIdRatioBased(
          ratio,
          idUpperBound,
          SamplingResultImpl.createWithProbability(Decision.RECORD_AND_SAMPLE, ratio),
          SamplingResultImpl.createWithProbability(Decision.DROP, ratio));
    }

    abstract double getRatio();

    abstract long getIdUpperBound();

    abstract SamplingResult getPositiveSamplingResult();

    abstract SamplingResult getNegativeSamplingResult();

    @Override
    public final SamplingResult shouldSample(
        SpanContext parentContext,
        String traceId,
        String name,
        Kind spanKind,
        ReadableAttributes attributes,
        List<SpanData.Link> parentLinks) {
      // Always sample if we are within probability range. This is true even for child spans (that
      // may have had a different sampling samplingResult made) to allow for different sampling
      // policies,
      // and dynamic increases to sampling probabilities for debugging purposes.
      // Note use of '<' for comparison. This ensures that we never sample for probability == 0.0,
      // while allowing for a (very) small chance of *not* sampling if the id == Long.MAX_VALUE.
      // This is considered a reasonable tradeoff for the simplicity/performance requirements (this
      // code is executed in-line for every Span creation).
      return Math.abs(TraceId.getTraceIdRandomPart(traceId)) < getIdUpperBound()
          ? getPositiveSamplingResult()
          : getNegativeSamplingResult();
    }

    @Override
    public final String getDescription() {
      return String.format("TraceIdRatioBased{%.6f}", getRatio());
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
          decision, Attributes.of(SAMPLING_PROBABILITY, probability));
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
