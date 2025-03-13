/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.opentelemetry.api.common

import io.opentelemetry.api.internal.InternalAttributeKeyImpl
import javax.annotation.concurrent.Immutable

/**
 * This interface provides a handle for setting the values of [Attributes]. The type of value
 * that can be set with an implementation of this key is denoted by the type parameter.
 *
 *
 * Implementations MUST be immutable, as these are used as the keys to Maps.
 *
 * @param <T> The type of value that can be set with the key.
</T> */
@Immutable
interface AttributeKey<T> {

    /** Returns the underlying String representation of the key.  */
    val key: String?

    /** Returns the type of attribute for this key. Useful for building switch statements.  */
    val type: AttributeType?

    companion object {
        /** Returns a new AttributeKey for String valued attributes.  */
        @JvmStatic
        fun stringKey(key: String?): AttributeKey<String> {
            return InternalAttributeKeyImpl.create(key, AttributeType.STRING)
        }

        /** Returns a new AttributeKey for Boolean valued attributes.  */
        @JvmStatic
        fun booleanKey(key: String?): AttributeKey<Boolean> {
            return InternalAttributeKeyImpl.create(key, AttributeType.BOOLEAN)
        }

        /** Returns a new AttributeKey for Long valued attributes.  */
        @JvmStatic
        fun longKey(key: String?): AttributeKey<Long> {
            return InternalAttributeKeyImpl.create(key, AttributeType.LONG)
        }

        /** Returns a new AttributeKey for Double valued attributes.  */
        @JvmStatic
        fun doubleKey(key: String?): AttributeKey<Double> {
            return InternalAttributeKeyImpl.create(key, AttributeType.DOUBLE)
        }

        /** Returns a new AttributeKey for List&lt;String&gt; valued attributes.  */
        @JvmStatic
        fun stringArrayKey(key: String?): AttributeKey<List<String>> {
            return InternalAttributeKeyImpl.create(key, AttributeType.STRING_ARRAY)
        }

        /** Returns a new AttributeKey for List&lt;Boolean&gt; valued attributes.  */
        @JvmStatic
        fun booleanArrayKey(key: String?): AttributeKey<List<Boolean>> {
            return InternalAttributeKeyImpl.create(key, AttributeType.BOOLEAN_ARRAY)
        }

        /** Returns a new AttributeKey for List&lt;Long&gt; valued attributes.  */
        @JvmStatic
        fun longArrayKey(key: String?): AttributeKey<List<Long>> {
            return InternalAttributeKeyImpl.create(key, AttributeType.LONG_ARRAY)
        }

        /** Returns a new AttributeKey for List&lt;Double&gt; valued attributes.  */
        @JvmStatic
        fun doubleArrayKey(key: String?): AttributeKey<List<Double>> {
            return InternalAttributeKeyImpl.create(key, AttributeType.DOUBLE_ARRAY)
        }
    }
}
