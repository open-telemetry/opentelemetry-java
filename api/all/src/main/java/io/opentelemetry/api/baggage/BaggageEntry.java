/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.baggage;

/** An entry in a set of baggage. */
public interface BaggageEntry {

  /** Returns the entry's value. */
  String getValue();

  /** Returns the entry's {@link BaggageEntryMetadata}. */
  BaggageEntryMetadata getEntryMetadata();
}
