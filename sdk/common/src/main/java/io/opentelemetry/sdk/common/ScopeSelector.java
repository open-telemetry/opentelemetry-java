/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common;

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.internal.GlobUtil;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

@AutoValue
@Immutable
public abstract class ScopeSelector {

  private final AtomicReference<Predicate<InstrumentationScopeInfo>> selectorPredicate =
      new AtomicReference<>();

  /** Returns a new {@link ScopeSelectorBuilder} for {@link ScopeSelector}. */
  public static ScopeSelectorBuilder builder() {
    return new ScopeSelectorBuilder();
  }

  /**
   * Returns a {@link ScopeSelector} selecting scopes with the {@code scopeName}.
   *
   * <p>Scope name may contain the wildcard characters {@code *} and {@code ?} with the following
   * matching criteria:
   *
   * <ul>
   *   <li>{@code *} matches 0 or more instances of any character
   *   <li>{@code ?} matches exactly one instance of any character
   * </ul>
   */
  public static ScopeSelector named(String scopeName) {
    return builder().setScopeName(scopeName).build();
  }

  static ScopeSelector create(String scopeName, @Nullable String scopeVersion) {
    ScopeSelector selector = new AutoValue_ScopeSelector(scopeName, scopeVersion);
    // Compute and cache the predicate because we need to check if a scope matches any registered
    // scope selector whenever a scope is created
    selector.selectorPredicate.set(matchesScopePredicate(selector));
    return selector;
  }

  ScopeSelector() {}

  /** Determine if the {@code scopeInfo} matches the criteria of this {@link ScopeSelector}. */
  public boolean matchesScope(InstrumentationScopeInfo scopeInfo) {
    return Objects.requireNonNull(selectorPredicate.get()).test(scopeInfo);
  }

  private static Predicate<InstrumentationScopeInfo> matchesScopePredicate(
      ScopeSelector scopeSelector) {
    Predicate<String> scopeNamePredicate =
        GlobUtil.toGlobPatternPredicate(scopeSelector.getScopeName());
    return scopeInfo -> {
      String scopeVersionCriteria = scopeSelector.getScopeVersion();
      return scopeNamePredicate.test(scopeInfo.getName())
          && (scopeVersionCriteria == null || scopeVersionCriteria.equals(scopeInfo.getVersion()));
    };
  }

  /**
   * Returns the selected scope name.
   *
   * <p>Scope name may contain the wildcard characters {@code *} and {@code ?} with the following
   * matching criteria:
   *
   * <ul>
   *   <li>{@code *} matches 0 or more instances of any character
   *   <li>{@code ?} matches exactly one instance of any character
   * </ul>
   */
  public abstract String getScopeName();

  /** Returns the selected scope version, or null of this selects all scope versions. */
  @Nullable
  public abstract String getScopeVersion();

  @Override
  public final String toString() {
    StringJoiner joiner = new StringJoiner(", ", "ScopeSelector{", "}");
    joiner.add("scopeName=" + getScopeName());
    if (getScopeVersion() != null) {
      joiner.add("scopeVersion=" + getScopeVersion());
    }
    return joiner.toString();
  }
}
