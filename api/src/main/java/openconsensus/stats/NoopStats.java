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

import java.util.Collections;
import java.util.List;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import openconsensus.internal.Utils;
import openconsensus.stats.data.Measure.MeasureDouble;
import openconsensus.stats.data.Measure.MeasureLong;
import openconsensus.stats.view.ViewComponent;
import openconsensus.stats.view.ViewManager;
import openconsensus.stats.view.data.View;
import openconsensus.tags.TagMap;

/** No-op implementations of stats classes. */
final class NoopStats {

  private NoopStats() {}

  /**
   * Returns a {@code StatsComponent} that has a no-op implementation for {@link StatsRecorder}.
   *
   * @return a {@code StatsComponent} that has a no-op implementation for {@code StatsRecorder}.
   */
  static StatsComponent newNoopStatsComponent() {
    return new NoopStatsComponent();
  }

  /**
   * Returns a {@code ViewComponent} that has a no-op implementation for {@link ViewManager}.
   *
   * @return a {@code ViewComponent} that has a no-op implementation for {@code ViewManager}.
   */
  static ViewComponent newNoopViewComponent() {
    return new NoopViewComponent();
  }

  /**
   * Returns a {@code StatsRecorder} that does not record any data.
   *
   * @return a {@code StatsRecorder} that does not record any data.
   */
  static StatsRecorder newNoopStatsRecorder() {
    return new NoopStatsRecorder();
  }

  /**
   * Returns a {@code MeasureMap} that ignores all calls to {@link MeasureMap#put}.
   *
   * @return a {@code MeasureMap} that ignores all calls to {@code MeasureMap#put}.
   */
  static MeasureMap newNoopMeasureMap() {
    return new NoopMeasureMap();
  }

  /**
   * Returns a {@code ViewManager} that maintains a map of views..
   *
   * @return a {@code ViewManager} that maintains a map of views..
   */
  static ViewManager newNoopViewManager() {
    return new NoopViewManager();
  }

  @ThreadSafe
  private static final class NoopViewComponent extends ViewComponent {
    private final ViewManager viewManager = newNoopViewManager();

    @Override
    public ViewManager getViewManager() {
      return viewManager;
    }
  }

  @ThreadSafe
  private static final class NoopStatsComponent extends StatsComponent {
    private final StatsRecorder statsRecorder = newNoopStatsRecorder();

    @Override
    public StatsRecorder getStatsRecorder() {
      return statsRecorder;
    }
  }

  @Immutable
  private static final class NoopStatsRecorder extends StatsRecorder {
    @Override
    public MeasureMap newMeasureMap() {
      return newNoopMeasureMap();
    }
  }

  private static final class NoopMeasureMap extends MeasureMap {
    @Override
    public MeasureMap put(MeasureDouble measure, double value) {
      Utils.checkArgument(value >= 0.0, "Unsupported negative values.");
      return this;
    }

    @Override
    public MeasureMap put(MeasureLong measure, long value) {
      Utils.checkArgument(value >= 0, "Unsupported negative values.");
      return this;
    }

    @Override
    public void record() {}

    @Override
    public void record(TagMap tags) {
      Utils.checkNotNull(tags, "tags");
    }
  }

  @ThreadSafe
  private static final class NoopViewManager extends ViewManager {
    @Override
    public void registerView(View newView) {
      Utils.checkNotNull(newView, "newView");
    }

    @Override
    public List<View> getAllRegisteredViews() {
      return Collections.emptyList();
    }
  }
}
