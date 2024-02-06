/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common;

import static java.util.Objects.requireNonNull;

import javax.annotation.Nullable;

/** Builder for {@link ScopeSelector}. */
public final class ScopeSelectorBuilder {

  private String scopeName = "*";
  @Nullable private String scopeVersion;

  ScopeSelectorBuilder() {}

  /**
   * Select scopes with the given {@code scopeName}.
   *
   * <p>Scope name may contain the wildcard characters {@code *} and {@code ?} with the following
   * matching criteria:
   *
   * <ul>
   *   <li>{@code *} matches 0 or more instances of any character
   *   <li>{@code ?} matches exactly one instance of any character
   * </ul>
   */
  public ScopeSelectorBuilder setScopeName(String scopeName) {
    requireNonNull(scopeName, "scopeName");
    this.scopeName = scopeName;
    return this;
  }

  public ScopeSelectorBuilder setScopeVersion(String scopeVersion) {
    requireNonNull(scopeVersion, "scopeVersion");
    this.scopeVersion = scopeVersion;
    return this;
  }

  /** Returns an {@link ScopeSelector} with the configuration of this builder. */
  public ScopeSelector build() {
    return ScopeSelector.create(scopeName, scopeVersion);
  }
}
