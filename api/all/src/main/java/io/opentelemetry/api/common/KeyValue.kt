/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.opentelemetry.api.common

/**
 * Key-value pair of [String] key and [Value] value.
 *
 * @see Value.of
 * @since 1.42.0
 */
interface KeyValue {
    /** Returns the key.  */
    val key: String?

    /** Returns the value.  */
    val value: Value<*>?

    companion object {
        /** Returns a [KeyValue] for the given `key` and `value`.  */
        fun of(key: String?, value: Value<*>?): KeyValue {
            return KeyValueImpl.create(key, value)
        }
    }
}
