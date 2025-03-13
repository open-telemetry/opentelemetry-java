/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.opentelemetry.api.common

import io.opentelemetry.api.common.AttributeKey.Companion.booleanArrayKey
import io.opentelemetry.api.common.AttributeKey.Companion.booleanKey
import io.opentelemetry.api.common.AttributeKey.Companion.doubleArrayKey
import io.opentelemetry.api.common.AttributeKey.Companion.doubleKey
import io.opentelemetry.api.common.AttributeKey.Companion.longArrayKey
import io.opentelemetry.api.common.AttributeKey.Companion.longKey
import io.opentelemetry.api.common.AttributeKey.Companion.stringArrayKey
import io.opentelemetry.api.common.AttributeKey.Companion.stringKey
import java.util.*
import java.util.function.Predicate

/** A builder of [Attributes] supporting an arbitrary number of key-value pairs.  */
interface AttributesBuilder {
    /** Create the [Attributes] from this.  */
    fun build(): Attributes?

    /**
     * Puts a [AttributeKey] with associated value into this.
     *
     *
     * The type parameter is unused.
     */
    // The type parameter was added unintentionally and unfortunately it is an API break for
    // implementations of this interface to remove it. It doesn't affect users of the interface in
    // any way, and has almost no effect on implementations, so we leave it until a future major
    // version.
    fun <T> put(key: AttributeKey<Long?>?, value: Int): AttributesBuilder?

    /** Puts a [AttributeKey] with associated value into this.  */
    fun <T> put(key: AttributeKey<T>?, value: T): AttributesBuilder?

    /**
     * Puts a String attribute into this.
     *
     *
     * Note: It is strongly recommended to use [.put], and pre-allocate
     * your keys, if possible.
     *
     * @return this Builder
     */
    fun put(key: String?, value: String?): AttributesBuilder {
        return put(stringKey(key), value)!!
    }

    /**
     * Puts a long attribute into this.
     *
     *
     * Note: It is strongly recommended to use [.put], and pre-allocate
     * your keys, if possible.
     *
     * @return this Builder
     */
    fun put(key: String?, value: Long): AttributesBuilder {
        return put(longKey(key), value)!!
    }

    /**
     * Puts a double attribute into this.
     *
     *
     * Note: It is strongly recommended to use [.put], and pre-allocate
     * your keys, if possible.
     *
     * @return this Builder
     */
    fun put(key: String?, value: Double): AttributesBuilder {
        return put(doubleKey(key), value)!!
    }

    /**
     * Puts a boolean attribute into this.
     *
     *
     * Note: It is strongly recommended to use [.put], and pre-allocate
     * your keys, if possible.
     *
     * @return this Builder
     */
    fun put(key: String?, value: Boolean): AttributesBuilder {
        return put(booleanKey(key), value)!!
    }

    /**
     * Puts a String array attribute into this.
     *
     *
     * Note: It is strongly recommended to use [.put], and pre-allocate
     * your keys, if possible.
     *
     * @return this Builder
     */
    fun put(key: String?, vararg value: String?): AttributesBuilder {
        if (value == null) {
            return this
        }
        return put(stringArrayKey(key), Arrays.asList(*value))!!
    }

    /**
     * Puts a List attribute into this.
     *
     * @return this Builder
     */
    fun <T> put(key: AttributeKey<List<T>>?, vararg value: T): AttributesBuilder? {
        if (value == null) {
            return this
        }
        return put(key, Arrays.asList(*value))
    }

    /**
     * Puts a Long array attribute into this.
     *
     *
     * Note: It is strongly recommended to use [.put], and pre-allocate
     * your keys, if possible.
     *
     * @return this Builder
     */
    fun put(key: String?, vararg value: Long): AttributesBuilder {
        if (value == null) {
            return this
        }
        return put(longArrayKey(key), ArrayBackedAttributesBuilder.toList(*value))!!
    }

    /**
     * Puts a Double array attribute into this.
     *
     *
     * Note: It is strongly recommended to use [.put], and pre-allocate
     * your keys, if possible.
     *
     * @return this Builder
     */
    fun put(key: String?, vararg value: Double): AttributesBuilder {
        if (value == null) {
            return this
        }
        return put(doubleArrayKey(key), ArrayBackedAttributesBuilder.toList(*value))!!
    }

    /**
     * Puts a Boolean array attribute into this.
     *
     *
     * Note: It is strongly recommended to use [.put], and pre-allocate
     * your keys, if possible.
     *
     * @return this Builder
     */
    fun put(key: String?, vararg value: Boolean): AttributesBuilder {
        if (value == null) {
            return this
        }
        return put(booleanArrayKey(key), ArrayBackedAttributesBuilder.toList(*value))!!
    }

    /**
     * Puts all the provided attributes into this Builder.
     *
     * @return this Builder
     */
    fun putAll(attributes: Attributes?): AttributesBuilder?

    /**
     * Remove all attributes where [AttributeKey.getKey] and [AttributeKey.getType]
     * match the `key`.
     *
     * @return this Builder
     */
    fun <T> remove(key: AttributeKey<T>?): AttributesBuilder {
        // default implementation is no-op
        return this
    }

    /**
     * Remove all attributes that satisfy the given predicate. Errors or runtime exceptions thrown by
     * the predicate are relayed to the caller.
     *
     * @return this Builder
     */
    fun removeIf(filter: Predicate<AttributeKey<*>?>?): AttributesBuilder {
        // default implementation is no-op
        return this
    }
}
