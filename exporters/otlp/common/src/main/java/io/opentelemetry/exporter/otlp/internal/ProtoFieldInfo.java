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
public abstract class ProtoFieldInfo {

  public static ProtoFieldInfo create(int fieldNumber, int tag, String jsonName) {
    return new AutoValue_ProtoFieldInfo(
        fieldNumber, tag, CodedOutputStream.computeTagSize(fieldNumber), jsonName);
  }

  public abstract int getFieldNumber();

  public abstract int getTag();

  public abstract int getTagSize();

  public abstract String getJsonName();
}
