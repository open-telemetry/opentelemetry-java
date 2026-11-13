/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.view;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.common.internal.GlobUtil;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.InstrumentSelector;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;
import io.opentelemetry.sdk.metrics.View;
import io.opentelemetry.sdk.metrics.ViewBuilder;
import io.opentelemetry.sdk.metrics.export.CardinalityLimitSelector;
import io.opentelemetry.sdk.metrics.export.DefaultAggregationSelector;
import io.opentelemetry.sdk.metrics.internal.aggregator.AggregationUtil;
import io.opentelemetry.sdk.metrics.internal.aggregator.AggregatorFactory;
import io.opentelemetry.sdk.metrics.internal.debug.SourceInfo;
import io.opentelemetry.sdk.metrics.internal.descriptor.Advice;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.state.MetricStorage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Central location for Views to be registered. Registration of a view is done via the {@link
 * SdkMeterProviderBuilder}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
@Immutable
public final class ViewRegistry {
  static final View DEFAULT_VIEW = View.builder().build();
  static final RegisteredView DEFAULT_REGISTERED_VIEW =
      RegisteredView.create(
          InstrumentSelector.builder().setName("*").build(),
          DEFAULT_VIEW,
          AttributesFilters.ALLOW_ALL,
          MetricStorage.DEFAULT_MAX_CARDINALITY,
          SourceInfo.noSourceInfo());
  private static final Logger logger = Logger.getLogger(ViewRegistry.class.getName());

  private final DefaultAggregationSelector defaultAggregationSelector;
  private final CardinalityLimitSelector cardinalityLimitSelector;
  private final ViewMatchingMode viewMatchingMode;
  private final Map<InstrumentType, RegisteredView> instrumentDefaultRegisteredView;
  private final List<RegisteredView> registeredViews;

  ViewRegistry(
      DefaultAggregationSelector defaultAggregationSelector,
      CardinalityLimitSelector cardinalityLimitSelector,
      List<RegisteredView> registeredViews,
      ViewMatchingMode viewMatchingMode) {
    this.defaultAggregationSelector = defaultAggregationSelector;
    this.cardinalityLimitSelector = cardinalityLimitSelector;
    this.viewMatchingMode = viewMatchingMode;
    this.instrumentDefaultRegisteredView = new HashMap<>();
    for (InstrumentType instrumentType : InstrumentType.values()) {
      Aggregation defaultAgg = defaultAggregationSelector.getDefaultAggregation(instrumentType);
      ViewBuilder vb = View.builder();
      Aggregation explicitAgg = null;
      if (!defaultAgg.equals(Aggregation.defaultAggregation())) {
        vb.setAggregation(defaultAgg);
        explicitAgg = defaultAgg;
      }
      int cardinalityLimit = cardinalityLimitSelector.getCardinalityLimit(instrumentType);
      Integer explicitLimit = null;
      if (cardinalityLimit != MetricStorage.DEFAULT_MAX_CARDINALITY) {
        vb.setCardinalityLimit(cardinalityLimit);
        explicitLimit = cardinalityLimit;
      }
      instrumentDefaultRegisteredView.put(
          instrumentType,
          RegisteredView.create(
              InstrumentSelector.builder().setName("*").build(),
              vb.build(),
              AttributesFilters.ALLOW_ALL,
              cardinalityLimit,
              SourceInfo.noSourceInfo(),
              explicitAgg,
              null,
              explicitLimit));
    }
    this.registeredViews = registeredViews;
  }

  ViewRegistry(
      DefaultAggregationSelector defaultAggregationSelector,
      CardinalityLimitSelector cardinalityLimitSelector,
      List<RegisteredView> registeredViews) {
    this(
        defaultAggregationSelector,
        cardinalityLimitSelector,
        registeredViews,
        ViewMatchingMode.INDEPENDENT);
  }

  /** Returns a {@link ViewRegistry}. */
  public static ViewRegistry create(
      DefaultAggregationSelector defaultAggregationSelector,
      CardinalityLimitSelector cardinalityLimitSelector,
      List<RegisteredView> registeredViews,
      ViewMatchingMode viewMatchingMode) {
    return new ViewRegistry(
        defaultAggregationSelector,
        cardinalityLimitSelector,
        new ArrayList<>(registeredViews),
        viewMatchingMode);
  }

