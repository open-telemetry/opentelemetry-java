/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.sdk.metrics.internal.view.StringPredicates;
import java.util.function.Predicate;

/**
 * Builder for {@link MeterSelector}.
 *
 * @deprecated Use {@link InstrumentSelectorBuilder}.
 */
@Deprecated
public final class MeterSelectorBuilder {

  private Predicate<String> nameFilter = StringPredicates.ALL;
  private Predicate<String> versionFilter = StringPredicates.ALL;
  private Predicate<String> schemaUrlFilter = StringPredicates.ALL;

  /** Sets a specifier for selecting Instruments by name. */
  public MeterSelectorBuilder setName(String name) {
    requireNonNull(name, "name");
    return setName(StringPredicates.exact(name));
  }

  /** Sets the {@link Predicate} for matching name. */
  public MeterSelectorBuilder setName(Predicate<String> nameFilter) {
    requireNonNull(nameFilter, "nameFilter");
    this.nameFilter = nameFilter;
    return this;
  }

  /** Sets the {@link Predicate} for matching versions. */
  public MeterSelectorBuilder setVersion(Predicate<String> versionFilter) {
    requireNonNull(versionFilter, "versionFilter");
    this.versionFilter = versionFilter;
    return this;
  }

  /** Sets a specifier for selecting Meters by version. */
  public MeterSelectorBuilder setVersion(String version) {
    requireNonNull(version, "version");
    return setVersion(StringPredicates.exact(version));
  }

  /** Sets the schema url to match. */
  public MeterSelectorBuilder setSchemaUrl(String schemaUrl) {
    requireNonNull(schemaUrl, "schemaUrl");
    return setSchemaUrl(StringPredicates.exact(schemaUrl));
  }

  /** Sets the {@link Predicate} for matching schema urls. */
  public MeterSelectorBuilder setSchemaUrl(Predicate<String> schemaUrlFilter) {
    requireNonNull(schemaUrlFilter, "schemaUrlFilter");
    this.schemaUrlFilter = schemaUrlFilter;
    return this;
  }

  /** Returns an InstrumentSelector instance with the content of this builder. */
  public MeterSelector build() {
    return MeterSelector.create(nameFilter, versionFilter, schemaUrlFilter);
  }
}
