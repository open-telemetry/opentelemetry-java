/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.exporter.otlp.profiles.FunctionData;
import javax.annotation.concurrent.Immutable;

/**
 * Auto value implementation of {@link FunctionData}, which describes a code function.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
@Immutable
@AutoValue
public abstract class ImmutableFunctionData implements FunctionData {

  /**
   * Returns a new FunctionData describing the given function characteristics.
   *
   * @return a new FunctionData describing the given function characteristics.
   */
  public static FunctionData create(
      int nameStrindex, int systemNameStrindex, int filenameStrindex, long startLine) {
    return new AutoValue_ImmutableFunctionData(
        nameStrindex, systemNameStrindex, filenameStrindex, startLine);
  }

  ImmutableFunctionData() {}
}
