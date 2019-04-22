/*
 * Copyright 2019, OpenConsensus Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package openconsensus.opencensusshim.trace;

import com.google.auto.value.AutoValue;
import javax.annotation.concurrent.Immutable;
import openconsensus.opencensusshim.common.Function;
import openconsensus.opencensusshim.internal.Utils;

/**
 * A class that represents all the possible values for an attribute. An attribute can have 3 types
 * of values: {@code String}, {@code Boolean} or {@code Long}.
 *
 * @since 0.1.0
 */
@Immutable
public abstract class AttributeValue {
  /**
   * Returns an {@code AttributeValue} with a string value.
   *
   * @param stringValue The new value.
   * @return an {@code AttributeValue} with a string value.
   * @throws NullPointerException if {@code stringValue} is {@code null}.
   * @since 0.1.0
   */
  public static AttributeValue stringAttributeValue(String stringValue) {
    return AttributeValueString.create(stringValue);
  }

  /**
   * Returns an {@code AttributeValue} with a boolean value.
   *
   * @param booleanValue The new value.
   * @return an {@code AttributeValue} with a boolean value.
   * @since 0.1.0
   */
  public static AttributeValue booleanAttributeValue(boolean booleanValue) {
    return AttributeValueBoolean.create(booleanValue);
  }

  /**
   * Returns an {@code AttributeValue} with a long value.
   *
   * @param longValue The new value.
   * @return an {@code AttributeValue} with a long value.
   * @since 0.1.0
   */
  public static AttributeValue longAttributeValue(long longValue) {
    return AttributeValueLong.create(longValue);
  }

  /**
   * Returns an {@code AttributeValue} with a double value.
   *
   * @param doubleValue The new value.
   * @return an {@code AttributeValue} with a double value.
   * @since 0.1.0
   */
  public static AttributeValue doubleAttributeValue(double doubleValue) {
    return AttributeValueDouble.create(doubleValue);
  }

  AttributeValue() {}

  /**
   * Applies a function to the underlying value. The function that is called depends on the value's
   * type, which can be {@code String}, {@code Long}, or {@code Boolean}.
   *
   * @param stringFunction the function that should be applied if the value has type {@code String}.
   * @param longFunction the function that should be applied if the value has type {@code Long}.
   * @param booleanFunction the function that should be applied if the value has type {@code
   *     Boolean}.
   * @param defaultFunction the function that should be applied if the value has a type that was
   *     added after this {@code match} method was added to the API. See {@link
   *     openconsensus.opencensusshim.common.Functions} for some common functions for handling
   *     unknown types.
   * @return the result of the function applied to the underlying value.
   * @since 0.1.0
   * @deprecated in favor of {@link #match(Function, Function, Function, Function, Function)}.
   */
  @Deprecated
  public abstract <T> T match(
      Function<? super String, T> stringFunction,
      Function<? super Boolean, T> booleanFunction,
      Function<? super Long, T> longFunction,
      Function<Object, T> defaultFunction);

  /**
   * Applies a function to the underlying value. The function that is called depends on the value's
   * type, which can be {@code String}, {@code Long}, or {@code Boolean}.
   *
   * @param stringFunction the function that should be applied if the value has type {@code String}.
   * @param longFunction the function that should be applied if the value has type {@code Long}.
   * @param booleanFunction the function that should be applied if the value has type {@code
   *     Boolean}.
   * @param doubleFunction the function that should be applied if the value has type {@code Double}.
   * @param defaultFunction the function that should be applied if the value has a type that was
   *     added after this {@code match} method was added to the API. See {@link
   *     openconsensus.opencensusshim.common.Functions} for some common functions for handling
   *     unknown types.
   * @return the result of the function applied to the underlying value.
   * @since 0.1.0
   */
  @SuppressWarnings("InconsistentOverloads")
  public abstract <T> T match(
      Function<? super String, T> stringFunction,
      Function<? super Boolean, T> booleanFunction,
      Function<? super Long, T> longFunction,
      Function<? super Double, T> doubleFunction,
      Function<Object, T> defaultFunction);

