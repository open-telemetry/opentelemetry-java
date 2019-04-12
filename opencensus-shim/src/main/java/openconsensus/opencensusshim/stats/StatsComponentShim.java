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

import io.opencensus.stats.StatsCollectionState;
import io.opencensus.stats.StatsComponent;
import io.opencensus.stats.StatsRecorder;
import io.opencensus.stats.ViewManager;

public final class StatsComponentShim extends StatsComponent {

  @Override
  public ViewManager getViewManager() {
    throw new UnsupportedOperationException();
  }

  @Override
  public StatsRecorder getStatsRecorder() {
    throw new UnsupportedOperationException();
  }

  @Override
  public StatsCollectionState getState() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setState(StatsCollectionState state) {
    throw new UnsupportedOperationException();
  }
}