  /** Returns a {@link ViewRegistry}. */
  public static ViewRegistry create(
      DefaultAggregationSelector defaultAggregationSelector,
      CardinalityLimitSelector cardinalityLimitSelector,
      List<RegisteredView> registeredViews) {
    return create(
        defaultAggregationSelector,
        cardinalityLimitSelector,
        registeredViews,
        ViewMatchingMode.INDEPENDENT);
  }

  /** Return a {@link ViewRegistry} using the default aggregation and no views registered. */
  public static ViewRegistry create() {
    return create(
        unused -> Aggregation.defaultAggregation(),
        CardinalityLimitSelector.defaultCardinalityLimitSelector(),
        Collections.emptyList(),
        ViewMatchingMode.INDEPENDENT);
  }

  /**
   * Returns the metric {@link View} for a given instrument.
   *
   * @param descriptor description of the instrument.
   * @return The list of {@link View}s for this instrument, or a default view.
   */
  public List<RegisteredView> findViews(
      InstrumentDescriptor descriptor, InstrumentationScopeInfo meterScope) {
    if (viewMatchingMode == ViewMatchingMode.COMPOSABLE) {
      return findComposableViews(descriptor, meterScope);
    }
    return findIndependentViews(descriptor, meterScope);
  }

  private List<RegisteredView> findIndependentViews(
      InstrumentDescriptor descriptor, InstrumentationScopeInfo meterScope) {
    List<RegisteredView> result = new ArrayList<>();
    // Find matching views for the instrument
    for (RegisteredView entry : registeredViews) {
      if (matchesSelector(entry.getInstrumentSelector(), descriptor, meterScope)) {
        AggregatorFactory viewAggregatorFactory =
            (AggregatorFactory) entry.getView().getAggregation();
        if (viewAggregatorFactory.isCompatibleWithInstrument(descriptor)) {
          result.add(entry);
        } else {
          logger.log(
              Level.WARNING,
              "View aggregation "
                  + AggregationUtil.aggregationName(entry.getView().getAggregation())
                  + " is incompatible with instrument "
                  + descriptor.getName()
                  + " of type "
                  + descriptor.getType());
        }
      }
    }

    // If a view matched, return it
    if (!result.isEmpty()) {
      return Collections.unmodifiableList(result);
    }

    return Collections.singletonList(getDefaultRegisteredView(descriptor));
  }

  private List<RegisteredView> findComposableViews(
      InstrumentDescriptor descriptor, InstrumentationScopeInfo meterScope) {
    List<RegisteredView> matchingViews = new ArrayList<>();
    for (RegisteredView entry : registeredViews) {
      if (matchesSelector(entry.getInstrumentSelector(), descriptor, meterScope)) {
        matchingViews.add(entry);
      }
    }

    if (matchingViews.isEmpty()) {
      return Collections.singletonList(getDefaultRegisteredView(descriptor));
    }

    // Collect distinct configured stream names case-insensitively, preserving order of appearance
    // Key: lowercase canonical name; Value: latest configured name casing
    LinkedHashMap<String, String> configuredNames = new LinkedHashMap<>();
    for (RegisteredView entry : matchingViews) {
      String viewName = entry.getView().getName();
      if (viewName != null) {
        configuredNames.put(viewName.toLowerCase(Locale.ROOT), viewName);
      }
    }

    List<RegisteredView> result = new ArrayList<>();

    if (configuredNames.isEmpty()) {
      // No matching View configures a stream name: single group using instrument's default name
      result.add(composeStreamGroup(descriptor, null, matchingViews));
    } else {
      for (Map.Entry<String, String> entry : configuredNames.entrySet()) {
        String canonicalName = entry.getKey();
        String targetName = entry.getValue();
        List<RegisteredView> group = new ArrayList<>();
        for (RegisteredView viewEntry : matchingViews) {
          String viewName = viewEntry.getView().getName();
          if (viewName == null || canonicalName.equals(viewName.toLowerCase(Locale.ROOT))) {
            group.add(viewEntry);
          }
        }
        result.add(composeStreamGroup(descriptor, targetName, group));
      }
    }

    return Collections.unmodifiableList(result);
  }

