/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.trace;

/**
 * A builder of {@link TraceState}. This implementation does full validation of the keys and values
 * in the entries, and will ignore any entries that do not conform to the W3C specification.
 */
public interface TraceStateBuilder {
  /**
   * Adds or updates the {@code Entry} that has the given {@code key} if it is present. The new
   * {@code Entry} will always be added in the front of the list of entries.
   *
   * @param key the key for the {@code Entry} to be added.
   * @param value the value for the {@code Entry} to be added.
   * @return this.
   */
  TraceStateBuilder put(String key, String value);

  /**
   * Removes the {@code Entry} that has the given {@code key} if it is present.
   *
   * @param key the key for the {@code Entry} to be removed.
   * @return this.
   */
  TraceStateBuilder remove(String key);

  /**
   * Builds a TraceState by adding the entries to the parent in front of the key-value pairs list
   * and removing duplicate entries.
   *
   * @return a TraceState with the new entries.
   */
  TraceState build();
}
