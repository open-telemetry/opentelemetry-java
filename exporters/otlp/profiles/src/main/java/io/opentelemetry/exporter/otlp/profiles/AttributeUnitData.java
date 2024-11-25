/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.profiles;

import javax.annotation.concurrent.Immutable;

/**
 * Represents a mapping between Attribute Keys and Units.
 *
 * @see "profiles.proto::AttributeUnit"
 */
@Immutable
public interface AttributeUnitData {

  /** Index into string table. */
  int getAttributeKeyStrindex();

  /** Index into string table. */
  int getUnitIndexStrindex();
}
