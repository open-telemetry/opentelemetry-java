/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.profiles;

import io.opentelemetry.exporter.internal.otlp.AttributeKeyValue;

/**
 * Describes a KeyValue pair with optional unit description for the value.
 *
 * @see "common.proto::KeyValue"
 * @see "profiles.proto::KeyValueAndUnit"
 */
public interface KeyValueAndUnitData<T> extends AttributeKeyValue<T> {

  /** Index into string table. */
  String getKeyStringIndex();

  // T getValue();

  /** Index into string table. 0 indicates implicit (via semconv) or undefined. */
  String getUnitStringIndex();
}
// TODO JH finish me, then add Marshaler, tests
