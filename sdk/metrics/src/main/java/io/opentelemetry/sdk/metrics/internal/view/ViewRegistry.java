/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.view;

import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.view.InstrumentSelector;
import io.opentelemetry.sdk.metrics.view.MeterSelector;
import io.opentelemetry.sdk.metrics.view.View;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
  public List<View> findViews(InstrumentDescriptor descriptor, InstrumentationLibraryInfo meter) {
    List<View> result = new ArrayList<>();
    for (RegisteredView entry : reverseRegistration) {
      if (matchesSelector(entry.getInstrumentSelector(), descriptor, meter)) {
        result.add(entry.getView());
      }
    }
    if (result.isEmpty()) {
      return Collections.singletonList(DEFAULT_VIEW);
    }
    return Collections.unmodifiableList(result);
  }

  // Matches an instrument selector against an instrument + meter.
  private static boolean matchesSelector(
      InstrumentSelector selector,
      InstrumentDescriptor descriptor,
      InstrumentationLibraryInfo meter) {
    return (selector.getInstrumentType() == null
            || selector.getInstrumentType() == descriptor.getType())
        && selector.getInstrumentNameFilter().test(descriptor.getName())
        && matchesMeter(selector.getMeterSelector(), meter);
  }

  // Matches a meter selector against a meter.
  private static boolean matchesMeter(MeterSelector selector, InstrumentationLibraryInfo meter) {
    return selector.getNameFilter().test(meter.getName())
        && selector.getVersionFilter().test(meter.getVersion())
        && selector.getSchemaUrlFilter().test(meter.getSchemaUrl());
  }
}
