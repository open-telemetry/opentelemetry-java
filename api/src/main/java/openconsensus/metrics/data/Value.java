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

package openconsensus.metrics.data;

import com.google.auto.value.AutoValue;
import javax.annotation.concurrent.Immutable;
import openconsensus.common.ExperimentalApi;

/**
 * The actual point value for a {@link Point}.
 *
 * <p>Currently there are four types of {@link Value}:
 *
 * <ul>
 *   <li>{@code double}
 *   <li>{@code long}
 *   <li>{@link Distribution}
 *   <li>{@link Summary}
 * </ul>
 *
 * <p>Each {@link Point} contains exactly one of the three {@link Value} types.
 *
 * @since 0.1.0
 */
@ExperimentalApi
@Immutable
public abstract class Value {

  Value() {}

  /**
   * Returns a double {@link Value}.
   *
   * @param value value in double.
   * @return a double {@code Value}.
   * @since 0.1.0
   */
  public static Value doubleValue(double value) {
    return ValueDouble.create(value);
  }

  /**
   * Returns a long {@link Value}.
   *
   * @param value value in long.
   * @return a long {@code Value}.
   * @since 0.1.0
   */
  public static Value longValue(long value) {
    return ValueLong.create(value);
  }

  /**
   * Returns a {@link Distribution} {@link Value}.
   *
   * @param value value in {@link Distribution}.
   * @return a {@code Distribution} {@code Value}.
   * @since 0.1.0
   */
  public static Value distributionValue(Distribution value) {
    return ValueDistribution.create(value);
  }

  /**
   * Returns a {@link Summary} {@link Value}.
   *
   * @param value value in {@link Summary}.
   * @return a {@code Summary} {@code Value}.
   * @since 0.1.0
   */
  public static Value summaryValue(Summary value) {
    return ValueSummary.create(value);
  }

  /**
   * Returns the double value of this {@link Value}.
   *
   * <p>This method should only be called with {@link Type#DOUBLE} {@link Value}.
   *
   * @return the double {@code Value}.
   * @since 0.1.0
   */
  public double getDouble() {
    throw new UnsupportedOperationException("Not implemented.");
  }

  /**
   * Returns the long value of this {@link Value}.
   *
   * <p>This method should only be called with {@link Type#LONG} {@link Value}.
   *
   * @return the long {@code Value}.
   * @since 0.1.0
   */
  public long getLong() {
    throw new UnsupportedOperationException("Not implemented.");
  }

  /**
   * Returns the distribution value of this {@link Value}.
   *
   * <p>This method should only be called with {@link Type#DISTRIBUTION} {@link Value}.
   *
   * @return the distribution {@code Value}.
   * @since 0.1.0
   */
  public Distribution getDistribution() {
    throw new UnsupportedOperationException("Not implemented.");
  }

  /**
   * Returns the summary value of this {@link Value}.
   *
   * <p>This method should only be called with {@link Type#SUMMARY} {@link Value}.
   *
   * @return the summary {@code Value}.
   * @since 0.1.0
   */
  public Summary getSummary() {
    throw new UnsupportedOperationException("Not implemented.");
  }

  /**
   * Returns a {@code Type} corresponding to the underlying value of this {@code Value}.
   *
   * @return a {@code Type} corresponding to the underlying value of this {@code Value}.
   * @since 0.1.0
   */
  public abstract Type getType();

  /** A 64-bit double-precision floating-point {@link Value}. */
  @AutoValue
  @Immutable
  abstract static class ValueDouble extends Value {

    ValueDouble() {}

    /**
     * Creates a {@link ValueDouble}.
     *
     * @param value the value in double.
     * @return a {@code ValueDouble}.
     */
    static ValueDouble create(double value) {
      return new AutoValue_Value_ValueDouble(value);
    }

    @Override
    public abstract double getDouble();

    @Override
    public final Type getType() {
      return Type.DISTRIBUTION;
    }
  }

  /** A 64-bit integer {@link Value}. */
  @AutoValue
  @Immutable
  abstract static class ValueLong extends Value {

    ValueLong() {}

    /**
     * Creates a {@link ValueLong}.
     *
     * @param value the value in long.
     * @return a {@code ValueLong}.
     */
    static ValueLong create(long value) {
      return new AutoValue_Value_ValueLong(value);
    }

    @Override
    public abstract long getLong();

    @Override
    public final Type getType() {
      return Type.LONG;
    }
  }

  /**
   * {@link ValueDistribution} contains summary statistics for a population of values. It optionally
   * contains a histogram representing the distribution of those values across a set of buckets.
   */
  @AutoValue
  @Immutable
  abstract static class ValueDistribution extends Value {

    ValueDistribution() {}

    /**
     * Creates a {@link ValueDistribution}.
     *
     * @param value the {@link Distribution} value.
     * @return a {@code ValueDistribution}.
     */
    static ValueDistribution create(Distribution value) {
      return new AutoValue_Value_ValueDistribution(value);
    }

    @Override
    public abstract Distribution getDistribution();

    @Override
    public final Type getType() {
      return Type.DISTRIBUTION;
    }
  }

  /**
   * {@link ValueSummary} contains a snapshot representing values calculated over an arbitrary time
   * window.
   */
  @AutoValue
  @Immutable
  abstract static class ValueSummary extends Value {

    ValueSummary() {}

    /**
     * Creates a {@link ValueSummary}.
     *
     * @param value the {@link Summary} value.
     * @return a {@code ValueSummary}.
     */
    static ValueSummary create(Summary value) {
      return new AutoValue_Value_ValueSummary(value);
    }

    @Override
    public abstract Summary getSummary();

    @Override
    public final Type getType() {
      return Type.SUMMARY;
    }
  }

  /**
   * An enum that represents all the possible value types for a {@code Value}.
   *
   * @since 0.1.0
   */
  public enum Type {
    LONG,
    DOUBLE,
    DISTRIBUTION,
    SUMMARY
  }
}
