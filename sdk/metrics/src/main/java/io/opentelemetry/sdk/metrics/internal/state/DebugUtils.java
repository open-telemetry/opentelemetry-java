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

  static String duplicateMetricErrorMessage(DuplicateMetricStorageException ex) {
    return duplicateMetricErrorMessage(ex.getExisting(), ex.getConflict());
  }

  /**
   * Creates a detailed error message comparing two MetricDescriptors.
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
    final StringBuilder result = new StringBuilder("Found duplicate metric definition: ");
    result.append(existing.getName()).append("\n");
    // Now we write out where the existing metric descriptor is coming from, either a raw instrument
    // or a view on a raw instrument.
    if (!conflict.getName().equals(conflict.getSourceInstrument().getName())) {
      // Record the source view.
      result.append("\tVIEW defined\n");
      conflict
          .getSourceView()
          .ifPresent(v -> result.append(v.getSourceInfo().multiLineDebugString()));
      result
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
    if (!existing.getDescription().equals(conflict.getDescription())) {
      result
          .append("- Description [")
          .append(conflict.getDescription())
          .append("] does not match [")
          .append(existing.getDescription())
          .append("]\n");
    }
    if (!existing.getUnit().equals(conflict.getUnit())) {
      result
          .append("- Unit [")
          .append(conflict.getUnit())
          .append("] does not match [")
          .append(existing.getUnit())
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
          .append(
              "Original instrument registered with same name but different description, unit, instrument type, or instrument value type.\n")
          .append(existing.getSourceInstrument().getSourceInfo().multiLineDebugString())
          .append("\n");
    } else {
      // Log that the view changed the name.
      result.append("Conflicting view registered.\n");
      existing
          .getSourceView()
          .ifPresent(view -> result.append(view.getSourceInfo().multiLineDebugString()));
      result
          .append("FROM instrument ")
          .append(existing.getSourceInstrument().getName())
          .append("\n")
          .append(existing.getSourceInstrument().getSourceInfo().multiLineDebugString())
          .append("\n");
    }
    return result.toString();
  }
}
