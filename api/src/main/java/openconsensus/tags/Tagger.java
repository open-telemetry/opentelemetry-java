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

package openconsensus.tags;

import openconsensus.common.Scope;

/**
 * Object for creating new {@link TagContext}s and {@code TagContext}s based on the current context.
 *
 * <p>This class returns {@link TagContextBuilder builders} that can be used to create the
 * implementation-dependent {@link TagContext}s.
 *
 * <p>Implementations may have different constraints and are free to convert tag contexts to their
 * own subtypes. This means callers cannot assume the {@link #getCurrentTagContext() current
 * context} is the same instance as the one {@link #withTagContext(TagContext) placed into scope}.
 *
 * @since 0.1.0
 */
public abstract class Tagger {

  /**
   * Returns an empty {@code TagContext}.
   *
   * @return an empty {@code TagContext}.
   * @since 0.1.0
   */
  public abstract TagContext empty();

  /**
   * Returns the current {@code TagContext}.
   *
   * @return the current {@code TagContext}.
   * @since 0.1.0
   */
  public abstract TagContext getCurrentTagContext();

  /**
   * Returns a new empty {@code Builder}.
   *
   * @return a new empty {@code Builder}.
   * @since 0.1.0
   */
  public abstract TagContextBuilder emptyBuilder();

  /**
   * Returns a builder based on this {@code TagContext}.
   *
   * @return a builder based on this {@code TagContext}.
   * @since 0.1.0
   */
  public abstract TagContextBuilder toBuilder(TagContext tags);

  /**
   * Returns a new builder created from the current {@code TagContext}.
   *
   * @return a new builder created from the current {@code TagContext}.
   * @since 0.1.0
   */
  public abstract TagContextBuilder currentBuilder();

  /**
   * Enters the scope of code where the given {@code TagContext} is in the current context
   * (replacing the previous {@code TagContext}) and returns an object that represents that scope.
   * The scope is exited when the returned object is closed.
   *
   * @param tags the {@code TagContext} to be set to the current context.
   * @return an object that defines a scope where the given {@code TagContext} is set to the current
   *     context.
   * @since 0.1.0
   */
  public abstract Scope withTagContext(TagContext tags);
}
