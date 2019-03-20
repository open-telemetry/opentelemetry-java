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

/**
 * State of the {@link StatsComponent}.
 *
 * @since 0.1.0
 */
public enum StatsCollectionState {

  /**
   * State that fully enables stats collection.
   *
   * <p>The {@link StatsComponent} collects stats for registered views.
   *
   * @since 0.1.0
   */
  ENABLED,

  /**
   * State that disables stats collection.
   *
   * <p>The {@link StatsComponent} does not need to collect stats for registered views and may
   * return empty {@link ViewData}s from {@link ViewManager#getView(View.Name)}.
   *
   * @since 0.1.0
   */
  DISABLED
}
