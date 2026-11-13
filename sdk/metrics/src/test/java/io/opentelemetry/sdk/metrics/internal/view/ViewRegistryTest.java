/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.view;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static io.opentelemetry.sdk.metrics.internal.view.ViewRegistry.DEFAULT_REGISTERED_VIEW;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.InstrumentSelector;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.InstrumentValueType;
import io.opentelemetry.sdk.metrics.View;
import io.opentelemetry.sdk.metrics.ViewTestUtil;
import io.opentelemetry.sdk.metrics.export.CardinalityLimitSelector;
import io.opentelemetry.sdk.metrics.export.DefaultAggregationSelector;
import io.opentelemetry.sdk.metrics.internal.descriptor.Advice;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class ViewRegistryTest {

  @RegisterExtension LogCapturer logs = LogCapturer.create().captureForType(ViewRegistry.class);

  private static final InstrumentationScopeInfo INSTRUMENTATION_SCOPE_INFO =
      InstrumentationScopeInfo.builder("name")
          .setVersion("version")
          .setSchemaUrl("schema_url")
          .build();

  private static RegisteredView registeredView(InstrumentSelector instrumentSelector, View view) {
    return ViewTestUtil.createRegisteredView(instrumentSelector, view);
  }

  @Test
  void findViews_SelectionOnType() {
    RegisteredView registeredView =
        registeredView(
            InstrumentSelector.builder().setType(InstrumentType.COUNTER).build(),
            View.builder().setDescription("description").build());
    ViewRegistry viewRegistry =
        ViewRegistry.create(
            DefaultAggregationSelector.getDefault(),
            CardinalityLimitSelector.defaultCardinalityLimitSelector(),
            Collections.singletonList(registeredView));

    assertThat(
            viewRegistry.findViews(
                InstrumentDescriptor.create(
                    "", "", "", InstrumentType.COUNTER, InstrumentValueType.LONG, Advice.empty()),
                INSTRUMENTATION_SCOPE_INFO))
        .isEqualTo(Collections.singletonList(registeredView));
    // this one doesn't match, so it gets the default still.
    assertThat(
            viewRegistry.findViews(
                InstrumentDescriptor.create(
                    "",
                    "",
                    "",
                    InstrumentType.UP_DOWN_COUNTER,
                    InstrumentValueType.LONG,
                    Advice.empty()),
                INSTRUMENTATION_SCOPE_INFO))
        .isEqualTo(Collections.singletonList(DEFAULT_REGISTERED_VIEW));

    assertThat(logs.getEvents()).hasSize(0);
  }

  @Test
  void findViews_SelectionOnUnit() {
    RegisteredView registeredView =
        registeredView(
            InstrumentSelector.builder().setUnit("ms").build(),
            View.builder().setDescription("description").build());
    ViewRegistry viewRegistry =
        ViewRegistry.create(
            DefaultAggregationSelector.getDefault(),
            CardinalityLimitSelector.defaultCardinalityLimitSelector(),
            Collections.singletonList(registeredView));

    assertThat(
            viewRegistry.findViews(
                InstrumentDescriptor.create(
                    "", "", "ms", InstrumentType.COUNTER, InstrumentValueType.LONG, Advice.empty()),
                INSTRUMENTATION_SCOPE_INFO))
        .isEqualTo(Collections.singletonList(registeredView));
    // this one doesn't match, so it gets the default still.
    assertThat(
            viewRegistry.findViews(
                InstrumentDescriptor.create(
                    "",
                    "",
                    "",
                    InstrumentType.UP_DOWN_COUNTER,
                    InstrumentValueType.LONG,
                    Advice.empty()),
                INSTRUMENTATION_SCOPE_INFO))
        .isEqualTo(Collections.singletonList(DEFAULT_REGISTERED_VIEW));

    assertThat(logs.getEvents()).hasSize(0);
  }

  @Test
  void findViews_SelectionOnName() {
    RegisteredView registeredView =
        registeredView(
            InstrumentSelector.builder().setName("overridden").build(),
            View.builder().setDescription("description").build());
    ViewRegistry viewRegistry =
        ViewRegistry.create(
            DefaultAggregationSelector.getDefault(),
            CardinalityLimitSelector.defaultCardinalityLimitSelector(),
            Collections.singletonList(registeredView));

    assertThat(
            viewRegistry.findViews(
                InstrumentDescriptor.create(
                    "overridden",
                    "",
                    "",
                    InstrumentType.COUNTER,
                    InstrumentValueType.LONG,
                    Advice.empty()),
                INSTRUMENTATION_SCOPE_INFO))
        .isEqualTo(Collections.singletonList(registeredView));
    // this one doesn't match, so it gets the default still.
    assertThat(
            viewRegistry.findViews(
                InstrumentDescriptor.create(
                    "default",
                    "",
                    "",
                    InstrumentType.COUNTER,
                    InstrumentValueType.LONG,
                    Advice.empty()),
                INSTRUMENTATION_SCOPE_INFO))
        .isEqualTo(Collections.singletonList(DEFAULT_REGISTERED_VIEW));

    assertThat(logs.getEvents()).hasSize(0);
  }

  @Test
  void findViews_MultipleMatchingViews() {
    RegisteredView registeredView1 =
        registeredView(
            InstrumentSelector.builder().setName("overridden").build(),
            View.builder().setAggregation(Aggregation.explicitBucketHistogram()).build());
    RegisteredView registeredView2 =
        registeredView(
            InstrumentSelector.builder().setName("*").build(),
            View.builder().setAggregation(Aggregation.sum()).build());

    ViewRegistry viewRegistry =
        ViewRegistry.create(
            DefaultAggregationSelector.getDefault(),
            CardinalityLimitSelector.defaultCardinalityLimitSelector(),
            Arrays.asList(registeredView1, registeredView2));

    assertThat(
            viewRegistry.findViews(
                InstrumentDescriptor.create(
                    "overridden",
                    "",
                    "",
                    InstrumentType.COUNTER,
                    InstrumentValueType.LONG,
                    Advice.empty()),
                INSTRUMENTATION_SCOPE_INFO))
        .isEqualTo(Arrays.asList(registeredView1, registeredView2));
    assertThat(
            viewRegistry.findViews(
                InstrumentDescriptor.create(
                    "default",
                    "",
                    "",
                    InstrumentType.COUNTER,
                    InstrumentValueType.LONG,
                    Advice.empty()),
                INSTRUMENTATION_SCOPE_INFO))
        .isEqualTo(Collections.singletonList(registeredView2));

    assertThat(logs.getEvents()).hasSize(0);
  }

  @Test
  void findViews_SelectionTypeAndName() {
    RegisteredView registeredView =
        registeredView(
            InstrumentSelector.builder()
                .setType(InstrumentType.COUNTER)
                .setName("overrides")
                .build(),
            View.builder().setAggregation(Aggregation.explicitBucketHistogram()).build());
    ViewRegistry viewRegistry =
        ViewRegistry.create(
            DefaultAggregationSelector.getDefault(),
            CardinalityLimitSelector.defaultCardinalityLimitSelector(),
            Collections.singletonList(registeredView));

    assertThat(
            viewRegistry.findViews(
                InstrumentDescriptor.create(
                    "overrides",
                    "",
                    "",
                    InstrumentType.COUNTER,
                    InstrumentValueType.LONG,
                    Advice.empty()),
                INSTRUMENTATION_SCOPE_INFO))
        .isEqualTo(Collections.singletonList(registeredView));
    // this one doesn't match, so it gets the default still.
    assertThat(
            viewRegistry.findViews(
                InstrumentDescriptor.create(
                    "overrides",
                    "",
                    "",
                    InstrumentType.UP_DOWN_COUNTER,
                    InstrumentValueType.LONG,
                    Advice.empty()),
                INSTRUMENTATION_SCOPE_INFO))
        .isEqualTo(Collections.singletonList(DEFAULT_REGISTERED_VIEW));
    // this one doesn't match, so it gets the default still.
    assertThat(
            viewRegistry.findViews(
                InstrumentDescriptor.create(
                    "default",
                    "",
                    "",
                    InstrumentType.COUNTER,
                    InstrumentValueType.LONG,
                    Advice.empty()),
                INSTRUMENTATION_SCOPE_INFO))
        .isEqualTo(Collections.singletonList(DEFAULT_REGISTERED_VIEW));

    assertThat(logs.getEvents()).hasSize(0);
  }

  @Test
  @SuppressLogger(ViewRegistry.class)
  void findViews_DefaultAggregationSelector() {
    RegisteredView registeredView =
        registeredView(
            InstrumentSelector.builder().setName("overridden").build(),
            View.builder().setDescription("description").build());
    // Configure a default aggregation selector that defaults to exponential histogram aggregation
    // for histogram and gauge instruments. Note, gauges are incompatible with exponential
    // histograms.
    DefaultAggregationSelector defaultAggregationSelector =
        instrumentType ->
            instrumentType == InstrumentType.HISTOGRAM
                    || instrumentType == InstrumentType.OBSERVABLE_GAUGE
                ? Aggregation.base2ExponentialBucketHistogram()
                : Aggregation.defaultAggregation();

    ViewRegistry viewRegistry =
        ViewRegistry.create(
            defaultAggregationSelector,
            CardinalityLimitSelector.defaultCardinalityLimitSelector(),
            Collections.singletonList(registeredView));

    // Counter instrument should result in default view
    assertThat(
            viewRegistry.findViews(
                InstrumentDescriptor.create(
                    "test",
                    "",
                    "",
                    InstrumentType.COUNTER,
                    InstrumentValueType.DOUBLE,
                    Advice.empty()),
                INSTRUMENTATION_SCOPE_INFO))
        .isEqualTo(Collections.singletonList(DEFAULT_REGISTERED_VIEW));
    // Histogram instrument named overridden should match the registered view, for which the
    // aggregation is explicit bucket histogram
    assertThat(
            viewRegistry.findViews(
                InstrumentDescriptor.create(
                    "overridden",
                    "",
                    "",
                    InstrumentType.HISTOGRAM,
                    InstrumentValueType.DOUBLE,
                    Advice.empty()),
                INSTRUMENTATION_SCOPE_INFO))
        .isEqualTo(Collections.singletonList(registeredView));
    // Histogram instrument named default should match no views, and should receive a default view
    // with the exponential histogram aggregation as dictated by the default aggregation
    // selector
    assertThat(
            viewRegistry.findViews(
                InstrumentDescriptor.create(
                    "default",
                    "",
                    "",
                    InstrumentType.HISTOGRAM,
                    InstrumentValueType.DOUBLE,
                    Advice.empty()),
                INSTRUMENTATION_SCOPE_INFO))
        .isEqualTo(
            Collections.singletonList(
                registeredView(
                    InstrumentSelector.builder().setName("*").build(),
                    View.builder()
                        .setAggregation(Aggregation.base2ExponentialBucketHistogram())
                        .build())));
    // At this point, no warning logs should have been produced.
    assertThat(logs.getEvents()).hasSize(0);
    // Gauge instrument matches no views, and should receive a default view with the exponential
    // histogram aggregation as dictated by the default aggregation selector. However, gauge is
    // incompatible with exponential histogram aggregation and the default view should be returned.
    assertThat(
            viewRegistry.findViews(
                InstrumentDescriptor.create(
                    "default",
                    "",
                    "",
                    InstrumentType.OBSERVABLE_GAUGE,
                    InstrumentValueType.DOUBLE,
                    Advice.empty()),
                INSTRUMENTATION_SCOPE_INFO))
        .isEqualTo(Collections.singletonList(DEFAULT_REGISTERED_VIEW));
    logs.assertContains(
        "Instrument default aggregation base2_exponential_bucket_histogram is incompatible with instrument default of type OBSERVABLE_GAUGE");
  }

  @Test
  @SuppressLogger(ViewRegistry.class)
  void findViews_IncompatibleViewIgnored() {
    RegisteredView registeredView =
        registeredView(
            InstrumentSelector.builder().setType(InstrumentType.OBSERVABLE_COUNTER).build(),
            View.builder().setAggregation(Aggregation.explicitBucketHistogram()).build());
    ViewRegistry viewRegistry =
        ViewRegistry.create(
            DefaultAggregationSelector.getDefault(),
            CardinalityLimitSelector.defaultCardinalityLimitSelector(),
            Collections.singletonList(registeredView));

    assertThat(
            viewRegistry.findViews(
                InstrumentDescriptor.create(
                    "test",
                    "",
                    "",
                    InstrumentType.OBSERVABLE_COUNTER,
                    InstrumentValueType.LONG,
                    Advice.empty()),
                INSTRUMENTATION_SCOPE_INFO))
        .isEqualTo(Collections.singletonList(DEFAULT_REGISTERED_VIEW));

    logs.assertContains(
        "View aggregation explicit_bucket_histogram is incompatible with instrument test of type OBSERVABLE_COUNTER");
  }

  @Test
  void defaults() {
    ViewRegistry viewRegistry = ViewRegistry.create();
    assertThat(
            viewRegistry.findViews(
                InstrumentDescriptor.create(
                    "", "", "", InstrumentType.COUNTER, InstrumentValueType.LONG, Advice.empty()),
                INSTRUMENTATION_SCOPE_INFO))
        .isEqualTo(Collections.singletonList(DEFAULT_REGISTERED_VIEW));
    assertThat(
            viewRegistry.findViews(
                InstrumentDescriptor.create(
                    "",
                    "",
                    "",
                    InstrumentType.UP_DOWN_COUNTER,
                    InstrumentValueType.LONG,
                    Advice.empty()),
                INSTRUMENTATION_SCOPE_INFO))
        .isEqualTo(Collections.singletonList(DEFAULT_REGISTERED_VIEW));
    assertThat(
            viewRegistry.findViews(
                InstrumentDescriptor.create(
                    "", "", "", InstrumentType.HISTOGRAM, InstrumentValueType.LONG, Advice.empty()),
                INSTRUMENTATION_SCOPE_INFO))
        .isEqualTo(Collections.singletonList(DEFAULT_REGISTERED_VIEW));
    assertThat(
            viewRegistry.findViews(
                InstrumentDescriptor.create(
                    "",
                    "",
                    "",
                    InstrumentType.OBSERVABLE_COUNTER,
                    InstrumentValueType.LONG,
                    Advice.empty()),
                INSTRUMENTATION_SCOPE_INFO))
        .isEqualTo(Collections.singletonList(DEFAULT_REGISTERED_VIEW));
    assertThat(
            viewRegistry.findViews(
                InstrumentDescriptor.create(
                    "",
                    "",
                    "",
                    InstrumentType.OBSERVABLE_GAUGE,
                    InstrumentValueType.LONG,
                    Advice.empty()),
                INSTRUMENTATION_SCOPE_INFO))
        .isEqualTo(Collections.singletonList(DEFAULT_REGISTERED_VIEW));
    assertThat(
            viewRegistry.findViews(
                InstrumentDescriptor.create(
                    "",
                    "",
                    "",
                    InstrumentType.OBSERVABLE_UP_DOWN_COUNTER,
                    InstrumentValueType.LONG,
                    Advice.empty()),
                INSTRUMENTATION_SCOPE_INFO))
        .isEqualTo(Collections.singletonList(DEFAULT_REGISTERED_VIEW));

    assertThat(logs.getEvents()).hasSize(0);
  }

  @Test
  @SuppressLogger(ViewRegistry.class)
  void findViews_ApplyAdvice() {
    // use incompatible aggregation for histogram
    DefaultAggregationSelector aggregationSelector =
        instrumentType ->
            instrumentType == InstrumentType.HISTOGRAM
                ? Aggregation.lastValue()
                : Aggregation.defaultAggregation();

    RegisteredView registeredView =
        registeredView(
            InstrumentSelector.builder().setName("test").build(),
            View.builder().setDescription("view applied").build());
    ViewRegistry viewRegistry =
        ViewRegistry.create(
            aggregationSelector,
            CardinalityLimitSelector.defaultCardinalityLimitSelector(),
            Collections.singletonList(registeredView));

    // If a view matches the descriptor, use it and ignore the advice
    assertThat(
            viewRegistry.findViews(
                InstrumentDescriptor.create(
                    "test",
                    "",
                    "",
                    InstrumentType.COUNTER,
                    InstrumentValueType.DOUBLE,
                    Advice.builder()
                        .setAttributes(Arrays.asList(stringKey("key1"), stringKey("key2")))
                        .build()),
                INSTRUMENTATION_SCOPE_INFO))
        .isEqualTo(Collections.singletonList(registeredView));

    // If there is no matching view and attributes advice was defined, use it
    assertThat(
            viewRegistry.findViews(
                InstrumentDescriptor.create(
                    "advice",
                    "",
                    "",
                    InstrumentType.COUNTER,
                    InstrumentValueType.DOUBLE,
                    Advice.builder()
                        .setAttributes(Arrays.asList(stringKey("key1"), stringKey("key2")))
                        .build()),
                INSTRUMENTATION_SCOPE_INFO))
        .hasSize(1)
        .element(0)
        .satisfies(view -> assertAdviceFilter(view, stringKey("key1"), stringKey("key2")));

    // If there is no matching view and attributes advice was defined, use it - incompatible
    // aggregation case
    assertThat(
            viewRegistry.findViews(
                InstrumentDescriptor.create(
                    "histogram_advice",
                    "",
                    "",
                    InstrumentType.HISTOGRAM,
                    InstrumentValueType.DOUBLE,
                    Advice.builder()
                        .setAttributes(Arrays.asList(stringKey("key1"), stringKey("key2")))
                        .build()),
                INSTRUMENTATION_SCOPE_INFO))
        .hasSize(1)
        .element(0)
        .satisfies(view -> assertAdviceFilter(view, stringKey("key1"), stringKey("key2")));

    // if advice is not defined, use the default view
    assertThat(
            viewRegistry.findViews(
                InstrumentDescriptor.create(
                    "advice",
                    "",
                    "",
                    InstrumentType.COUNTER,
                    InstrumentValueType.DOUBLE,
                    Advice.empty()),
                INSTRUMENTATION_SCOPE_INFO))
        .isEqualTo(Collections.singletonList(DEFAULT_REGISTERED_VIEW));
  }

  private static void assertAdviceFilter(RegisteredView view, AttributeKey<?>... expectedKeys) {
    assertThat(view)
        .as("is the same as the default view, except the attributes filter")
        .usingRecursiveComparison()
        .withStrictTypeChecking()
        .ignoringFields("attributesFilter")
        .isEqualTo(DEFAULT_REGISTERED_VIEW);
    // The advice filter uses FilteredAttributes.create under the hood; verify by applying it to a
    // superset of attributes and checking the retained keys.
    AttributesBuilder builder = Attributes.builder();
    for (AttributeKey<?> key : expectedKeys) {
      builder.put(key.getKey(), "v");
    }
    builder.put("extra", "drop");
    Attributes filtered = view.getAttributesFilter().apply(builder.build());
    assertThat(filtered.asMap().keySet())
        .asInstanceOf(InstanceOfAssertFactories.collection(AttributeKey.class))
        .containsExactlyInAnyOrder(expectedKeys);
  }

  @Test
  void findComposableViews_SingleMatchingView() {
    RegisteredView registeredView =
        registeredView(
            InstrumentSelector.builder().setName("my_counter").build(),
            View.builder().setDescription("custom description").build());
    ViewRegistry viewRegistry =
        ViewRegistry.create(
            DefaultAggregationSelector.getDefault(),
            CardinalityLimitSelector.defaultCardinalityLimitSelector(),
            Collections.singletonList(registeredView),
            ViewMatchingMode.COMPOSABLE);

    List<RegisteredView> views =
        viewRegistry.findViews(
            InstrumentDescriptor.create(
                "my_counter",
                "desc",
                "1",
                InstrumentType.COUNTER,
                InstrumentValueType.LONG,
                Advice.empty()),
            INSTRUMENTATION_SCOPE_INFO);

    assertThat(views).hasSize(1);
    assertThat(views.get(0).getView().getDescription()).isEqualTo("custom description");
    assertThat(views.get(0).getView().getAggregation()).isEqualTo(Aggregation.defaultAggregation());
  }

  @Test
  void findComposableViews_LastWinsPrecedence() {
    RegisteredView view1 =
        registeredView(
            InstrumentSelector.builder().setName("my_counter").build(),
            View.builder()
                .setDescription("description 1")
                .setCardinalityLimit(100)
                .setAggregation(Aggregation.sum())
                .build());
    RegisteredView view2 =
        registeredView(
            InstrumentSelector.builder().setName("my_counter").build(),
            View.builder().setDescription("description 2").setCardinalityLimit(500).build());

    ViewRegistry viewRegistry =
        ViewRegistry.create(
            DefaultAggregationSelector.getDefault(),
            CardinalityLimitSelector.defaultCardinalityLimitSelector(),
            Arrays.asList(view1, view2),
            ViewMatchingMode.COMPOSABLE);

    List<RegisteredView> views =
        viewRegistry.findViews(
            InstrumentDescriptor.create(
                "my_counter",
                "desc",
                "1",
                InstrumentType.COUNTER,
                InstrumentValueType.LONG,
                Advice.empty()),
            INSTRUMENTATION_SCOPE_INFO);

    assertThat(views).hasSize(1);
    assertThat(views.get(0).getView().getDescription()).isEqualTo("description 2");
    assertThat(views.get(0).getCardinalityLimit()).isEqualTo(500);
    assertThat(views.get(0).getView().getAggregation()).isEqualTo(Aggregation.sum());
  }

  @Test
  void findComposableViews_AttributeFilterConjunction() {
    RegisteredView policyView =
        registeredView(
            InstrumentSelector.builder().setName("*").build(),
            View.builder()
                .setAttributeFilter(new HashSet<>(Arrays.asList("keep1", "keep2", "keep3")))
                .build());
    RegisteredView serviceView =
        registeredView(
            InstrumentSelector.builder().setName("my_counter").build(),
            View.builder()
                .setAttributeFilter(new HashSet<>(Arrays.asList("keep1", "keep2")))
                .build());

    ViewRegistry viewRegistry =
        ViewRegistry.create(
            DefaultAggregationSelector.getDefault(),
            CardinalityLimitSelector.defaultCardinalityLimitSelector(),
            Arrays.asList(policyView, serviceView),
            ViewMatchingMode.COMPOSABLE);

    List<RegisteredView> views =
        viewRegistry.findViews(
            InstrumentDescriptor.create(
                "my_counter",
                "desc",
                "1",
                InstrumentType.COUNTER,
                InstrumentValueType.LONG,
                Advice.empty()),
            INSTRUMENTATION_SCOPE_INFO);

    assertThat(views).hasSize(1);
    Attributes attributes =
        Attributes.builder()
            .put("keep1", "v1")
            .put("keep2", "v2")
            .put("keep3", "v3")
            .put("drop", "v4")
            .build();
    Attributes filtered = views.get(0).getAttributesFilter().apply(attributes);
    assertThat(filtered.asMap()).containsOnlyKeys(stringKey("keep1"), stringKey("keep2"));
  }

  @Test
  void findComposableViews_AttributeFilterOverridesAdvice() {
    RegisteredView view =
        registeredView(
            InstrumentSelector.builder().setName("my_counter").build(),
            View.builder().setAttributeFilter(Collections.singleton("custom_key")).build());

    ViewRegistry viewRegistry =
        ViewRegistry.create(
            DefaultAggregationSelector.getDefault(),
            CardinalityLimitSelector.defaultCardinalityLimitSelector(),
            Collections.singletonList(view),
            ViewMatchingMode.COMPOSABLE);

    List<RegisteredView> views =
        viewRegistry.findViews(
            InstrumentDescriptor.create(
                "my_counter",
                "desc",
                "1",
                InstrumentType.COUNTER,
                InstrumentValueType.LONG,
                Advice.builder()
                    .setAttributes(Collections.singletonList(stringKey("advice_key")))
                    .build()),
            INSTRUMENTATION_SCOPE_INFO);

    assertThat(views).hasSize(1);
    Attributes attributes =
        Attributes.builder().put("custom_key", "v1").put("advice_key", "v2").build();
    Attributes filtered = views.get(0).getAttributesFilter().apply(attributes);
    assertThat(filtered.asMap()).containsOnlyKeys(stringKey("custom_key"));
  }

  @Test
  void findComposableViews_UnspecifiedAttributeFilterPreservesAdvice() {
    RegisteredView view =
        registeredView(
            InstrumentSelector.builder().setName("my_counter").build(),
            View.builder().setDescription("custom description").build());

    ViewRegistry viewRegistry =
        ViewRegistry.create(
            DefaultAggregationSelector.getDefault(),
            CardinalityLimitSelector.defaultCardinalityLimitSelector(),
            Collections.singletonList(view),
            ViewMatchingMode.COMPOSABLE);

    List<RegisteredView> views =
        viewRegistry.findViews(
            InstrumentDescriptor.create(
                "my_counter",
                "desc",
                "1",
                InstrumentType.COUNTER,
                InstrumentValueType.LONG,
                Advice.builder()
                    .setAttributes(Collections.singletonList(stringKey("advice_key")))
                    .build()),
            INSTRUMENTATION_SCOPE_INFO);

    assertThat(views).hasSize(1);
    Attributes attributes =
        Attributes.builder().put("advice_key", "v1").put("other_key", "v2").build();
    Attributes filtered = views.get(0).getAttributesFilter().apply(attributes);
    assertThat(filtered.asMap()).containsOnlyKeys(stringKey("advice_key"));
  }

  @Test
  void findComposableViews_SemanticIncompatibilityFallback() {
    RegisteredView validView =
        registeredView(
            InstrumentSelector.builder().setName("my_counter").build(),
            View.builder().setAggregation(Aggregation.sum()).build());
    RegisteredView invalidView =
        registeredView(
            InstrumentSelector.builder().setName("my_counter").build(),
            View.builder().setAggregation(Aggregation.lastValue()).build());

    ViewRegistry viewRegistry =
        ViewRegistry.create(
            DefaultAggregationSelector.getDefault(),
            CardinalityLimitSelector.defaultCardinalityLimitSelector(),
            Arrays.asList(validView, invalidView),
            ViewMatchingMode.COMPOSABLE);

    List<RegisteredView> views =
        viewRegistry.findViews(
            InstrumentDescriptor.create(
                "my_counter",
                "desc",
                "1",
                InstrumentType.COUNTER,
                InstrumentValueType.LONG,
                Advice.empty()),
            INSTRUMENTATION_SCOPE_INFO);

    assertThat(views).hasSize(1);
    assertThat(views.get(0).getView().getAggregation()).isEqualTo(Aggregation.sum());
    assertThat(logs.getEvents()).hasSize(1);
    assertThat(logs.getEvents().get(0).getMessage())
        .contains(
            "View aggregation last_value is incompatible with instrument my_counter of type COUNTER");
  }

  @Test
  void findComposableViews_DistinctConfiguredStreamNames() {
    RegisteredView globalPolicy =
        registeredView(
            InstrumentSelector.builder().setName("*").build(),
            View.builder().setAttributeFilter(Collections.singleton("allowed_key")).build());
    RegisteredView streamA =
        registeredView(
            InstrumentSelector.builder().setName("my_histogram").build(),
            View.builder().setName("histogram.stream_a").setAggregation(Aggregation.sum()).build());
    RegisteredView streamB =
        registeredView(
            InstrumentSelector.builder().setName("my_histogram").build(),
            View.builder()
                .setName("histogram.stream_b")
                .setAggregation(Aggregation.explicitBucketHistogram())
                .build());

    ViewRegistry viewRegistry =
        ViewRegistry.create(
            DefaultAggregationSelector.getDefault(),
            CardinalityLimitSelector.defaultCardinalityLimitSelector(),
            Arrays.asList(globalPolicy, streamA, streamB),
            ViewMatchingMode.COMPOSABLE);

    List<RegisteredView> views =
        viewRegistry.findViews(
            InstrumentDescriptor.create(
                "my_histogram",
                "desc",
                "ms",
                InstrumentType.HISTOGRAM,
                InstrumentValueType.DOUBLE,
                Advice.empty()),
            INSTRUMENTATION_SCOPE_INFO);

    assertThat(views).hasSize(2);
    RegisteredView resA =
        views.stream()
            .filter(v -> "histogram.stream_a".equals(v.getView().getName()))
            .findFirst()
            .get();
    RegisteredView resB =
        views.stream()
            .filter(v -> "histogram.stream_b".equals(v.getView().getName()))
            .findFirst()
            .get();

    assertThat(resA.getView().getAggregation()).isEqualTo(Aggregation.sum());
    assertThat(resB.getView().getAggregation()).isEqualTo(Aggregation.explicitBucketHistogram());

    Attributes attrs =
        Attributes.builder().put("allowed_key", "v1").put("dropped_key", "v2").build();
    assertThat(resA.getAttributesFilter().apply(attrs).asMap())
        .containsOnlyKeys(stringKey("allowed_key"));
    assertThat(resB.getAttributesFilter().apply(attrs).asMap())
        .containsOnlyKeys(stringKey("allowed_key"));
  }

  @Test
  void findComposableViews_ExplicitDefaultAggregationResetsPreceding() {
    RegisteredView view1 =
        registeredView(
            InstrumentSelector.builder().setName("my_histogram").build(),
            View.builder().setAggregation(Aggregation.base2ExponentialBucketHistogram()).build());
    RegisteredView view2ExplicitDefault =
        registeredView(
            InstrumentSelector.builder().setName("my_histogram").build(),
            View.builder().setAggregation(Aggregation.defaultAggregation()).build());

    InstrumentDescriptor histogramDesc =
        InstrumentDescriptor.create(
            "my_histogram",
            "desc",
            "ms",
            InstrumentType.HISTOGRAM,
            InstrumentValueType.DOUBLE,
            Advice.empty());

    ViewRegistry registryWithExplicitDefault =
        ViewRegistry.create(
            DefaultAggregationSelector.getDefault(),
            CardinalityLimitSelector.defaultCardinalityLimitSelector(),
            Arrays.asList(view1, view2ExplicitDefault),
            ViewMatchingMode.COMPOSABLE);

    List<RegisteredView> views =
        registryWithExplicitDefault.findViews(histogramDesc, INSTRUMENTATION_SCOPE_INFO);
    assertThat(views).hasSize(1);
    assertThat(views.get(0).getView().getAggregation()).isEqualTo(Aggregation.defaultAggregation());

    RegisteredView view2Unspecified =
        registeredView(
            InstrumentSelector.builder().setName("my_histogram").build(),
            View.builder().setDescription("updated description").build());

    ViewRegistry registryWithUnspecified =
        ViewRegistry.create(
            DefaultAggregationSelector.getDefault(),
            CardinalityLimitSelector.defaultCardinalityLimitSelector(),
            Arrays.asList(view1, view2Unspecified),
            ViewMatchingMode.COMPOSABLE);

    List<RegisteredView> viewsUnspecified =
        registryWithUnspecified.findViews(histogramDesc, INSTRUMENTATION_SCOPE_INFO);
    assertThat(viewsUnspecified).hasSize(1);
    assertThat(viewsUnspecified.get(0).getView().getAggregation())
        .isEqualTo(Aggregation.base2ExponentialBucketHistogram());
    assertThat(viewsUnspecified.get(0).getView().getDescription()).isEqualTo("updated description");
  }

  @Test
  void findComposableViews_ExplicitDefaultCardinalityResetsPreceding() {
    RegisteredView view1 =
        registeredView(
            InstrumentSelector.builder().setName("my_counter").build(),
            View.builder().setCardinalityLimit(100).build());
    RegisteredView view2ExplicitDefault =
        registeredView(
            InstrumentSelector.builder().setName("my_counter").build(),
            View.builder().setCardinalityLimit(2000).build());

    InstrumentDescriptor counterDesc =
        InstrumentDescriptor.create(
            "my_counter",
            "desc",
            "1",
            InstrumentType.COUNTER,
            InstrumentValueType.LONG,
            Advice.empty());

    ViewRegistry registry =
        ViewRegistry.create(
            DefaultAggregationSelector.getDefault(),
            CardinalityLimitSelector.defaultCardinalityLimitSelector(),
            Arrays.asList(view1, view2ExplicitDefault),
            ViewMatchingMode.COMPOSABLE);

    List<RegisteredView> views = registry.findViews(counterDesc, INSTRUMENTATION_SCOPE_INFO);
    assertThat(views).hasSize(1);
    assertThat(views.get(0).getCardinalityLimit()).isEqualTo(2000);

    RegisteredView view2Unspecified =
        registeredView(
            InstrumentSelector.builder().setName("my_counter").build(),
            View.builder().setDescription("desc2").build());

    ViewRegistry registryUnspecified =
        ViewRegistry.create(
            DefaultAggregationSelector.getDefault(),
            CardinalityLimitSelector.defaultCardinalityLimitSelector(),
            Arrays.asList(view1, view2Unspecified),
            ViewMatchingMode.COMPOSABLE);

    List<RegisteredView> viewsUnspecified =
        registryUnspecified.findViews(counterDesc, INSTRUMENTATION_SCOPE_INFO);
    assertThat(viewsUnspecified).hasSize(1);
    assertThat(viewsUnspecified.get(0).getCardinalityLimit()).isEqualTo(100);
  }

  @Test
  void findComposableViews_AllConfiguredAggregationsIncompatible() {
    RegisteredView view1 =
        registeredView(
            InstrumentSelector.builder().setName("my_counter").build(),
            View.builder().setAggregation(Aggregation.lastValue()).build());
    RegisteredView view2 =
        registeredView(
            InstrumentSelector.builder().setName("my_counter").build(),
            View.builder().setAggregation(Aggregation.lastValue()).build());

    ViewRegistry viewRegistry =
        ViewRegistry.create(
            DefaultAggregationSelector.getDefault(),
            CardinalityLimitSelector.defaultCardinalityLimitSelector(),
            Arrays.asList(view1, view2),
            ViewMatchingMode.COMPOSABLE);

    List<RegisteredView> views =
        viewRegistry.findViews(
            InstrumentDescriptor.create(
                "my_counter",
                "desc",
                "1",
                InstrumentType.COUNTER,
                InstrumentValueType.LONG,
                Advice.empty()),
            INSTRUMENTATION_SCOPE_INFO);

    assertThat(views).hasSize(1);
    assertThat(views.get(0).getView().getAggregation()).isEqualTo(Aggregation.defaultAggregation());
    assertThat(logs.getEvents()).hasSize(2);
    assertThat(logs.getEvents().get(0).getMessage())
        .contains(
            "View aggregation last_value is incompatible with instrument my_counter of type COUNTER");
    assertThat(logs.getEvents().get(1).getMessage())
        .contains(
            "View aggregation last_value is incompatible with instrument my_counter of type COUNTER");
  }

  @Test
  void findComposableViews_ThreeOrMoreViewsMerged() {
    RegisteredView view1 =
        registeredView(
            InstrumentSelector.builder().setName("my_counter").build(),
            View.builder().setAttributeFilter(new HashSet<>(Arrays.asList("a", "b", "c"))).build());
    RegisteredView view2 =
        registeredView(
            InstrumentSelector.builder().setName("my_counter").build(),
            View.builder()
                .setAttributeFilter(new HashSet<>(Arrays.asList("b", "c", "d")))
                .setDescription("composed desc")
                .build());
    RegisteredView view3 =
        registeredView(
            InstrumentSelector.builder().setName("my_counter").build(),
            View.builder().setCardinalityLimit(500).setAggregation(Aggregation.sum()).build());

    ViewRegistry viewRegistry =
        ViewRegistry.create(
            DefaultAggregationSelector.getDefault(),
            CardinalityLimitSelector.defaultCardinalityLimitSelector(),
            Arrays.asList(view1, view2, view3),
            ViewMatchingMode.COMPOSABLE);

    List<RegisteredView> views =
        viewRegistry.findViews(
            InstrumentDescriptor.create(
                "my_counter",
                "desc",
                "1",
                InstrumentType.COUNTER,
                InstrumentValueType.LONG,
                Advice.empty()),
            INSTRUMENTATION_SCOPE_INFO);

    assertThat(views).hasSize(1);
    RegisteredView composed = views.get(0);
    assertThat(composed.getView().getDescription()).isEqualTo("composed desc");
    assertThat(composed.getCardinalityLimit()).isEqualTo(500);
    assertThat(composed.getView().getAggregation()).isEqualTo(Aggregation.sum());

    Attributes attrs =
        Attributes.builder()
            .put("a", "1")
            .put("b", "2")
            .put("c", "3")
            .put("d", "4")
            .put("e", "5")
            .build();
    assertThat(composed.getAttributesFilter().apply(attrs).asMap())
        .containsOnlyKeys(stringKey("b"), stringKey("c"));
  }

  @Test
  void findComposableViews_CaseInsensitiveStreamNamesGrouped() {
    RegisteredView view1 =
        registeredView(
            InstrumentSelector.builder().setName("my_counter").build(),
            View.builder().setName("HttpRequests").setDescription("desc1").build());
    RegisteredView view2 =
        registeredView(
            InstrumentSelector.builder().setName("my_counter").build(),
            View.builder().setName("httprequests").setDescription("desc2").build());

    ViewRegistry viewRegistry =
        ViewRegistry.create(
            DefaultAggregationSelector.getDefault(),
            CardinalityLimitSelector.defaultCardinalityLimitSelector(),
            Arrays.asList(view1, view2),
            ViewMatchingMode.COMPOSABLE);

    List<RegisteredView> views =
        viewRegistry.findViews(
            InstrumentDescriptor.create(
                "my_counter",
                "desc",
                "1",
                InstrumentType.COUNTER,
                InstrumentValueType.LONG,
                Advice.empty()),
            INSTRUMENTATION_SCOPE_INFO);

    // Both views differing only in case must merge into 1 stream group
    assertThat(views).hasSize(1);
    // Target stream name uses the casing of the last configured view in the group
    assertThat(views.get(0).getView().getName()).isEqualTo("httprequests");
    assertThat(views.get(0).getView().getDescription()).isEqualTo("desc2");
  }

  @Test
  void findComposableViews_MultipleFiltersEqualAcrossInvocations() {
    RegisteredView view1 =
        registeredView(
            InstrumentSelector.builder().setName("my_counter").build(),
            View.builder().setAttributeFilter(new HashSet<>(Arrays.asList("a", "b"))).build());
    RegisteredView view2 =
        registeredView(
            InstrumentSelector.builder().setName("my_counter").build(),
            View.builder().setAttributeFilter(new HashSet<>(Arrays.asList("b", "c"))).build());

    ViewRegistry viewRegistry =
        ViewRegistry.create(
            DefaultAggregationSelector.getDefault(),
            CardinalityLimitSelector.defaultCardinalityLimitSelector(),
            Arrays.asList(view1, view2),
            ViewMatchingMode.COMPOSABLE);

    InstrumentDescriptor descriptor =
        InstrumentDescriptor.create(
            "my_counter",
            "desc",
            "1",
            InstrumentType.COUNTER,
            InstrumentValueType.LONG,
            Advice.empty());

    List<RegisteredView> views1 = viewRegistry.findViews(descriptor, INSTRUMENTATION_SCOPE_INFO);
    List<RegisteredView> views2 = viewRegistry.findViews(descriptor, INSTRUMENTATION_SCOPE_INFO);

    assertThat(views1).hasSize(1);
    assertThat(views2).hasSize(1);

    View composedView1 = views1.get(0).getView();
    View composedView2 = views2.get(0).getView();

    // Verify value equality and hashCode contract across separate invocations
    assertThat(composedView1).isEqualTo(composedView2).hasSameHashCodeAs(composedView2);
  }

  @Test
  void findComposableViews_UnicodeStreamNamesDoNotCrash() {
    // Turkish capital I with dot above (\u0130) lowercases to \u0069\u0307 in Locale.ROOT (length 2
    // vs 1)
    String unicodeName = "İ";
    RegisteredView view =
        registeredView(
            InstrumentSelector.builder().setName("my_counter").build(),
            View.builder().setName(unicodeName).setDescription("unicode desc").build());

    ViewRegistry viewRegistry =
        ViewRegistry.create(
            DefaultAggregationSelector.getDefault(),
            CardinalityLimitSelector.defaultCardinalityLimitSelector(),
            Collections.singletonList(view),
            ViewMatchingMode.COMPOSABLE);

    List<RegisteredView> views =
        viewRegistry.findViews(
            InstrumentDescriptor.create(
                "my_counter",
                "desc",
                "1",
                InstrumentType.COUNTER,
                InstrumentValueType.LONG,
                Advice.empty()),
            INSTRUMENTATION_SCOPE_INFO);

    assertThat(views).hasSize(1);
    assertThat(views.get(0).getView().getName()).isEqualTo(unicodeName);
    assertThat(views.get(0).getView().getDescription()).isEqualTo("unicode desc");
  }
}
