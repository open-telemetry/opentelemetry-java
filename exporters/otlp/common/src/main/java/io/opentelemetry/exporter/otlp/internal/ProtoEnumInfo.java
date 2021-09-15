/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal;

import com.google.auto.value.AutoValue;

/**
 * Information about a field in a proto definition.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
@AutoValue
public abstract class ProtoEnumInfo {

  public static ProtoEnumInfo create(int enumNumber, String jsonName) {
    return new AutoValue_ProtoEnumInfo(enumNumber, jsonName);
  }

  public abstract int getEnumNumber();

  public abstract String getJsonName();
}
