/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.view;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.sdk.metrics.internal.view.StringPredicates;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/** Builder for {@link MeterSelector}. */
public final class MeterSelectorBuilder {

  private Predicate<String> nameFilter = StringPredicates.ALL;
  private Predicate<String> versionFilter = StringPredicates.ALL;
  private Predicate<String> schemaUrlFilter = StringPredicates.ALL;

  /**
   * Sets the {@link Predicate} for matching name.
   *
   * <p>Note: The last provided of {@link #setNameFilter}, {@link #setNamePattern} and {@link
   * #setName} is used.
   */
  public MeterSelectorBuilder setNameFilter(Predicate<String> nameFilter) {
    requireNonNull(nameFilter, "nameFilter");
    this.nameFilter = nameFilter;
    return this;
  }

  /**
   * Sets the {@link Pattern} for matching name.
   *
   * <p>Note: The last provided of {@link #setNameFilter}, {@link #setNamePattern} and {@link
   * #setName} is used.
   */
  public MeterSelectorBuilder setNamePattern(Pattern pattern) {
    requireNonNull(pattern, "pattern");
    return setNameFilter(StringPredicates.regex(pattern));
  }

  /**
   * Sets a specifier for selecting Instruments by name.
   *
   * <p>Note: The last provided of {@link #setNameFilter}, {@link #setNamePattern} and {@link
   * #setName} is used.
   */
  public MeterSelectorBuilder setName(String name) {
    requireNonNull(name, "name");
    return setNameFilter(StringPredicates.exact(name));
  }

  /**
   * Sets the {@link Predicate} for matching versions.
   *
   * <p>Note: The last provided of {@link #setVersionFilter}, {@link #setVersionPattern} and {@link
   * #setVersion} is used.
   */
  public MeterSelectorBuilder setVersionFilter(Predicate<String> versionFilter) {
    requireNonNull(versionFilter, "versionFilter");
    this.versionFilter = versionFilter;
    return this;
  }

  /**
   * Sets the {@link Pattern} for matching versions.
   *
   * <p>Note: The last provided of {@link #setVersionFilter}, {@link #setVersionPattern} and {@link
   * #setVersion} is used.
   */
  public MeterSelectorBuilder setVersionPattern(Pattern pattern) {
    requireNonNull(pattern, "pattern");
    return setVersionFilter(StringPredicates.regex(pattern));
  }

  /**
   * Sets a specifier for selecting Meters by version.
   *
   * <p>Note: The last provided of {@link #setVersionFilter}, {@link #setVersionPattern} and {@link
   * #setVersion} is used.
   */
  public MeterSelectorBuilder setVersion(String version) {
    requireNonNull(version, "version");
    return setVersionFilter(StringPredicates.exact(version));
  }

  /**
   * Sets the {@link Predicate} for matching schema urls.
   *
   * <p>Note: The last provided of {@link #setSchemaUrlFilter}, {@link #setSchemaUrlPattern} and
   * {@link #setSchemaUrl} is used.
   */
  public MeterSelectorBuilder setSchemaUrlFilter(Predicate<String> schemaUrlFilter) {
    requireNonNull(schemaUrlFilter, "schemaUrlFilter");
    this.schemaUrlFilter = schemaUrlFilter;
    return this;
  }

  /**
   * Sets the {@link Pattern} for matching schema urls.
   *
   * <p>Note: The last provided of {@link #setSchemaUrlFilter}, {@link #setSchemaUrlPattern} and
   * {@link #setSchemaUrl} is used.
   */
  public MeterSelectorBuilder setSchemaUrlPattern(Pattern pattern) {
    requireNonNull(pattern, "pattern");
    return setSchemaUrlFilter(StringPredicates.regex(pattern));
  }

  /**
   * Sets the schema url to match.
   *
   * <p>Note: The last provided of {@link #setSchemaUrlFilter}, {@link #setSchemaUrlPattern} and
   * {@link #setSchemaUrl} is used.
   */
  public MeterSelectorBuilder setSchemaUrl(String schemaUrl) {
    requireNonNull(schemaUrl, "schemaUrl");
    return setSchemaUrlFilter(StringPredicates.exact(schemaUrl));
  }

  /** Returns an InstrumentSelector instance with the content of this builder. */
  public MeterSelector build() {
    return MeterSelector.create(nameFilter, versionFilter, schemaUrlFilter);
  }
}
