/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.baggage;

import javax.annotation.concurrent.Immutable;

/** An entry in a set of baggage. */
@Immutable
public interface BaggageEntry {

  /** Returns the entry's value. */
  String getValue();

  /** Returns the entry's {@link BaggageEntryMetadata}. */
  BaggageEntryMetadata getMetadata();
}
