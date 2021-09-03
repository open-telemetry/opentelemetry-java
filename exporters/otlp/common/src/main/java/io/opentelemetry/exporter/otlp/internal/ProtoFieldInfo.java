/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class ProtoFieldInfo {

  public static ProtoFieldInfo create(int fieldNumber, String jsonName) {
    return new AutoValue_ProtoFieldInfo(fieldNumber, jsonName);
  }

  public abstract int getFieldNumber();

  public abstract String getJsonName();
}
