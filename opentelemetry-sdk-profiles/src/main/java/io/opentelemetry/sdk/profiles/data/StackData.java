/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.profiles.data;

import java.util.List;
import javax.annotation.concurrent.Immutable;

/**
 * A Stack represents a list of locations. The first location is the leaf frame.
 *
 * @see "profiles.proto::Stack"
 */
@Immutable
public interface StackData {

  /**
   * Returns a new StackData representing the given list of frames.
   *
   * @return a new StackData representing the given list of frames.
   */
  @SuppressWarnings("AutoValueSubclassLeaked")
  static StackData create(List<Integer> locationIndices) {
    return new AutoValue_ImmutableStackData(locationIndices);
  }

  List<Integer> getLocationIndices();
}
