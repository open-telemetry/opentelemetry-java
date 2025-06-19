/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.resources.internal;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.common.Attributes;
import java.util.Collection;
import javax.annotation.concurrent.Immutable;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
@Immutable
@AutoValue
abstract class RawAttributeMergeResult {
  /** Merged raw attributes. */
  abstract Attributes getAttributes();

  /**
   * Entities in conflict that should be removed from resource to avoid reporting invalid attribute
   * sets in OTLP resource.
   */
  abstract Collection<Entity> getConflicts();

  static final RawAttributeMergeResult create(Attributes attributes, Collection<Entity> conflicts) {
    return new AutoValue_RawAttributeMergeResult(attributes, conflicts);
  }
}
