/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.opentelemetry.api.common

import java.nio.ByteBuffer

/**
 * Value mirrors the proto [AnyValue](https://github.com/open-telemetry/opentelemetry-proto/blob/ac3242b03157295e4ee9e616af53b81517b06559/opentelemetry/proto/common/v1/common.proto#L28)
 * message type, and is used to model any type.
 *
 *
 * It can be used to represent:
 *
 *
 *  * Primitive values via [.of], [.of], [.of], [       ][.of].
 *  * String-keyed maps (i.e. associative arrays, dictionaries) via [.of],
 * [.of]. Note, because map values are type [Value], maps can be nested
 * within other maps.
 *  * Arrays (heterogeneous or homogenous) via [.of]. Note, because array values
 * are type [Value], arrays can contain primitives, complex types like maps or arrays,
 * or any combination.
 *  * Raw bytes via [.of]
 *
 *
 *
 * Currently, Value is only used as an argument for [ ][io.opentelemetry.api.logs.LogRecordBuilder.setBody].
 *
 * @param <T> the type. See [.getValue] for description of types.
 * @since 1.42.0
</T> */
interface Value<T> {
    /** Returns the type of this [Value]. Useful for building switch statements.  */
    val type: ValueType?

    /**
     * Returns the value for this [Value].
     *
     *
     * The return type varies by [.getType] as described below:
     *
     *
     *  * [ValueType.STRING] returns [String]
     *  * [ValueType.BOOLEAN] returns `boolean`
     *  * [ValueType.LONG] returns `long`
     *  * [ValueType.DOUBLE] returns `double`
     *  * [ValueType.ARRAY] returns [List] of [Value]
     *  * [ValueType.KEY_VALUE_LIST] returns [List] of [KeyValue]
     *  * [ValueType.BYTES] returns read only [ByteBuffer]. See [       ][ByteBuffer.asReadOnlyBuffer].
     *
     */
    val value: T

    /**
     * Return a string encoding of this [Value]. This is intended to be a fallback serialized
     * representation in case there is no suitable encoding that can utilize [.getType] /
     * [.getValue] to serialize specific types.
     *
     *
     * WARNING: No guarantees are made about the encoding of this string response. It MAY change in
     * a future minor release. If you need a reliable string encoding, write your own serializer.
     */
    // TODO(jack-berg): Should this be a JSON encoding?
    fun asString(): String?

    companion object {
        /** Returns an [Value] for the [String] value.  */
        @JvmStatic
        fun of(value: String?): Value<String> {
            return ValueString.create(value)
        }

        /** Returns an [Value] for the `boolean` value.  */
        @JvmStatic
        fun of(value: Boolean): Value<Boolean> {
            return ValueBoolean.create(value)
        }

        /** Returns an [Value] for the `long` value.  */
        @JvmStatic
        fun of(value: Long): Value<Long> {
            return ValueLong.create(value)
        }

        /** Returns an [Value] for the `double` value.  */
        @JvmStatic
        fun of(value: Double): Value<Double> {
            return ValueDouble.create(value)
        }

        /** Returns an [Value] for the `byte[]` value.  */
        @JvmStatic
        fun of(value: ByteArray?): Value<ByteBuffer> {
            return ValueBytes.create(value)
        }

        /** Returns an [Value] for the array of [Value] values.  */
        fun of(vararg value: Value<*>?): Value<List<Value<*>>> {
            return ValueArray.create(*value)
        }

        /** Returns an [Value] for the list of [Value] values.  */
        fun of(value: List<Value<*>?>?): Value<List<Value<*>>> {
            return ValueArray.create(value)
        }

        /**
         * Returns an [Value] for the array of [KeyValue] values. [KeyValue.getKey]
         * values should not repeat - duplicates may be dropped.
         */
        @JvmStatic
        fun of(vararg value: KeyValue?): Value<List<KeyValue>> {
            return KeyValueList.create(*value)
        }

        /** Returns an [Value] for the [Map] of key, [Value].  */
        fun of(value: Map<String?, Value<*>?>?): Value<List<KeyValue>> {
            return KeyValueList.createFromMap(value)
        }
    }
}
