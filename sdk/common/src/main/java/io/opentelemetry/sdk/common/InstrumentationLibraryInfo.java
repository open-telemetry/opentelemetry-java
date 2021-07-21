/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common;

import static java.util.Objects.requireNonNull;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.trace.Tracer;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Holds information about the instrumentation library specified when creating an instance of {@link
 * Tracer} using the Tracer Provider.
 */
@AutoValue
@Immutable
public abstract class InstrumentationLibraryInfo {
  private static final InstrumentationLibraryInfo EMPTY = create("", null);

  /**
   * Creates a new instance of {@link InstrumentationLibraryInfo}.
   *
   * @param name name of the instrumentation library (e.g., "io.opentelemetry.contrib.mongodb"),
   *     must not be null
   * @param version version of the instrumentation library (e.g., "1.0.0"), might be null
   * @return the new instance
   */
  public static InstrumentationLibraryInfo create(String name, @Nullable String version) {
    requireNonNull(name, "name");
    return new AutoValue_InstrumentationLibraryInfo(name, version, null);
  }

  /**
   * Creates a new instance of {@link InstrumentationLibraryInfo}.
   *
   * @param name name of the instrumentation library (e.g., "io.opentelemetry.contrib.mongodb"),
   *     must not be null
   * @param version version of the instrumentation library (e.g., "1.0.0"), might be null
   * @param schemaUrl the URL of the OpenTelemetry schema being used by this instrumentation
   *     library.
   * @return the new instance
   * @since 1.4.0
   */
  public static InstrumentationLibraryInfo create(
      String name, @Nullable String version, @Nullable String schemaUrl) {
    requireNonNull(name, "name");
    return new AutoValue_InstrumentationLibraryInfo(name, version, schemaUrl);
  }

  /**
   * Returns an "empty" {@code InstrumentationLibraryInfo}.
   *
   * @return an "empty" {@code InstrumentationLibraryInfo}.
   */
  public static InstrumentationLibraryInfo empty() {
    return EMPTY;
  }

  /**
   * Returns the name of the instrumentation library.
   *
   * @return the name of the instrumentation library.
   */
  public abstract String getName();

  /**
   * Returns the version of the instrumentation library, or {@code null} if not available.
   *
   * @return the version of the instrumentation library, or {@code null} if not available.
   */
  @Nullable
  public abstract String getVersion();

  /**
   * Returns the URL of the schema used by this instrumentation library, or {@code null} if not
   * available.
   *
   * @return the URL of the schema used by this instrumentation library, or {@code null} if not
   *     available.
   */
  @Nullable
  public abstract String getSchemaUrl();

  // Package protected ctor to avoid others to extend this class.
  InstrumentationLibraryInfo() {}
}
