/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;

/**
 * Utilities for logging metric diagnostic issues.
 *
 * <p>This is a publicly accessible class purely for testing.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class DebugUtils {
  private DebugUtils() {}

  /**
   * Creates a detailed error message comparing two {@link MetricDescriptor}s.
   *
   * <p>Called when the metrics with the descriptors have the same name, but {@link
   * MetricDescriptor#isCompatibleWith(MetricDescriptor)} is {@code false}.
   *
   * <p>This should identify all issues between the descriptor and log information on where they are
   * defined. Users should be able to find/fix issues based on this error.
   *
   * <p>Visible for testing.
   *
   * @param existing The already registered metric stream.
   * @param conflict The about-to-be registered metric stream.
   * @return A multi-line debugging string.
   */
  public static String duplicateMetricErrorMessage(
      MetricDescriptor existing, MetricDescriptor conflict) {
    StringBuilder result = new StringBuilder("Found duplicate metric definition: ");
    result.append(existing.getName()).append("\n");
    // Now we write out where the existing metric descriptor is coming from, either a raw instrument
    // or a view on a raw instrument.
    if (!conflict.getName().equals(conflict.getSourceInstrument().getName())) {
      // Record the source view.
      result
          .append("\tVIEW defined\n")
          .append(conflict.getViewSourceInfo().multiLineDebugString())
          .append("\tFROM instrument ")
          .append(conflict.getSourceInstrument().getName())
          .append("\n")
          .append(conflict.getSourceInstrument().getSourceInfo().multiLineDebugString());
    } else {
      result
          .append(conflict.getSourceInstrument().getSourceInfo().multiLineDebugString())
          .append("\n");
    }
    // Add information on what's at conflict.
    result.append("Causes\n");
    if (!existing.getName().equals(conflict.getName())) {
      result
          .append("- Name [")
          .append(conflict.getName())
          .append("] does not match [")
          .append(existing.getName())
          .append("]\n");
    }
    if (!existing.getDescription().equals(conflict.getDescription())) {
      result
          .append("- Description [")
          .append(conflict.getDescription())
          .append("] does not match [")
          .append(existing.getDescription())
          .append("]\n");
    }
    if (!existing.getAggregationName().equals(conflict.getAggregationName())) {
      result
          .append("- Aggregation [")
          .append(conflict.getAggregationName())
          .append("] does not match [")
          .append(existing.getAggregationName())
          .append("]\n");
    }
    if (!existing
        .getSourceInstrument()
        .getName()
        .equals(conflict.getSourceInstrument().getName())) {
      result
          .append("- InstrumentName [")
          .append(conflict.getSourceInstrument().getName())
          .append("] does not match [")
          .append(existing.getSourceInstrument().getName())
          .append("]\n");
    }
    if (!existing
        .getSourceInstrument()
        .getDescription()
        .equals(conflict.getSourceInstrument().getDescription())) {
      result
          .append("- InstrumentDescription [")
          .append(conflict.getSourceInstrument().getDescription())
          .append("] does not match [")
          .append(existing.getSourceInstrument().getDescription())
          .append("]\n");
    }
    if (!existing
        .getSourceInstrument()
        .getUnit()
        .equals(conflict.getSourceInstrument().getUnit())) {
      result
          .append("- InstrumentUnit [")
          .append(conflict.getSourceInstrument().getUnit())
          .append("] does not match [")
          .append(existing.getSourceInstrument().getUnit())
          .append("]\n");
    }
    if (!existing
        .getSourceInstrument()
        .getType()
        .equals(conflict.getSourceInstrument().getType())) {
      result
          .append("- InstrumentType [")
          .append(conflict.getSourceInstrument().getType())
          .append("] does not match [")
          .append(existing.getSourceInstrument().getType())
          .append("]\n");
    }
    if (!existing
        .getSourceInstrument()
        .getValueType()
        .equals(conflict.getSourceInstrument().getValueType())) {
      result
          .append("- InstrumentValueType [")
          .append(conflict.getSourceInstrument().getValueType())
          .append("] does not match [")
          .append(existing.getSourceInstrument().getValueType())
          .append("]\n");
    }

    // Next we write out where the existing metric descriptor came from, either a raw instrument
    // or a view on a raw instrument.
    if (existing.getName().equals(existing.getSourceInstrument().getName())) {
      result
          .append("Original instrument registered with same name but is incompatible.\n")
          .append(existing.getSourceInstrument().getSourceInfo().multiLineDebugString())
          .append("\n");
    } else {
      // Log that the view changed the name.
      result
          .append("Conflicting view registered.\n")
          .append(existing.getViewSourceInfo().multiLineDebugString())
          .append("FROM instrument ")
          .append(existing.getSourceInstrument().getName())
          .append("\n")
          .append(existing.getSourceInstrument().getSourceInfo().multiLineDebugString())
          .append("\n");
    }
    return result.toString();
  }
}