  private RegisteredView composeStreamGroup(
      InstrumentDescriptor descriptor, @Nullable String targetName, List<RegisteredView> group) {
    String effectiveDescription = resolveDescription(descriptor, group);
    int effectiveCardinalityLimit =
        resolveCardinalityLimit(descriptor, group, cardinalityLimitSelector);
    Aggregation effectiveAggregation =
        resolveAggregation(descriptor, group, defaultAggregationSelector);
    AttributeFilterComposition filterComposition = composeAttributeFilter(descriptor, group);

    RegisteredView lastEntry = group.get(group.size() - 1);
    ViewBuilder viewBuilder = View.builder();
    if (targetName != null) {
      viewBuilder.setName(targetName);
    }
    if (effectiveDescription != null) {
      viewBuilder.setDescription(effectiveDescription);
    }
    viewBuilder.setAggregation(effectiveAggregation);
    if (filterComposition.explicitPredicate != null) {
      viewBuilder.setAttributeFilter(filterComposition.explicitPredicate);
    }
    viewBuilder.setCardinalityLimit(effectiveCardinalityLimit);
    View composedView = viewBuilder.build();

    return RegisteredView.create(
        lastEntry.getInstrumentSelector(),
        composedView,
        filterComposition.attributesFilter,
        effectiveCardinalityLimit,
        lastEntry.getViewSourceInfo(),
        effectiveAggregation,
        filterComposition.explicitPredicate,
        effectiveCardinalityLimit);
  }

  private static String resolveDescription(
      InstrumentDescriptor descriptor, List<RegisteredView> group) {
    String effectiveDescription = descriptor.getDescription();
    for (RegisteredView entry : group) {
      String desc = entry.getView().getDescription();
      if (desc != null) {
        effectiveDescription = desc;
      }
    }
    return effectiveDescription;
  }

  private static int resolveCardinalityLimit(
      InstrumentDescriptor descriptor,
      List<RegisteredView> group,
      CardinalityLimitSelector cardinalityLimitSelector) {
    int effectiveCardinalityLimit =
        cardinalityLimitSelector.getCardinalityLimit(descriptor.getType());
    for (RegisteredView entry : group) {
      Integer limit = entry.getExplicitCardinalityLimit();
      if (limit != null) {
        effectiveCardinalityLimit = limit;
      }
    }
    return effectiveCardinalityLimit;
  }

  private static Aggregation resolveAggregation(
      InstrumentDescriptor descriptor,
      List<RegisteredView> group,
      DefaultAggregationSelector defaultAggregationSelector) {
    Aggregation effectiveAggregation = null;
    for (int i = group.size() - 1; i >= 0; i--) {
      RegisteredView entry = group.get(i);
      Aggregation agg = entry.getExplicitAggregation();
      if (agg != null) {
        AggregatorFactory factory = (AggregatorFactory) agg;
        if (factory.isCompatibleWithInstrument(descriptor)) {
          effectiveAggregation = agg;
          break;
        } else {
          logger.log(
              Level.WARNING,
              "View aggregation "
                  + AggregationUtil.aggregationName(agg)
                  + " is incompatible with instrument "
                  + descriptor.getName()
                  + " of type "
                  + descriptor.getType());
        }
      }
    }

    if (effectiveAggregation == null) {
      Aggregation defaultAgg =
          defaultAggregationSelector.getDefaultAggregation(descriptor.getType());
      AggregatorFactory defaultFactory = (AggregatorFactory) defaultAgg;
      if (!defaultFactory.isCompatibleWithInstrument(descriptor)) {
        logger.log(
            Level.WARNING,
            "Instrument default aggregation "
                + AggregationUtil.aggregationName(defaultAgg)
                + " is incompatible with instrument "
                + descriptor.getName()
                + " of type "
                + descriptor.getType());
        effectiveAggregation = DEFAULT_VIEW.getAggregation();
      } else {
        effectiveAggregation = defaultAgg;
      }
    }
    return effectiveAggregation;
  }

