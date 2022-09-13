/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common;

import static java.util.Objects.requireNonNull;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.common.Attributes;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Holds information about instrumentation scope.
 *
 * <p>Instrumentation scope is a logical unit of the application code with which emitted telemetry
 * is associated. The most common approach is to use the instrumentation library as the scope,
 * however other scopes are also common, e.g. a module, a package, or a class may be chosen as the
 * instrumentation scope.
 */
@AutoValue
@Immutable
public abstract class InstrumentationScopeInfo {
  private static final InstrumentationScopeInfo EMPTY = create("");

  /** Creates a new instance of {@link InstrumentationScopeInfo}. */
  public static InstrumentationScopeInfo create(String name) {
    return InstrumentationScopeInfo.create(name, null, null, Attributes.empty());
  }

  /**
   * Creates a new instance of {@link InstrumentationScopeInfo}.
   *
   * @deprecated Use {@link #builder(String)} or {@link #create(String)}.
   */
  @Deprecated
  public static InstrumentationScopeInfo create(
      String name, @Nullable String version, @Nullable String schemaUrl) {
    return InstrumentationScopeInfo.create(name, version, schemaUrl, Attributes.empty());
  }

  static InstrumentationScopeInfo create(
      String name, @Nullable String version, @Nullable String schemaUrl, Attributes attributes) {
    requireNonNull(name, "name");
    requireNonNull(attributes, "attributes");
    return new AutoValue_InstrumentationScopeInfo(name, version, schemaUrl, attributes);
  }

  /**
   * Returns a {@link InstrumentationScopeInfoBuilder builder} for a {@link
   * InstrumentationScopeInfo}.
   *
   * @since 1.18.0
   */
  public static InstrumentationScopeInfoBuilder builder(String name) {
    return new InstrumentationScopeInfoBuilder(name);
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

  /**
   * Returns the attributes of this instrumentation scope.
   *
   * @since 1.18.0
   */
  public abstract Attributes getAttributes();

  InstrumentationScopeInfo() {}
}
