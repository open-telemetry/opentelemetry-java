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

package openconsensus.opencensusshim.common;

/*>>>
import org.checkerframework.checker.nullness.qual.Nullable;
*/

/**
 * Commonly used {@link Function} instances.
 *
 * @since 0.1.0
 */
public final class Functions {
  private Functions() {}

  private static final Function<Object, /*@Nullable*/ Void> RETURN_NULL =
      new Function<Object, /*@Nullable*/ Void>() {
        @Override
        @javax.annotation.Nullable
        public Void apply(Object ignored) {
          return null;
        }
      };

  private static final Function<Object, Void> THROW_ILLEGAL_ARGUMENT_EXCEPTION =
      new Function<Object, Void>() {
        @Override
        public Void apply(Object ignored) {
          throw new IllegalArgumentException();
        }
      };

  private static final Function<Object, Void> THROW_ASSERTION_ERROR =
      new Function<Object, Void>() {
        @Override
        public Void apply(Object ignored) {
          throw new AssertionError();
        }
      };

  private static final Function<Object, /*@Nullable*/ String> RETURN_TO_STRING =
      new Function<Object, /*@Nullable*/ String>() {
        @Override
        public /*@Nullable*/ String apply(Object input) {
          return input == null ? null : input.toString();
        }
      };

  /**
   * A {@code Function} that always ignores its argument and returns {@code null}.
   *
   * @return a {@code Function} that always ignores its argument and returns {@code null}.
   * @since 0.1.0
   */
  public static <T> Function<Object, /*@Nullable*/ T> returnNull() {
    // It is safe to cast a producer of Void to anything, because Void is always null.
    @SuppressWarnings("unchecked")
    Function<Object, /*@Nullable*/ T> function = (Function<Object, /*@Nullable*/ T>) RETURN_NULL;
    return function;
  }

  /**
   * A {@code Function} that always ignores its argument and returns a constant value.
   *
   * @return a {@code Function} that always ignores its argument and returns a constant value.
   * @since 0.1.0
   */
  public static <T> Function<Object, T> returnConstant(final T constant) {
    return new Function<Object, T>() {
      @Override
      public T apply(Object ignored) {
        return constant;
      }
    };
  }

  /**
   * A {@code Function} that always returns the {@link #toString()} value of the input.
   *
   * @return a {@code Function} that always returns the {@link #toString()} value of the input.
   * @since 0.1.0
   */
  public static Function<Object, /*@Nullable*/ String> returnToString() {
    return RETURN_TO_STRING;
  }

  /**
   * A {@code Function} that always ignores its argument and throws an {@link
   * IllegalArgumentException}.
   *
   * @return a {@code Function} that always ignores its argument and throws an {@link
   *     IllegalArgumentException}.
   * @since 0.1.0
   */
  public static <T> Function<Object, T> throwIllegalArgumentException() {
    // It is safe to cast this function to have any return type, since it never returns a result.
    @SuppressWarnings("unchecked")
    Function<Object, T> function = (Function<Object, T>) THROW_ILLEGAL_ARGUMENT_EXCEPTION;
    return function;
  }

  /**
   * A {@code Function} that always ignores its argument and throws an {@link AssertionError}.
   *
   * @return a {@code Function} that always ignores its argument and throws an {@code
   *     AssertionError}.
   * @since 0.1.0
   */
  public static <T> Function<Object, T> throwAssertionError() {
    // It is safe to cast this function to have any return type, since it never returns a result.
    @SuppressWarnings("unchecked")
    Function<Object, T> function = (Function<Object, T>) THROW_ASSERTION_ERROR;
    return function;
  }
}
