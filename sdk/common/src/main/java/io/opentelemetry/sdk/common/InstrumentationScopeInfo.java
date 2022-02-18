/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common;

import static java.util.Objects.requireNonNull;

import com.google.auto.value.AutoValue;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Holds information about instrumentation scope.
 *
 * <p>Instrumentation scope is a logical unit of the application code with which emitted telemetry
 * is associated. The most common approach is to use the instrumentation library as the scope,
 * however other scopes are also common, e.g. a module, a package, or a class can be chosen as the
 * instrumentation scope.
 */
@AutoValue
@Immutable
public abstract class InstrumentationScopeInfo {
  private static final InstrumentationScopeInfo EMPTY = create("");

  /** Creates a new instance of {@link InstrumentationScopeInfo}. */
  public static InstrumentationScopeInfo create(String name) {
    requireNonNull(name, "name");
    return new AutoValue_InstrumentationScopeInfo(name, null, null);
  }

  /** Creates a new instance of {@link InstrumentationScopeInfo}. */
  public static InstrumentationScopeInfo create(
      String name, @Nullable String version, @Nullable String schemaUrl) {
    requireNonNull(name, "name");
    return new AutoValue_InstrumentationScopeInfo(name, version, schemaUrl);
  }

  /** Returns an "empty" {@link InstrumentationScopeInfo}. */
  public static InstrumentationScopeInfo empty() {
    return EMPTY;
  }

  /** Returns the name of the instrumentation scope. */
  public abstract String getName();

  /** Returns the version of the instrumentation scope, or {@code null} if not available. */
  @Nullable
  public abstract String getVersion();

  /**
   * Returns the URL of the schema used by this instrumentation scope, or {@code null} if not
   * available.
   */
  @Nullable
  public abstract String getSchemaUrl();

  InstrumentationScopeInfo() {}
}
