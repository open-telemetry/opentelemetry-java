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

package openconsensus.opencensusshim.stats;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import openconsensus.opencensusshim.internal.DefaultVisibilityForTesting;
import openconsensus.opencensusshim.internal.Provider;

/**
 * Class for accessing the default {@link StatsComponent}.
 *
 * @since 0.1.0
 */
public final class Stats {
  private static final Logger logger = Logger.getLogger(Stats.class.getName());

  private static final StatsComponent statsComponent =
      loadStatsComponent(StatsComponent.class.getClassLoader());

  /**
   * Returns the default {@link StatsRecorder}.
   *
   * @since 0.1.0
   */
  public static StatsRecorder getStatsRecorder() {
    return statsComponent.getStatsRecorder();
  }

  /**
   * Returns the default {@link ViewManager}.
   *
   * @since 0.1.0
   */
  public static ViewManager getViewManager() {
    return statsComponent.getViewManager();
  }

  // Any provider that may be used for StatsComponent can be added here.
  @DefaultVisibilityForTesting
  static StatsComponent loadStatsComponent(@Nullable ClassLoader classLoader) {
    try {
      // Call Class.forName with literal string name of the class to help shading tools.
      return Provider.createInstance(
          Class.forName(
              "openconsensus.opencensusshim.impl.stats.StatsComponentImpl",
              /*initialize=*/ true,
              classLoader),
          StatsComponent.class);
    } catch (ClassNotFoundException e) {
      logger.log(
          Level.FINE,
          "Couldn't load full implementation for StatsComponent, now trying to load lite "
              + "implementation.",
          e);
    }
    try {
      // Call Class.forName with literal string name of the class to help shading tools.
      return Provider.createInstance(
          Class.forName(
              "openconsensus.opencensusshim.impllite.stats.StatsComponentImplLite",
              /*initialize=*/ true,
              classLoader),
          StatsComponent.class);
    } catch (ClassNotFoundException e) {
      logger.log(
          Level.FINE,
          "Couldn't load lite implementation for StatsComponent, now using "
              + "default implementation for StatsComponent.",
          e);
    }
    return NoopStats.newNoopStatsComponent();
  }

  private Stats() {}
}
