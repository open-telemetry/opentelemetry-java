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

package openconsensus.stats;

import openconsensus.stats.view.ViewComponent;
import openconsensus.stats.view.ViewManager;

/**
 * Class for accessing the default {@link StatsComponent}.
 *
 * @since 0.1.0
 */
public final class Stats {
  private static final StatsComponent statsComponent = NoopStats.newNoopStatsComponent();
  private static final ViewComponent viewComponent = NoopStats.newNoopViewComponent();

  /**
   * Returns the default {@link StatsRecorder}.
   *
   * @return stats recorder.
   * @since 0.1.0
   */
  public static StatsRecorder getStatsRecorder() {
    return statsComponent.getStatsRecorder();
  }

  /**
   * Returns the default {@link ViewManager}.
   *
   * @return view manager.
   * @since 0.1.0
   */
  public static ViewManager getViewManager() {
    return viewComponent.getViewManager();
  }

  private Stats() {}
}
