/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.spi;

import io.opentelemetry.api.internal.ConfigUtil;
import java.util.Iterator;
import java.util.Set;

/** Config properties that allow access to the property names. */
public interface IterableConfigProperties extends ConfigProperties, Iterable<String> {

  /**
   * Returns all property names in the normalized form. System properties are normalized using
   * {@link ConfigUtil#normalizePropertyKey(String)}. Environment variables are normalized using
   * {@link ConfigUtil#normalizeEnvironmentVariableKey(String)}.
   *
   * @return all property names in the normalized form.
   */
  Set<String> getNormalizedPropertyNames();

  @Override
  default Iterator<String> iterator() {
    return getNormalizedPropertyNames().iterator();
  }
}
