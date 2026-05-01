/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig;

import static io.opentelemetry.api.trace.SpanKind.CLIENT;
import static io.opentelemetry.api.trace.SpanKind.CONSUMER;
import static io.opentelemetry.api.trace.SpanKind.INTERNAL;
import static io.opentelemetry.api.trace.SpanKind.PRODUCER;
import static io.opentelemetry.api.trace.SpanKind.SERVER;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.ComposableRuleBasedSamplerFactory.DeclarativeConfigSamplingPredicate.toSpanParent;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.config.DeclarativeConfigException;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.common.ComponentLoader;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.ComposableRuleBasedSamplerFactory.AttributeMatcher;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.ComposableRuleBasedSamplerFactory.DeclarativeConfigSamplingPredicate;
import io.opentelemetry.sdk.common.internal.IncludeExcludePredicate;
import io.opentelemetry.sdk.declarativeconfig.internal.model.ExperimentalComposableAlwaysOffSamplerModel;
import io.opentelemetry.sdk.declarativeconfig.internal.model.ExperimentalComposableAlwaysOnSamplerModel;
import io.opentelemetry.sdk.declarativeconfig.internal.model.ExperimentalComposableProbabilitySamplerModel;
import io.opentelemetry.sdk.declarativeconfig.internal.model.ExperimentalComposableRuleBasedSamplerModel;
import io.opentelemetry.sdk.declarativeconfig.internal.model.ExperimentalComposableRuleBasedSamplerRuleAttributePatternsModel;
import io.opentelemetry.sdk.declarativeconfig.internal.model.ExperimentalComposableRuleBasedSamplerRuleAttributeValuesModel;
import io.opentelemetry.sdk.declarativeconfig.internal.model.ExperimentalComposableRuleBasedSamplerRuleModel;
import io.opentelemetry.sdk.declarativeconfig.internal.model.ExperimentalComposableSamplerModel;
import io.opentelemetry.sdk.declarativeconfig.internal.model.ExperimentalSpanParent;
import io.opentelemetry.sdk.declarativeconfig.internal.model.SpanKind;
import io.opentelemetry.sdk.extension.incubator.trace.samplers.ComposableSampler;
import io.opentelemetry.sdk.trace.IdGenerator;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ComposableRuleBasedSamplerFactoryTest {

  private final DeclarativeConfigContext context =
      new DeclarativeConfigContext(ComponentLoader.forClassLoader(getClass().getClassLoader()));

  @ParameterizedTest
  @MethodSource("createTestCases")
  void create(ExperimentalComposableRuleBasedSamplerModel model, ComposableSampler expectedResult) {
    ComposableSampler composableSampler =
        ComposableRuleBasedSamplerFactory.getInstance().create(model, context);
    assertThat(composableSampler.toString()).isEqualTo(expectedResult.toString());
  }

  @ParameterizedTest
  @MethodSource("createInvalidTestCases")
  void createInvalid(ExperimentalComposableRuleBasedSamplerModel model, String expectedMessage) {
    assertThatThrownBy(() -> ComposableRuleBasedSamplerFactory.getInstance().create(model, context))
        .isInstanceOf(DeclarativeConfigException.class)
        .hasMessage(expectedMessage);
  }

  private static Stream<Arguments> createInvalidTestCases() {
    return Stream.of(
        Arguments.of(
            new ExperimentalComposableRuleBasedSamplerModel()
                .withRules(
                    Collections.singletonList(
                        new ExperimentalComposableRuleBasedSamplerRuleModel()
                            .withAttributePatterns(
                                new ExperimentalComposableRuleBasedSamplerRuleAttributePatternsModel()
                                    .withKey("http.path")
                                    .withIncluded(Collections.emptyList())
                                    .withExcluded(null))
                            .withSampler(
                                new ExperimentalComposableSamplerModel()
                                    .withAlwaysOn(
                                        new ExperimentalComposableAlwaysOnSamplerModel())))),
            "included must not be empty"),
        Arguments.of(
            new ExperimentalComposableRuleBasedSamplerModel()
                .withRules(
                    Collections.singletonList(
                        new ExperimentalComposableRuleBasedSamplerRuleModel()
                            .withAttributePatterns(
                                new ExperimentalComposableRuleBasedSamplerRuleAttributePatternsModel()
                                    .withKey("http.path")
                                    .withIncluded(null)
                                    .withExcluded(Collections.emptyList()))
                            .withSampler(
                                new ExperimentalComposableSamplerModel()
                                    .withAlwaysOn(
                                        new ExperimentalComposableAlwaysOnSamplerModel())))),
            "excluded must not be empty"),
        Arguments.of(
            new ExperimentalComposableRuleBasedSamplerModel()
                .withRules(
                    Collections.singletonList(
                        new ExperimentalComposableRuleBasedSamplerRuleModel()
                            .withAttributeValues(
                                new ExperimentalComposableRuleBasedSamplerRuleAttributeValuesModel()
                                    .withKey("http.route")
                                    .withValues(null))
                            .withSampler(
                                new ExperimentalComposableSamplerModel()
                                    .withAlwaysOn(
                                        new ExperimentalComposableAlwaysOnSamplerModel())))),
            ".values is required and must be non-empty"),
        Arguments.of(
            new ExperimentalComposableRuleBasedSamplerModel()
                .withRules(
                    Collections.singletonList(
                        new ExperimentalComposableRuleBasedSamplerRuleModel()
                            .withAttributeValues(
                                new ExperimentalComposableRuleBasedSamplerRuleAttributeValuesModel()
                                    .withKey("http.route")
                                    .withValues(Collections.emptyList()))
                            .withSampler(
                                new ExperimentalComposableSamplerModel()
                                    .withAlwaysOn(
                                        new ExperimentalComposableAlwaysOnSamplerModel())))),
            ".values is required and must be non-empty"));
  }

  private static Stream<Arguments> createTestCases() {
    return Stream.of(
        Arguments.of(
            new ExperimentalComposableRuleBasedSamplerModel(),
            ComposableSampler.ruleBasedBuilder().build()),
        // attributePatterns with only included (no excluded) - analogous to
        // https://github.com/open-telemetry/opentelemetry-java/issues/8337
        Arguments.of(
            new ExperimentalComposableRuleBasedSamplerModel()
                .withRules(
                    Collections.singletonList(
                        new ExperimentalComposableRuleBasedSamplerRuleModel()
                            .withAttributePatterns(
                                new ExperimentalComposableRuleBasedSamplerRuleAttributePatternsModel()
                                    .withKey("http.path")
                                    .withIncluded(Collections.singletonList("/internal/*")))
                            .withSampler(
                                new ExperimentalComposableSamplerModel()
                                    .withAlwaysOn(
                                        new ExperimentalComposableAlwaysOnSamplerModel())))),
            ComposableSampler.ruleBasedBuilder()
                .add(
                    new DeclarativeConfigSamplingPredicate(
                        null,
                        new AttributeMatcher(
                            "http.path",
                            IncludeExcludePredicate.createPatternMatching(
                                Collections.singletonList("/internal/*"), null)),
                        null,
                        null),
                    ComposableSampler.alwaysOn())
                .build()),
        // Recreate example
        Arguments.of(
            new ExperimentalComposableRuleBasedSamplerModel()
                .withRules(
                    Arrays.asList(
                        new ExperimentalComposableRuleBasedSamplerRuleModel()
                            .withAttributeValues(
                                new ExperimentalComposableRuleBasedSamplerRuleAttributeValuesModel()
                                    .withKey("http.route")
                                    .withValues(Arrays.asList("/healthz", "/livez")))
                            .withSampler(
                                new ExperimentalComposableSamplerModel()
                                    .withAlwaysOff(
                                        new ExperimentalComposableAlwaysOffSamplerModel())),
                        new ExperimentalComposableRuleBasedSamplerRuleModel()
                            .withAttributePatterns(
                                new ExperimentalComposableRuleBasedSamplerRuleAttributePatternsModel()
                                    .withKey("http.path")
                                    .withIncluded(Collections.singletonList("/internal/*"))
                                    .withExcluded(Collections.singletonList("/internal/special/*")))
                            .withSampler(
                                new ExperimentalComposableSamplerModel()
                                    .withAlwaysOn(
                                        new ExperimentalComposableAlwaysOnSamplerModel())),
                        new ExperimentalComposableRuleBasedSamplerRuleModel()
                            .withParent(Collections.singletonList(ExperimentalSpanParent.NONE))
                            .withSpanKinds(Collections.singletonList(SpanKind.CLIENT))
                            .withSampler(
                                new ExperimentalComposableSamplerModel()
                                    .withProbability(
                                        new ExperimentalComposableProbabilitySamplerModel()
                                            .withRatio(0.05))),
                        new ExperimentalComposableRuleBasedSamplerRuleModel()
                            .withSampler(
                                new ExperimentalComposableSamplerModel()
                                    .withProbability(
                                        new ExperimentalComposableProbabilitySamplerModel()
                                            .withRatio(0.05))))),
            ComposableSampler.ruleBasedBuilder()
                .add(
                    new DeclarativeConfigSamplingPredicate(
                        new AttributeMatcher(
                            "http.route",
                            IncludeExcludePredicate.createExactMatching(
                                Arrays.asList("/healthz", "/livez"), null)),
                        null,
                        null,
                        null),
                    ComposableSampler.alwaysOff())
                .add(
                    new DeclarativeConfigSamplingPredicate(
                        null,
                        new AttributeMatcher(
                            "http.path",
                            IncludeExcludePredicate.createPatternMatching(
                                Collections.singletonList("/internal/*"),
                                Collections.singletonList("/internal/special/*"))),
                        null,
                        null),
                    ComposableSampler.alwaysOn())
                .add(
                    new DeclarativeConfigSamplingPredicate(
                        null,
                        null,
                        Collections.singleton(ExperimentalSpanParent.NONE),
                        Collections.singleton(CLIENT)),
                    ComposableSampler.probability(0.05))
                .add(
                    new DeclarativeConfigSamplingPredicate(null, null, null, null),
                    ComposableSampler.probability(0.05))
                .build()));
  }

  private static final Context noParent = Context.current();
  private static final Context localParent =
      Context.root()
          .with(
              Span.wrap(
                  SpanContext.create(
                      IdGenerator.random().generateTraceId(),
                      IdGenerator.random().generateSpanId(),
                      TraceFlags.getDefault(),
                      TraceState.getDefault())));
  private static final Context remoteParent =
      Context.root()
          .with(
              Span.wrap(
                  SpanContext.createFromRemoteParent(
                      IdGenerator.random().generateTraceId(),
                      IdGenerator.random().generateSpanId(),
                      TraceFlags.getDefault(),
                      TraceState.getDefault())));
  private static final String tid = IdGenerator.random().generateTraceId();
  private static final String sn = "name";
  private static final io.opentelemetry.api.trace.SpanKind sk = CLIENT;
  private static final AttributeKey<String> HTTP_ROUTE = AttributeKey.stringKey("http.route");
  private static final AttributeKey<String> HTTP_PATH = AttributeKey.stringKey("http.path");

  @ParameterizedTest
  @MethodSource("declarativeConfigSamplingPredicateArgs")
  void declarativeConfigSamplingPredicate(
      DeclarativeConfigSamplingPredicate predicate,
      Context context,
      io.opentelemetry.api.trace.SpanKind spanKind,
      Attributes attributes,
      boolean expectedResult) {
    assertThat(predicate.matches(context, tid, sn, spanKind, attributes, emptyList()))
        .isEqualTo(expectedResult);
  }

  @SuppressWarnings("unused")
  private static Stream<Arguments> declarativeConfigSamplingPredicateArgs() {
    DeclarativeConfigSamplingPredicate matchAll =
        new DeclarativeConfigSamplingPredicate(null, null, null, null);
    DeclarativeConfigSamplingPredicate valuesMatcher =
        new DeclarativeConfigSamplingPredicate(
            new AttributeMatcher(
                "http.route",
                IncludeExcludePredicate.createExactMatching(
                    Arrays.asList("/healthz", "/livez"), null)),
            null,
            null,
            null);
    DeclarativeConfigSamplingPredicate patternsMatcher =
        new DeclarativeConfigSamplingPredicate(
            null,
            new AttributeMatcher(
                "http.path",
                IncludeExcludePredicate.createPatternMatching(
                    Collections.singletonList("/internal/*"),
                    Collections.singletonList("/internal/special/*"))),
            null,
            null);
    DeclarativeConfigSamplingPredicate parentMatcher =
        new DeclarativeConfigSamplingPredicate(
            null, null, Collections.singleton(ExperimentalSpanParent.NONE), null);
    DeclarativeConfigSamplingPredicate spanKindMatcher =
        new DeclarativeConfigSamplingPredicate(null, null, null, Collections.singleton(CLIENT));
    DeclarativeConfigSamplingPredicate multiMatcher =
        new DeclarativeConfigSamplingPredicate(
            new AttributeMatcher(
                "http.route",
                IncludeExcludePredicate.createExactMatching(
                    Arrays.asList("/healthz", "/livez"), null)),
            new AttributeMatcher(
                "http.path",
                IncludeExcludePredicate.createPatternMatching(
                    Collections.singletonList("/internal/*"),
                    Collections.singletonList("/internal/special/*"))),
            Collections.singleton(ExperimentalSpanParent.NONE),
            Collections.singleton(CLIENT));

    return Stream.of(
        // match all
        Arguments.of(matchAll, noParent, sk, Attributes.empty(), true),
        Arguments.of(matchAll, noParent, sk, Attributes.of(HTTP_ROUTE, "/healthz"), true),
        Arguments.of(
            matchAll, noParent, sk, Attributes.of(HTTP_PATH, "/internal/admin/users"), true),
        Arguments.of(matchAll, noParent, SERVER, Attributes.empty(), true),
        Arguments.of(matchAll, remoteParent, sk, Attributes.empty(), true),
        // value matcher
        Arguments.of(valuesMatcher, noParent, sk, Attributes.of(HTTP_ROUTE, "/healthz"), true),
        Arguments.of(valuesMatcher, noParent, sk, Attributes.of(HTTP_ROUTE, "/livez"), true),
        Arguments.of(valuesMatcher, noParent, sk, Attributes.of(HTTP_ROUTE, "/foo"), false),
        Arguments.of(valuesMatcher, noParent, sk, Attributes.empty(), false),
        // pattern matcher
        Arguments.of(
            patternsMatcher, noParent, sk, Attributes.of(HTTP_PATH, "/internal/admin/users"), true),
        Arguments.of(
            patternsMatcher,
            noParent,
            sk,
            Attributes.of(HTTP_PATH, "/internal/management/config"),
            true),
        Arguments.of(
            patternsMatcher, noParent, sk, Attributes.of(HTTP_PATH, "/users/profile/123"), false),
        Arguments.of(
            patternsMatcher,
            noParent,
            sk,
            Attributes.of(HTTP_PATH, "/internal/special/foo"),
            false),
        // parent matcher
        Arguments.of(parentMatcher, noParent, sk, Attributes.empty(), true),
        Arguments.of(parentMatcher, localParent, sk, Attributes.empty(), false),
        Arguments.of(parentMatcher, remoteParent, sk, Attributes.empty(), false),
        // span kind matcher
        Arguments.of(spanKindMatcher, noParent, CLIENT, Attributes.empty(), true),
        Arguments.of(spanKindMatcher, noParent, SERVER, Attributes.empty(), false),
        Arguments.of(spanKindMatcher, noParent, INTERNAL, Attributes.empty(), false),
        Arguments.of(spanKindMatcher, noParent, PRODUCER, Attributes.empty(), false),
        Arguments.of(spanKindMatcher, noParent, CONSUMER, Attributes.empty(), false),
        // multi matcher
        Arguments.of(
            multiMatcher,
            noParent,
            CLIENT,
            Attributes.of(HTTP_ROUTE, "/livez", HTTP_PATH, "/internal/admin/users"),
            true),
        Arguments.of(multiMatcher, noParent, CLIENT, Attributes.of(HTTP_ROUTE, "/livez"), false),
        Arguments.of(
            multiMatcher,
            noParent,
            CLIENT,
            Attributes.of(HTTP_PATH, "/internal/admin/users"),
            false),
        Arguments.of(
            multiMatcher,
            noParent,
            SERVER,
            Attributes.of(HTTP_ROUTE, "/livez", HTTP_PATH, "/internal/admin/users"),
            false),
        Arguments.of(
            multiMatcher,
            localParent,
            CLIENT,
            Attributes.of(HTTP_ROUTE, "/livez", HTTP_PATH, "/internal/admin/users"),
            false));
  }

  @Test
  void toSpanParent_Valid() {
    assertThat(toSpanParent(SpanContext.getInvalid())).isEqualTo(ExperimentalSpanParent.NONE);
    assertThat(toSpanParent(Span.fromContext(localParent).getSpanContext()))
        .isEqualTo(ExperimentalSpanParent.LOCAL);
    assertThat(toSpanParent(Span.fromContext(remoteParent).getSpanContext()))
        .isEqualTo(ExperimentalSpanParent.REMOTE);
  }
}
