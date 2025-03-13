/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.opentelemetry.api.common

/**
 * An enum that represents all the possible value types for an `AttributeKey` and hence the
 * types of values that are allowed for [Attributes].
 */
enum class AttributeType {
    STRING,
    BOOLEAN,
    LONG,
    DOUBLE,
    STRING_ARRAY,
    BOOLEAN_ARRAY,
    LONG_ARRAY,
    DOUBLE_ARRAY
}
