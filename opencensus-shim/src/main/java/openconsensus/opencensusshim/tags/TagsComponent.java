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

package openconsensus.opencensusshim.tags;

import openconsensus.opencensusshim.tags.propagation.TagPropagationComponent;

/**
 * Class that holds the implementation for {@link Tagger} and {@link TagPropagationComponent}.
 *
 * <p>All objects returned by methods on {@code TagsComponent} are cacheable.
 *
 * @since 0.1.0
 */
public abstract class TagsComponent {

  /**
   * Returns the {@link Tagger} for this implementation.
   *
   * @since 0.1.0
   */
  public abstract Tagger getTagger();

  /**
   * Returns the {@link TagPropagationComponent} for this implementation.
   *
   * @since 0.1.0
   */
  public abstract TagPropagationComponent getTagPropagationComponent();

  /**
   * Returns the current {@code TaggingState}.
   *
   * <p>When no implementation is available, {@code getState} always returns {@link
   * TaggingState#DISABLED}.
   *
   * <p>Once {@link #getState()} is called, subsequent calls to {@link #setState(TaggingState)} will
   * throw an {@code IllegalStateException}.
   *
   * @return the current {@code TaggingState}.
   * @since 0.1.0
   */
  public abstract TaggingState getState();

  /**
   * Sets the current {@code TaggingState}.
   *
   * <p>When no implementation is available, {@code setState} does not change the state.
   *
   * @param state the new {@code TaggingState}.
   * @throws IllegalStateException if {@link #getState()} was previously called.
   * @deprecated This method is deprecated because other libraries could cache the result of {@link
   *     #getState()}, use a stale value, and behave incorrectly. It is only safe to call early in
   *     initialization. This method throws {@link IllegalStateException} after {@code getState()}
   *     has been called, in order to limit changes to the result of {@code getState()}.
   * @since 0.1.0
   */
  @Deprecated
  public abstract void setState(TaggingState state);
}
