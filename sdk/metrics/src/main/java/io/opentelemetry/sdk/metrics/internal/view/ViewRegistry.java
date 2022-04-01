/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.view;

import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.InstrumentSelector;
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;
import io.opentelemetry.sdk.metrics.View;
import io.opentelemetry.sdk.metrics.internal.debug.SourceInfo;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
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
          AttributesProcessor.NOOP,
          SourceInfo.noSourceInfo());
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
        result.add(entry);
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
    if (selector.getInstrumentType() != null
        && selector.getInstrumentType() != descriptor.getType()) {
      return false;
    }
    if (selector.getInstrumentName() != null
        && !matchesName(selector.getInstrumentName(), descriptor.getName())) {
      return false;
    }
    return matchesMeter(selector, meterScope);
  }

  /**
   * Determine if the {@code instrumentName} matches the {@code namePattern} from {@link
   * InstrumentSelector#getInstrumentName()}.
   *
   * <p>{@code namePattern} may contain the wildcard characters {@code *} and {@code ?} with the
   * following matching criteria:
   *
   * <ul>
   *   <li>{@code *} matches 0 or more instances of any character
   *   <li>{@code ?} matches exactly one instance of any character
   * </ul>
   *
   * <p>Based off implementation from: https://www.rgagnon.com/javadetails/java-0515.html
   */
  // Visible for testing
  static boolean matchesName(String namePattern, String instrumentName) {
    if (!namePattern.contains("*") && !namePattern.contains("?")) {
      return namePattern.equals(instrumentName);
    }

    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < namePattern.length(); i++) {
      char c = namePattern.charAt(i);
      switch (c) {
        case '*':
          builder.append(".*");
          break;
        case '?':
          builder.append('.');
          break;
        case '(':
        case ')':
        case '[':
        case ']':
        case '$':
        case '^':
        case '.':
        case '{':
        case '}':
        case '|':
          builder.append("\\").append(c);
          break;
        default:
          builder.append(c);
          break;
      }
    }
    String regex = builder.append('$').toString();
    return Pattern.matches(regex, instrumentName);
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
}
