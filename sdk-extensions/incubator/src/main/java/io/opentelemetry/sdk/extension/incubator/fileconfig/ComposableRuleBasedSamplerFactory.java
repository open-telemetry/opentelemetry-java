/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static io.opentelemetry.sdk.extension.incubator.fileconfig.FileConfigUtil.requireNonNull;
import static java.util.stream.Collectors.toSet;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalComposableRuleBasedSamplerModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalComposableRuleBasedSamplerRuleAttributePatternsModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalComposableRuleBasedSamplerRuleAttributeValuesModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalComposableRuleBasedSamplerRuleModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalSpanParent;
import io.opentelemetry.sdk.extension.incubator.trace.samplers.ComposableRuleBasedSamplerBuilder;
import io.opentelemetry.sdk.extension.incubator.trace.samplers.ComposableSampler;
import io.opentelemetry.sdk.extension.incubator.trace.samplers.SamplingPredicate;
import io.opentelemetry.sdk.internal.IncludeExcludePredicate;
import io.opentelemetry.sdk.trace.data.LinkData;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Predicate;
import javax.annotation.Nullable;

final class ComposableRuleBasedSamplerFactory
    implements Factory<ExperimentalComposableRuleBasedSamplerModel, ComposableSampler> {

  private static final ComposableRuleBasedSamplerFactory INSTANCE =
      new ComposableRuleBasedSamplerFactory();

  private ComposableRuleBasedSamplerFactory() {}

  static ComposableRuleBasedSamplerFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public ComposableSampler create(
      ExperimentalComposableRuleBasedSamplerModel model, DeclarativeConfigContext context) {
    ComposableRuleBasedSamplerBuilder builder = ComposableSampler.ruleBasedBuilder();

    List<ExperimentalComposableRuleBasedSamplerRuleModel> rules = model.getRules();
    if (rules != null) {
      rules.forEach(
          rule -> {
            AttributeMatcher valueMatcher = attributeValuesMatcher(rule.getAttributeValues());
            AttributeMatcher patternMatcher = attributePatternsMatcher(rule.getAttributePatterns());
            // TODO: should be null when omitted but is empty
            Set<ExperimentalSpanParent> matchingParents =
                rule.getParent() != null && !rule.getParent().isEmpty()
                    ? new HashSet<>(rule.getParent())
                    : null;
            // TODO: should be null when omitted but is empty
            Set<SpanKind> matchingSpanKinds =
                rule.getSpanKinds() != null && !rule.getSpanKinds().isEmpty()
                    ? rule.getSpanKinds().stream()
                        .map(DeclarativeConfigSamplingPredicate::toSpanKind)
                        .collect(toSet())
                    : null;

            SamplingPredicate predicate =
                new DeclarativeConfigSamplingPredicate(
                    valueMatcher, patternMatcher, matchingParents, matchingSpanKinds);
            ComposableSampler sampler =
                ComposableSamplerFactory.getInstance()
                    .create(requireNonNull(rule.getSampler(), "rule sampler"), context);
            builder.add(predicate, sampler);
          });
    }

    return builder.build();
  }

  @Nullable
  private static AttributeMatcher attributeValuesMatcher(
      @Nullable
          ExperimentalComposableRuleBasedSamplerRuleAttributeValuesModel attributeValuesModel) {
    if (attributeValuesModel == null) {
      return null;
    }
    return new AttributeMatcher(
        requireNonNull(attributeValuesModel.getKey(), "attribute_values key"),
        IncludeExcludePredicate.createExactMatching(attributeValuesModel.getValues(), null));
  }

  @Nullable
  private static AttributeMatcher attributePatternsMatcher(
      @Nullable
          ExperimentalComposableRuleBasedSamplerRuleAttributePatternsModel attributePatternsModel) {
    if (attributePatternsModel == null) {
      return null;
    }
    return new AttributeMatcher(
        requireNonNull(attributePatternsModel.getKey(), "attribute_patterns key"),
        IncludeExcludePredicate.createPatternMatching(
            attributePatternsModel.getIncluded(), attributePatternsModel.getExcluded()));
  }

  // Visible for testing
  static final class DeclarativeConfigSamplingPredicate implements SamplingPredicate {
    @Nullable private final AttributeMatcher attributeValuesMatcher;
    @Nullable private final AttributeMatcher attributePatternsMatcher;
    @Nullable private final Set<ExperimentalSpanParent> matchingParents;
    @Nullable private final Set<SpanKind> matchingSpanKinds;

    DeclarativeConfigSamplingPredicate(
        @Nullable AttributeMatcher attributeValuesMatcher,
        @Nullable AttributeMatcher attributePatternsMatcher,
        @Nullable Set<ExperimentalSpanParent> matchingParents,
        @Nullable Set<SpanKind> matchingSpanKinds) {
      this.attributeValuesMatcher = attributeValuesMatcher;
      this.attributePatternsMatcher = attributePatternsMatcher;
      this.matchingParents = matchingParents;
      this.matchingSpanKinds = matchingSpanKinds;
    }

    @Override
    public boolean matches(
        Context parentContext,
        String traceId,
        String name,
        SpanKind spanKind,
        Attributes attributes,
        List<LinkData> parentLinks) {
      // all conditions must match

      // check attribute value condition
      if (attributeValuesMatcher != null && !attributesMatch(attributeValuesMatcher, attributes)) {
        return false;
      }

      // check attribute pattern condition
      if (attributePatternsMatcher != null
          && !attributesMatch(attributePatternsMatcher, attributes)) {
        return false;
      }

      // check parent condition
      if (matchingParents != null
          && !matchingParents.contains(
              toSpanParent(Span.fromContext(parentContext).getSpanContext()))) {
        return false;
      }

      // check span kind conditions
      if (matchingSpanKinds != null && !matchingSpanKinds.contains(spanKind)) {
        return false;
      }

      // If no conditions are specified, match all spans
      return true;
    }

    private static boolean attributesMatch(AttributeMatcher matcher, Attributes attributes) {
      boolean[] match = new boolean[] {false};
      attributes.forEach(
          (key, value) -> {
            if (matcher.matchesKey(key.getKey()) && matcher.matchesValue(String.valueOf(value))) {
              match[0] = true;
            }
          });
      return match[0];
    }

    @Override
    public String toString() {
      StringJoiner joiner = new StringJoiner(", ", "DeclarativeConfigSamplingPredicate{", "}");
      joiner.add("attributeValuesMatcher=" + attributeValuesMatcher);
      joiner.add("attributePatterns=" + attributePatternsMatcher);
      joiner.add("matchingParents=" + matchingParents);
      joiner.add("matchingSpanKinds=" + matchingSpanKinds);
      return joiner.toString();
    }

    private static SpanKind toSpanKind(
        io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SpanKind spanKind) {
      switch (spanKind) {
        case INTERNAL:
          return SpanKind.INTERNAL;
        case SERVER:
          return SpanKind.SERVER;
        case CLIENT:
          return SpanKind.CLIENT;
        case PRODUCER:
          return SpanKind.PRODUCER;
        case CONSUMER:
          return SpanKind.CONSUMER;
      }
      throw new IllegalArgumentException("Unrecognized span kind: " + spanKind);
    }

    // Visible for testing
    static ExperimentalSpanParent toSpanParent(SpanContext parentSpanContext) {
      if (!parentSpanContext.isValid()) {
        return ExperimentalSpanParent.NONE;
      }
      if (parentSpanContext.isRemote()) {
        return ExperimentalSpanParent.REMOTE;
      }
      return ExperimentalSpanParent.LOCAL;
    }
  }

  // Visible for testing
  static class AttributeMatcher {
    private final Predicate<String> attributeKeyMatcher;
    private final Predicate<String> attributeValueMatcher;

    AttributeMatcher(String attributeKey, Predicate<String> attributeValueMatcher) {
      this(new EqualsPredicate(attributeKey), attributeValueMatcher);
    }

    AttributeMatcher(
        Predicate<String> attributeKeyMatcher, Predicate<String> attributeValueMatcher) {
      this.attributeKeyMatcher = attributeKeyMatcher;
      this.attributeValueMatcher = attributeValueMatcher;
    }

    boolean matchesKey(String attributeKey) {
      return attributeKeyMatcher.test(attributeKey);
    }

    boolean matchesValue(String attributeValue) {
      return attributeValueMatcher.test(attributeValue);
    }

    @Override
    public String toString() {
      return "AttributeMatcher{keyMatcher="
          + attributeKeyMatcher
          + ", valueMatcher="
          + attributeValueMatcher
          + '}';
    }
  }

  private static class EqualsPredicate implements Predicate<String> {
    private final String value;

    private EqualsPredicate(String value) {
      this.value = value;
    }

    @Override
    public boolean test(String s) {
      return value.equals(s);
    }

    @Override
    public String toString() {
      return "EqualsPredicate{" + value + '}';
    }
  }
}
