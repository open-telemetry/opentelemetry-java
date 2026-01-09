/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.view;

import static io.opentelemetry.sdk.metrics.internal.view.NoopAttributesProcessor.NOOP;
import static java.util.Objects.requireNonNull;

import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.internal.GlobUtil;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.InstrumentSelector;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;
import io.opentelemetry.sdk.metrics.View;
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
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
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
          NOOP,
          MetricStorage.DEFAULT_MAX_CARDINALITY,
          SourceInfo.noSourceInfo());
  private static final Logger logger = Logger.getLogger(ViewRegistry.class.getName());

  private final Map<InstrumentType, RegisteredView> instrumentDefaultRegisteredView;
  private final List<RegisteredView> registeredViews;

  ViewRegistry(
      DefaultAggregationSelector defaultAggregationSelector,
      CardinalityLimitSelector cardinalityLimitSelector,
      List<RegisteredView> registeredViews) {
    instrumentDefaultRegisteredView = new HashMap<>();
    for (InstrumentType instrumentType : InstrumentType.values()) {
      instrumentDefaultRegisteredView.put(
          instrumentType,
          RegisteredView.create(
              InstrumentSelector.builder().setName("*").build(),
              View.builder()
                  .setAggregation(defaultAggregationSelector.getDefaultAggregation(instrumentType))
                  .build(),
              AttributesProcessor.noop(),
              cardinalityLimitSelector.getCardinalityLimit(instrumentType),
              SourceInfo.noSourceInfo()));
    }
    this.registeredViews = registeredViews;
  }

  /** Returns a {@link ViewRegistry}. */
  public static ViewRegistry create(
      DefaultAggregationSelector defaultAggregationSelector,
      CardinalityLimitSelector cardinalityLimitSelector,
      List<RegisteredView> registeredViews) {
    return new ViewRegistry(
        defaultAggregationSelector, cardinalityLimitSelector, new ArrayList<>(registeredViews));
  }

  /** Return a {@link ViewRegistry} using the default aggregation and no views registered. */
  public static ViewRegistry create() {
    return create(
        unused -> Aggregation.defaultAggregation(),
        CardinalityLimitSelector.defaultCardinalityLimitSelector(),
        Collections.emptyList());
  }

  /**
   * Returns the metric {@link View} for a given instrument.
   *
   * @param descriptor description of the instrument.
   * @return The list of {@link View}s for this instrument, or a default view.
   */
  public List<RegisteredView> findViews(
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

    // No views matched, use default view
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

    return Collections.singletonList(instrumentDefaultView);
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
        new AdviceAttributesProcessor(requireNonNull(advice.getAttributes())),
        instrumentDefaultView.getCardinalityLimit(),
        instrumentDefaultView.getViewSourceInfo());
  }
}
