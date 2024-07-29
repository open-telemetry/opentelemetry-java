/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.internal;

import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;

/**
 * Builder for {@link ScopeConfigurator}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 *
 * @param <T> The scope configuration object, e.g. {@code TracerConfig}, {@code LoggerConfig},
 *     {@code MeterConfig}.
 */
public final class ScopeConfiguratorBuilder<T> {

  private final ScopeConfigurator<T> baseScopeConfigurator;
  @Nullable private T defaultScopeConfig;
  private final List<Condition<T>> conditions = new ArrayList<>();

  ScopeConfiguratorBuilder(ScopeConfigurator<T> baseScopeConfigurator) {
    this.baseScopeConfigurator = baseScopeConfigurator;
  }

  /**
   * Set the default scope config, which is returned by {@link ScopeConfigurator#apply(Object)} if a
   * {@link InstrumentationScopeInfo} does not match any {@link #addCondition(Predicate, Object)
   * conditions}. If a default is not set, an SDK defined default is used.
   */
  public ScopeConfiguratorBuilder<T> setDefault(T defaultScopeConfig) {
    this.defaultScopeConfig = defaultScopeConfig;
    return this;
  }

  /**
   * Add a condition. Conditions are evaluated in order. The {@code scopeConfig} for the first match
   * is returned by {@link ScopeConfigurator#apply(Object)}.
   *
   * @param scopePredicate predicate that {@link InstrumentationScopeInfo}s are evaluated against
   * @param scopeConfig the scope config to use when this condition is the first matching {@code
   *     scopePredicate}
   * @see #nameMatchesGlob(String)
   * @see #nameEquals(String)
   */
  public ScopeConfiguratorBuilder<T> addCondition(
      Predicate<InstrumentationScopeInfo> scopePredicate, T scopeConfig) {
    conditions.add(new Condition<>(scopePredicate, scopeConfig));
    return this;
  }

  /**
   * Helper function for pattern matching {@link InstrumentationScopeInfo#getName()} against the
   * {@code globPattern}.
   *
   * <p>{@code globPattern} may contain the wildcard characters {@code *} and {@code ?} with the
   * following matching criteria:
   *
   * <ul>
   *   <li>{@code *} matches 0 or more instances of any character
   *   <li>{@code ?} matches exactly one instance of any character
   * </ul>
   *
   * @see #addCondition(Predicate, Object)
   */
  public static Predicate<InstrumentationScopeInfo> nameMatchesGlob(String globPattern) {
    Predicate<String> globPredicate = GlobUtil.toGlobPatternPredicate(globPattern);
    return scopeInfo -> globPredicate.test(scopeInfo.getName());
  }

  /**
   * Helper function for exact matching {@link InstrumentationScopeInfo#getName()} against the
   * {@code scopeName}.
   *
   * @see #addCondition(Predicate, Object)
   */
  public static Predicate<InstrumentationScopeInfo> nameEquals(String scopeName) {
    return scopeInfo -> scopeInfo.getName().equals(scopeName);
  }

  /** Build a {@link ScopeConfigurator} with the configuration of this builder. */
  public ScopeConfigurator<T> build() {
    // TODO: return an instance with toString implementation which self describes rules
    return scopeInfo -> {
      T scopeConfig = baseScopeConfigurator.apply(scopeInfo);
      if (scopeConfig != null) {
        return scopeConfig;
      }
      for (Condition<T> condition : conditions) {
        if (condition.scopeMatcher.test(scopeInfo)) {
          return condition.scopeConfig;
        }
      }
      return defaultScopeConfig;
    };
  }

  private static final class Condition<T> {
    private final Predicate<InstrumentationScopeInfo> scopeMatcher;
    private final T scopeConfig;

    private Condition(Predicate<InstrumentationScopeInfo> scopeMatcher, T scopeConfig) {
      this.scopeMatcher = scopeMatcher;
      this.scopeConfig = scopeConfig;
    }
  }
}
