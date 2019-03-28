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

package openconsensus.stats.view;

import javax.annotation.concurrent.ThreadSafe;
import openconsensus.stats.StatsRecorder;

/**
 * Class that holds the implementations for {@link ViewManager} and {@link StatsRecorder}.
 *
 * <p>All objects returned by methods on {@code StatsComponent} are cacheable.
 *
 * @since 0.1.0
 */
@ThreadSafe
public abstract class ViewComponent {

  /**
   * Returns the default {@link ViewManager}.
   *
   * @since 0.1.0
   */
  public abstract ViewManager getViewManager();
}
