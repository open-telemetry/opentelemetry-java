/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.internal;

import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.function.Predicate;
import java.util.stream.Collectors;
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

  @Nullable private final ScopeConfigurator<T> baseScopeConfigurator;
  @Nullable private T defaultScopeConfig;
  private final List<Condition<T>> conditions = new ArrayList<>();

  ScopeConfiguratorBuilder(@Nullable ScopeConfigurator<T> baseScopeConfigurator) {
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
    return new ScopeNameMatcher(GlobUtil.createGlobPatternPredicate(globPattern));
  }

  /**
   * Helper function for exact matching {@link InstrumentationScopeInfo#getName()} against the
   * {@code scopeName}.
   *
   * @see #addCondition(Predicate, Object)
   */
  public static Predicate<InstrumentationScopeInfo> nameEquals(String scopeName) {
    return new ScopeNameMatcher(name -> name.equals(scopeName));
  }

  /** Build a {@link ScopeConfigurator} with the configuration of this builder. */
  public ScopeConfigurator<T> build() {
    return new ScopeConfiguratorImpl<>(baseScopeConfigurator, defaultScopeConfig, conditions);
  }

  private static final class Condition<T> {
    private final Predicate<InstrumentationScopeInfo> scopeMatcher;
    private final T scopeConfig;

    private Condition(Predicate<InstrumentationScopeInfo> scopeMatcher, T scopeConfig) {
      this.scopeMatcher = scopeMatcher;
      this.scopeConfig = scopeConfig;
    }

    @Override
    public String toString() {
      StringJoiner joiner = new StringJoiner(", ", "Condition{", "}");
      joiner.add("scopeMatcher=" + scopeMatcher);
      joiner.add("scopeConfig=" + scopeConfig);
      return joiner.toString();
    }
  }

  private static class ScopeConfiguratorImpl<T> implements ScopeConfigurator<T> {
    @Nullable private final ScopeConfigurator<T> baseScopeConfigurator;
    @Nullable private final T defaultScopeConfig;
    private final List<Condition<T>> conditions;

    private ScopeConfiguratorImpl(
        @Nullable ScopeConfigurator<T> baseScopeConfigurator,
        @Nullable T defaultScopeConfig,
        List<Condition<T>> conditions) {
      this.baseScopeConfigurator = baseScopeConfigurator;
      this.defaultScopeConfig = defaultScopeConfig;
      this.conditions = conditions;
    }

    @Override
    @Nullable
    public T apply(InstrumentationScopeInfo scopeInfo) {
      if (baseScopeConfigurator != null) {
        T scopeConfig = baseScopeConfigurator.apply(scopeInfo);
        if (scopeConfig != null) {
          return scopeConfig;
        }
      }
      for (Condition<T> condition : conditions) {
        if (condition.scopeMatcher.test(scopeInfo)) {
          return condition.scopeConfig;
        }
      }
      return defaultScopeConfig;
    }

    @Override
    public String toString() {
      StringJoiner joiner = new StringJoiner(", ", "ScopeConfiguratorImpl{", "}");
      if (baseScopeConfigurator != null) {
        joiner.add("baseScopeConfigurator=" + baseScopeConfigurator);
      }
      if (defaultScopeConfig != null) {
        joiner.add("defaultScopeConfig=" + defaultScopeConfig);
      }
      joiner.add(
          "conditions="
              + conditions.stream()
                  .map(Objects::toString)
                  .collect(Collectors.joining(",", "[", "]")));
      return joiner.toString();
    }
  }

  private static class ScopeNameMatcher implements Predicate<InstrumentationScopeInfo> {
    private final Predicate<String> nameMatcher;

    private ScopeNameMatcher(Predicate<String> nameMatcher) {
      this.nameMatcher = nameMatcher;
    }

    @Override
    public boolean test(InstrumentationScopeInfo scopeInfo) {
      return nameMatcher.test(scopeInfo.getName());
    }

    @Override
    public String toString() {
      return "ScopeNameMatcher{nameMatcher=" + nameMatcher + "}";
    }
  }
}
