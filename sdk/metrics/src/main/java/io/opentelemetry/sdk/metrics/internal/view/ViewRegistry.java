/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.view;

import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.InstrumentSelector;
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;
import io.opentelemetry.sdk.metrics.View;
import io.opentelemetry.sdk.metrics.internal.aggregator.AggregationUtil;
import io.opentelemetry.sdk.metrics.internal.aggregator.AggregatorFactory;
import io.opentelemetry.sdk.metrics.internal.debug.SourceInfo;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
          // InstrumentSelector requires some selection criteria but the default view is a special
          // case, so we trick it by setting a select all criteria manually.
          InstrumentSelector.builder().setName((unused) -> true).build(),
          DEFAULT_VIEW,
          AttributesProcessor.NOOP,
          SourceInfo.noSourceInfo());
  private static final Logger logger = Logger.getLogger(ViewRegistry.class.getName());

  private final List<RegisteredView> reverseRegistration;

  ViewRegistry(List<RegisteredView> reverseRegistration) {
    this.reverseRegistration = reverseRegistration;
  }

  /** Returns a builder of {@link ViewRegistry}. */
  public static ViewRegistryBuilder builder() {
    return new ViewRegistryBuilder();
  }

  /**
   * Returns the metric {@link View} for a given instrument.
   *
   * @param descriptor description of the instrument.
   * @return The list of {@link View}s for this instrument in registered order, or a default
   *     aggregation view.
   */
  public List<RegisteredView> findViews(
      InstrumentDescriptor descriptor, InstrumentationScopeInfo meterScope) {
    List<RegisteredView> result = new ArrayList<>();
    for (RegisteredView entry : reverseRegistration) {
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

    if (result.isEmpty()) {
      return Collections.singletonList(DEFAULT_REGISTERED_VIEW);
    }
    return Collections.unmodifiableList(result);
  }

  // Matches an instrument selector against an instrument + meter.
  private static boolean matchesSelector(
      InstrumentSelector selector,
      InstrumentDescriptor descriptor,
      InstrumentationScopeInfo meterScope) {
    return (selector.getInstrumentType() == null
            || selector.getInstrumentType() == descriptor.getType())
        && selector.getInstrumentNameFilter().test(descriptor.getName())
        && matchesMeter(selector, meterScope);
  }

  // Matches a meter selector against a meter.
  private static boolean matchesMeter(
      InstrumentSelector selector, InstrumentationScopeInfo meterScope) {
    return selector.getMeterNameFilter().test(meterScope.getName())
        && selector.getMeterVersionFilter().test(meterScope.getVersion())
        && selector.getMeterSchemaUrlFilter().test(meterScope.getSchemaUrl());
  }
}
