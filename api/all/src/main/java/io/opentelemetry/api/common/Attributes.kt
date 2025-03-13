/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.opentelemetry.api.common

import java.util.function.BiConsumer
import javax.annotation.concurrent.Immutable

/**
 * An immutable container for attributes.
 *
 *
 * The keys are [AttributeKey]s and the values are Object instances that match the type of
 * the provided key.
 *
 *
 * Null keys will be silently dropped.
 *
 *
 * Note: The behavior of null-valued attributes is undefined, and hence strongly discouraged.
 *
 *
 * Implementations of this interface *must* be immutable and have well-defined value-based
 * equals/hashCode implementations. If an implementation does not strictly conform to these
 * requirements, behavior of the OpenTelemetry APIs and default SDK cannot be guaranteed.
 *
 *
 * For this reason, it is strongly suggested that you use the implementation that is provided
 * here via the factory methods and the [AttributesBuilder].
 */
@Immutable
interface Attributes {
    /** Returns the value for the given [AttributeKey], or `null` if not found.  */
    fun <T> get(key: AttributeKey<T>?): T?

    /** Iterates over all the key-value pairs of attributes contained by this instance.  */
    fun forEach(consumer: BiConsumer<in AttributeKey<*>?, in Any?>?)

    /** The number of attributes contained in this.  */
    fun size(): Int

    /** Whether there are any attributes contained in this.  */
    val isEmpty: Boolean

    /** Returns a read-only view of this [Attributes] as a [Map].  */
    fun asMap(): Map<AttributeKey<*>?, Any?>?

    /**
     * Returns a new [AttributesBuilder] instance populated with the data of this [ ].
     */
    fun toBuilder(): AttributesBuilder?

    companion object {
        /** Returns a [Attributes] instance with no attributes.  */
        @JvmStatic
        fun empty(): Attributes {
            return ArrayBackedAttributes.EMPTY
        }

        /** Returns a [Attributes] instance with a single key-value pair.  */
        fun <T> of(key: AttributeKey<T>?, value: T?): Attributes {
            if (key == null || key.key!!.isEmpty() || value == null) {
                return empty()
            }
            return ArrayBackedAttributes(arrayOf(key, value))
        }

        /**
         * Returns a [Attributes] instance with two key-value pairs. Order of the keys is not
         * preserved. Duplicate keys will be removed.
         */
        fun <T, U> of(key1: AttributeKey<T>?, value1: T?, key2: AttributeKey<U?>?, value2: U?): Attributes {
            if (key1 == null || key1.key!!.isEmpty() || value1 == null) {
                return of(key2, value2)
            }
            if (key2 == null || key2.key!!.isEmpty() || value2 == null) {
                return of(key1, value1)
            }
            if (key1.key == key2.key) {
                // last one in wins
                return of(key2, value2)
            }
            if (key1.key!!.compareTo(key2.key!!) > 0) {
                return ArrayBackedAttributes(arrayOf(key2, value2, key1, value1))
            }
            return ArrayBackedAttributes(arrayOf(key1, value1, key2, value2))
        }

        /**
         * Returns a [Attributes] instance with three key-value pairs. Order of the keys is not
         * preserved. Duplicate keys will be removed.
         */
        fun <T, U, V> of(
            key1: AttributeKey<T>?,
            value1: T,
            key2: AttributeKey<U>?,
            value2: U,
            key3: AttributeKey<V>?,
            value3: V
        ): Attributes {
            return ArrayBackedAttributes.sortAndFilterToAttributes(key1, value1, key2, value2, key3, value3)
        }

        /**
         * Returns a [Attributes] instance with four key-value pairs. Order of the keys is not
         * preserved. Duplicate keys will be removed.
         */
        fun <T, U, V, W> of(
            key1: AttributeKey<T>?,
            value1: T,
            key2: AttributeKey<U>?,
            value2: U,
            key3: AttributeKey<V>?,
            value3: V,
            key4: AttributeKey<W>?,
            value4: W
        ): Attributes {
            return ArrayBackedAttributes.sortAndFilterToAttributes(
                key1,
                value1,
                key2,
                value2,
                key3,
                value3,
                key4,
                value4
            )
        }

        /**
         * Returns a [Attributes] instance with five key-value pairs. Order of the keys is not
         * preserved. Duplicate keys will be removed.
         */
        fun <T, U, V, W, X> of(
            key1: AttributeKey<T>?,
            value1: T,
            key2: AttributeKey<U>?,
            value2: U,
            key3: AttributeKey<V>?,
            value3: V,
            key4: AttributeKey<W>?,
            value4: W,
            key5: AttributeKey<X>?,
            value5: X
        ): Attributes {
            return ArrayBackedAttributes.sortAndFilterToAttributes(
                key1, value1,
                key2, value2,
                key3, value3,
                key4, value4,
                key5, value5
            )
        }

        /**
         * Returns a [Attributes] instance with the given key-value pairs. Order of the keys is not
         * preserved. Duplicate keys will be removed.
         */
        fun <T, U, V, W, X, Y> of(
            key1: AttributeKey<T>?,
            value1: T,
            key2: AttributeKey<U>?,
            value2: U,
            key3: AttributeKey<V>?,
            value3: V,
            key4: AttributeKey<W>?,
            value4: W,
            key5: AttributeKey<X>?,
            value5: X,
            key6: AttributeKey<Y>?,
            value6: Y
        ): Attributes {
            return ArrayBackedAttributes.sortAndFilterToAttributes(
                key1, value1,
                key2, value2,
                key3, value3,
                key4, value4,
                key5, value5,
                key6, value6
            )
        }

        /** Returns a new [AttributesBuilder] instance for creating arbitrary [Attributes].  */
        @JvmStatic
        fun builder(): AttributesBuilder {
            return ArrayBackedAttributesBuilder()
        }
    }
}
