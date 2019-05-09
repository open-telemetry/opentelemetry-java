/*
 * Copyright 2019, OpenTelemetry Authors
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

package io.opentelemetry.opencensusshim.tags;

import io.opentelemetry.opencensusshim.internal.DefaultVisibilityForTesting;
import io.opentelemetry.opencensusshim.internal.Provider;
import io.opentelemetry.opencensusshim.tags.propagation.TagPropagationComponent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

/**
 * Class for accessing the default {@link TagsComponent}.
 *
 * @since 0.1.0
 */
public final class Tags {
  private static final Logger logger = Logger.getLogger(Tags.class.getName());

  private static final TagsComponent tagsComponent =
      loadTagsComponent(TagsComponent.class.getClassLoader());

  private Tags() {}

  /**
   * Returns the default {@code Tagger}.
   *
   * @return the default {@code Tagger}.
   * @since 0.1.0
   */
  public static Tagger getTagger() {
    return tagsComponent.getTagger();
  }

  /**
   * Returns the default {@code TagPropagationComponent}.
   *
   * @return the default {@code TagPropagationComponent}.
   * @since 0.1.0
   */
  public static TagPropagationComponent getTagPropagationComponent() {
    return tagsComponent.getTagPropagationComponent();
  }

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
  public static TaggingState getState() {
    return tagsComponent.getState();
  }

  /**
   * Sets the current {@code TaggingState}.
   *
   * <p>When no implementation is available, {@code setState} does not change the state.
   *
   * @param state the new {@code TaggingState}.
   * @throws IllegalStateException if {@link #getState()} was previously called.
   * @deprecated This method is deprecated because other libraries could cache the result of {@link
   *     #getState()}, use a stale value, and behave incorrectly. It is only safe to call early in
   *     initialization. This method throws {@link IllegalStateException} after {@link #getState()}
   *     has been called, in order to limit changes to the result of {@code getState()}.
   * @since 0.1.0
   */
  @Deprecated
  public static void setState(TaggingState state) {
    tagsComponent.setState(state);
  }

  // Any provider that may be used for TagsComponent can be added here.
  @DefaultVisibilityForTesting
  static TagsComponent loadTagsComponent(@Nullable ClassLoader classLoader) {
    try {
      // Call Class.forName with literal string name of the class to help shading tools.
      return Provider.createInstance(
          Class.forName(
              "opentelemetry.opencensusshim.impl.tags.TagsComponentImpl",
              /*initialize=*/ true,
              classLoader),
          TagsComponent.class);
    } catch (ClassNotFoundException e) {
      logger.log(
          Level.FINE,
          "Couldn't load full implementation for TagsComponent, now trying to load lite "
              + "implementation.",
          e);
    }
    try {
      // Call Class.forName with literal string name of the class to help shading tools.
      return Provider.createInstance(
          Class.forName(
              "opentelemetry.opencensusshim.impllite.tags.TagsComponentImplLite",
              /*initialize=*/ true,
              classLoader),
          TagsComponent.class);
    } catch (ClassNotFoundException e) {
      logger.log(
          Level.FINE,
          "Couldn't load lite implementation for TagsComponent, now using "
              + "default implementation for TagsComponent.",
          e);
    }
    return NoopTags.newNoopTagsComponent();
  }
}
