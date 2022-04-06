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
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    if (selector.getInstrumentType() != null
        && selector.getInstrumentType() != descriptor.getType()) {
      return false;
    }
    if (selector.getInstrumentName() != null
        && !toGlobPatternPredicate(selector.getInstrumentName()).test(descriptor.getName())) {
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

  /**
   * Return a predicate that returns {@code true} if a string matches the {@code globPattern}.
   *
   * <p>{@code globPattern} may contain the wildcard characters {@code *} and {@code ?} with the
   * following matching criteria:
   *
   * <ul>
   *   <li>{@code *} matches 0 or more instances of any character
   *   <li>{@code ?} matches exactly one instance of any character
   * </ul>
   */
  // Visible for testing
  static Predicate<String> toGlobPatternPredicate(String globPattern) {
    // Match all
    if (globPattern.equals("*")) {
      return unused -> true;
    }

    // If globPattern contains '*' or '?', convert it to a regex and return corresponding predicate
    for (int i = 0; i < globPattern.length(); i++) {
      char c = globPattern.charAt(i);
      if (c == '*' || c == '?') {
        Pattern pattern = toRegexPattern(globPattern);
        return string -> pattern.matcher(string).matches();
      }
    }

    // Exact match, ignoring case
    return globPattern::equalsIgnoreCase;
  }

  /**
   * Transform the {@code globPattern} to a regex by converting {@code *} to {@code .*}, {@code ?}
   * to {@code .}, and escaping other regex special characters.
   */
  private static Pattern toRegexPattern(String globPattern) {
    int tokenStart = -1;
    StringBuilder patternBuilder = new StringBuilder();
    for (int i = 0; i < globPattern.length(); i++) {
      char c = globPattern.charAt(i);
      if (c == '*' || c == '?') {
        if (tokenStart != -1) {
          patternBuilder.append(Pattern.quote(globPattern.substring(tokenStart, i)));
          tokenStart = -1;
        }
        if (c == '*') {
          patternBuilder.append(".*");
        } else {
          // c == '?'
          patternBuilder.append(".");
        }
      } else {
        if (tokenStart == -1) {
          tokenStart = i;
        }
      }
    }
    if (tokenStart != -1) {
      patternBuilder.append(Pattern.quote(globPattern.substring(tokenStart)));
    }
    return Pattern.compile(patternBuilder.toString());
  }
}
