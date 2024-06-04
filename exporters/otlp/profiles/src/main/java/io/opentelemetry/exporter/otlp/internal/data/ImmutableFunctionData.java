/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.exporter.otlp.profiles.FunctionData;
import javax.annotation.concurrent.Immutable;

@Immutable
@AutoValue
public abstract class ImmutableFunctionData implements FunctionData {

  public static FunctionData create(
      long nameIndex, long systemNameIndex, long filenameIndex, long startLine) {
    return new AutoValue_ImmutableFunctionData(
        nameIndex, systemNameIndex, filenameIndex, startLine);
  }

  ImmutableFunctionData() {}
}
