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

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import openconsensus.opencensusshim.internal.DefaultVisibilityForTesting;
import openconsensus.opencensusshim.internal.Provider;
import openconsensus.opencensusshim.tags.propagation.TagPropagationComponent;

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

  // Any provider that may be used for TagsComponent can be added here.
  @DefaultVisibilityForTesting
  static TagsComponent loadTagsComponent(@Nullable ClassLoader classLoader) {
    try {
      // Call Class.forName with literal string name of the class to help shading tools.
      return Provider.createInstance(
          Class.forName(
              "openconsensus.opencensusshim.impl.tags.TagsComponentImpl",
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
              "openconsensus.opencensusshim.impllite.tags.TagsComponentImplLite",
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
