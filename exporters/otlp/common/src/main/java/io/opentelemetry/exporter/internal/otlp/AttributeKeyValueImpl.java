/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.common.AttributeKey;

@AutoValue
abstract class AttributeKeyValueImpl<T> implements AttributeKeyValue<T> {

  AttributeKeyValueImpl() {}

  static <T> AttributeKeyValueImpl<T> create(AttributeKey<T> attributeKey, T value) {
    return new AutoValue_AttributeKeyValueImpl<T>(attributeKey, value);
  }
}
