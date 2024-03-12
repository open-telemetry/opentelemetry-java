/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.logs;

/**
 * AnyValue type options, mirroring <a
 * href="https://github.com/open-telemetry/opentelemetry-proto/blob/ac3242b03157295e4ee9e616af53b81517b06559/opentelemetry/proto/common/v1/common.proto#L31">AnyValue#value
 * options</a>.
 */
public enum AnyValueType {
  STRING,
  BOOLEAN,
  LONG,
  DOUBLE,
  ARRAY,
  KEY_VALUE_LIST,
  BYTES
}
