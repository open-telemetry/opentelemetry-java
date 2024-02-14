/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common;

import io.opentelemetry.sdk.internal.GlobUtil;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Utilities for configuring scopes.
 */
public final class ScopeConfig {

  /**
   * Returns a function which returns {@code matchingConfig} to scopes which match the {@code scopeMatcher}. If a scope does match, returns null, which triggers the default behavior.
   *
   * <p>See {@link #scopeNameEquals(String)}, {@link #scopeNameMatches(String)} for helper functions for {@code scopeMatcher}.
   */
  public static <T> Function<InstrumentationScopeInfo, T> applyToMatching(
      Predicate<InstrumentationScopeInfo> scopeMatcher, T matchingConfig) {
    return scopeInfo -> scopeMatcher.test(scopeInfo) ? matchingConfig : null;
  }

  /**
   * Returns a function which returns {@code matchingConfig} to scopes which match the {@code scopeMatcher}, else returns {@code defaultConfig}. This is useful for overriding the default behavior. For example, you can disable by default and selectively enable select scopes.
   *
   * <p>See {@link #scopeNameEquals(String)}, {@link #scopeNameMatches(String)} for helper functions for {@code scopeMatcher}.
   */
  public static <T> Function<InstrumentationScopeInfo, T> applyToMatching(
      Predicate<InstrumentationScopeInfo> scopeMatcher, T matchingConfig, T defaultConfig) {
    return scopeInfo -> scopeMatcher.test(scopeInfo) ? matchingConfig : defaultConfig;
  }

  /**
   * Returns a predicate which returns {@code true} if the {@link InstrumentationScopeInfo#getName()} is an exact match of {@code targetScopeName}.
   */
  public static Predicate<InstrumentationScopeInfo> scopeNameEquals(String scopeName) {
    return scopeInfo -> scopeInfo.getName().equals(scopeName);
  }

  /**
   * Returns a predicate which returns {@code true} if the {@link InstrumentationScopeInfo#getName()} is a wildcard match of the {@code scopeNameGlobPattern}.
   *
   * <p>{@code scopeNameGlobPattern} name may contain the wildcard characters {@code *} and {@code ?} with the following matching criteria:
   *
   * <ul>
   *   <li>{@code *} matches 0 or more instances of any character
   *   <li>{@code ?} matches exactly one instance of any character
   * </ul>
   */
  public static Predicate<InstrumentationScopeInfo> scopeNameMatches(
      String scopeNameGlobPattern) {
    Predicate<String> globPredicate = GlobUtil.toGlobPatternPredicate(scopeNameGlobPattern);
    return scopeInfo -> globPredicate.test(scopeInfo.getName());
  }

  private ScopeConfig() {}
}