  private static AttributeFilterComposition composeAttributeFilter(
      InstrumentDescriptor descriptor, List<RegisteredView> group) {
    List<Predicate<String>> explicitFilters = new ArrayList<>();
    for (RegisteredView entry : group) {
      Predicate<String> explicitFilter = entry.getExplicitAttributeFilter();
      if (explicitFilter != null) {
        explicitFilters.add(explicitFilter);
      }
    }

    if (!explicitFilters.isEmpty()) {
      Predicate<String> composedPredicate = CompositeAndPredicate.create(explicitFilters);
      return new AttributeFilterComposition(
          composedPredicate, AttributesFilters.byKeyName(composedPredicate));
    }
    if (descriptor.getAdvice().hasAttributes()) {
      return new AttributeFilterComposition(
          null, AttributesFilters.byAdvice(requireNonNull(descriptor.getAdvice().getAttributes())));
    }
    return new AttributeFilterComposition(null, AttributesFilters.ALLOW_ALL);
  }

  private static final class AttributeFilterComposition {
    @Nullable private final Predicate<String> explicitPredicate;
    private final UnaryOperator<Attributes> attributesFilter;

    AttributeFilterComposition(
        @Nullable Predicate<String> explicitPredicate, UnaryOperator<Attributes> attributesFilter) {
      this.explicitPredicate = explicitPredicate;
      this.attributesFilter = attributesFilter;
    }
  }

  private RegisteredView getDefaultRegisteredView(InstrumentDescriptor descriptor) {
    RegisteredView instrumentDefaultView =
        requireNonNull(instrumentDefaultRegisteredView.get(descriptor.getType()));

    AggregatorFactory viewAggregatorFactory =
        (AggregatorFactory) instrumentDefaultView.getView().getAggregation();

    if (!viewAggregatorFactory.isCompatibleWithInstrument(descriptor)) {
      // The aggregation from default aggregation selector was incompatible with instrument, use
      // default aggregation instead
      logger.log(
          Level.WARNING,
          "Instrument default aggregation "
              + AggregationUtil.aggregationName(instrumentDefaultView.getView().getAggregation())
              + " is incompatible with instrument "
              + descriptor.getName()
              + " of type "
              + descriptor.getType());
      instrumentDefaultView = DEFAULT_REGISTERED_VIEW;
    }

    // if the user defined an attributes advice, use it
    if (descriptor.getAdvice().hasAttributes()) {
      instrumentDefaultView =
          applyAdviceToDefaultView(instrumentDefaultView, descriptor.getAdvice());
    }

    return instrumentDefaultView;
  }

  // Matches an instrument selector against an instrument + meter.
  private static boolean matchesSelector(
      InstrumentSelector selector,
      InstrumentDescriptor descriptor,
      InstrumentationScopeInfo meterScope) {
    if (selector.getInstrumentType() != null
        && selector.getInstrumentType() != descriptor.getType()) {
      return false;
    }
    if (selector.getInstrumentUnit() != null
        && !selector.getInstrumentUnit().equals(descriptor.getUnit())) {
      return false;
    }
    if (selector.getInstrumentName() != null
        && !GlobUtil.createGlobPatternPredicate(selector.getInstrumentName())
            .test(descriptor.getName())) {
      return false;
    }
    return matchesMeter(selector, meterScope);
  }

  // Matches a meter selector against a meter.
  private static boolean matchesMeter(
      InstrumentSelector selector, InstrumentationScopeInfo meterScope) {
    if (selector.getMeterName() != null && !selector.getMeterName().equals(meterScope.getName())) {
      return false;
    }
    if (selector.getMeterVersion() != null
        && !selector.getMeterVersion().equals(meterScope.getVersion())) {
      return false;
    }
    return selector.getMeterSchemaUrl() == null
        || selector.getMeterSchemaUrl().equals(meterScope.getSchemaUrl());
  }

  private static RegisteredView applyAdviceToDefaultView(
      RegisteredView instrumentDefaultView, Advice advice) {
    return RegisteredView.create(
        instrumentDefaultView.getInstrumentSelector(),
        instrumentDefaultView.getView(),
        AttributesFilters.byAdvice(requireNonNull(advice.getAttributes())),
        instrumentDefaultView.getCardinalityLimit(),
        instrumentDefaultView.getViewSourceInfo(),
        instrumentDefaultView.getExplicitAggregation(),
        instrumentDefaultView.getExplicitAttributeFilter(),
        instrumentDefaultView.getExplicitCardinalityLimit());
  }
}