  @Immutable
  @AutoValue
  abstract static class AttributeValueString extends AttributeValue {

    AttributeValueString() {}

    static AttributeValue create(String stringValue) {
      return new AutoValue_AttributeValue_AttributeValueString(
          Utils.checkNotNull(stringValue, "stringValue"));
    }

    @Override
    public final <T> T match(
        Function<? super String, T> stringFunction,
        Function<? super Boolean, T> booleanFunction,
        Function<? super Long, T> longFunction,
        Function<Object, T> defaultFunction) {
      return stringFunction.apply(getStringValue());
    }

    @Override
    public final <T> T match(
        Function<? super String, T> stringFunction,
        Function<? super Boolean, T> booleanFunction,
        Function<? super Long, T> longFunction,
        Function<? super Double, T> doubleFunction,
        Function<Object, T> defaultFunction) {
      return stringFunction.apply(getStringValue());
    }

    abstract String getStringValue();
  }

  @Immutable
  @AutoValue
  abstract static class AttributeValueBoolean extends AttributeValue {

    AttributeValueBoolean() {}

    static AttributeValue create(Boolean booleanValue) {
      return new AutoValue_AttributeValue_AttributeValueBoolean(
          Utils.checkNotNull(booleanValue, "booleanValue"));
    }

    @Override
    public final <T> T match(
        Function<? super String, T> stringFunction,
        Function<? super Boolean, T> booleanFunction,
        Function<? super Long, T> longFunction,
        Function<Object, T> defaultFunction) {
      return booleanFunction.apply(getBooleanValue());
    }

    @Override
    public final <T> T match(
        Function<? super String, T> stringFunction,
        Function<? super Boolean, T> booleanFunction,
        Function<? super Long, T> longFunction,
        Function<? super Double, T> doubleFunction,
        Function<Object, T> defaultFunction) {
      return booleanFunction.apply(getBooleanValue());
    }

    abstract Boolean getBooleanValue();
  }

  @Immutable
  @AutoValue
  abstract static class AttributeValueLong extends AttributeValue {

    AttributeValueLong() {}

    static AttributeValue create(Long longValue) {
      return new AutoValue_AttributeValue_AttributeValueLong(
          Utils.checkNotNull(longValue, "longValue"));
    }

    @Override
    public final <T> T match(
        Function<? super String, T> stringFunction,
        Function<? super Boolean, T> booleanFunction,
        Function<? super Long, T> longFunction,
        Function<Object, T> defaultFunction) {
      return longFunction.apply(getLongValue());
    }

    @Override
    public final <T> T match(
        Function<? super String, T> stringFunction,
        Function<? super Boolean, T> booleanFunction,
        Function<? super Long, T> longFunction,
        Function<? super Double, T> doubleFunction,
        Function<Object, T> defaultFunction) {
      return longFunction.apply(getLongValue());
    }

    abstract Long getLongValue();
  }

  @Immutable
  @AutoValue
  abstract static class AttributeValueDouble extends AttributeValue {

    AttributeValueDouble() {}

    static AttributeValue create(Double doubleValue) {
      return new AutoValue_AttributeValue_AttributeValueDouble(
          Utils.checkNotNull(doubleValue, "doubleValue"));
    }

    @Override
    public final <T> T match(
        Function<? super String, T> stringFunction,
        Function<? super Boolean, T> booleanFunction,
        Function<? super Long, T> longFunction,
        Function<Object, T> defaultFunction) {
      return defaultFunction.apply(getDoubleValue());
    }

    @Override
    public final <T> T match(
        Function<? super String, T> stringFunction,
        Function<? super Boolean, T> booleanFunction,
        Function<? super Long, T> longFunction,
        Function<? super Double, T> doubleFunction,
        Function<Object, T> defaultFunction) {
      return doubleFunction.apply(getDoubleValue());
    }

    abstract Double getDoubleValue();
  }
}
