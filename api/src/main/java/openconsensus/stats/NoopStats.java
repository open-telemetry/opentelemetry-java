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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import openconsensus.internal.Utils;
import openconsensus.stats.Measure.MeasureDouble;
import openconsensus.stats.Measure.MeasureLong;
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
   * Returns a {@code StatsRecorder} that does not record any data.
   *
   * @return a {@code StatsRecorder} that does not record any data.
   */
  static StatsRecorder getNoopStatsRecorder() {
    return NoopStatsRecorder.INSTANCE;
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
  private static final class NoopStatsComponent extends StatsComponent {
    private final ViewManager viewManager = newNoopViewManager();

    @Override
    public ViewManager getViewManager() {
      return viewManager;
    }

    @Override
    public StatsRecorder getStatsRecorder() {
      return getNoopStatsRecorder();
    }
  }

  @Immutable
  private static final class NoopStatsRecorder extends StatsRecorder {
    static final StatsRecorder INSTANCE = new NoopStatsRecorder();

    @Override
    public MeasureMap newMeasureMap() {
      return newNoopMeasureMap();
    }
  }

  private static final class NoopMeasureMap extends MeasureMap {
    private static final Logger logger = Logger.getLogger(NoopMeasureMap.class.getName());
    private boolean hasUnsupportedValues;

    @Override
    public MeasureMap put(MeasureDouble measure, double value) {
      if (value < 0) {
        hasUnsupportedValues = true;
      }
      return this;
    }

    @Override
    public MeasureMap put(MeasureLong measure, long value) {
      if (value < 0) {
        hasUnsupportedValues = true;
      }
      return this;
    }

    @Override
    public void record() {}

    @Override
    public void record(TagMap tags) {
      Utils.checkNotNull(tags, "tags");

      if (hasUnsupportedValues) {
        // drop all the recorded values
        logger.log(Level.WARNING, "Dropping values, value to record must be non-negative.");
      }
    }
  }

  @ThreadSafe
  private static final class NoopViewManager extends ViewManager {
    @GuardedBy("registeredViews")
    private final Map<View.Name, View> registeredViews = new HashMap<View.Name, View>();

    // Cached set of exported views. It must be set to null whenever a view is registered or
    // unregistered.
    @javax.annotation.Nullable private volatile Set<View> exportedViews;

    @Override
    public void registerView(View newView) {
      Utils.checkNotNull(newView, "newView");
      synchronized (registeredViews) {
        exportedViews = null;
        View existing = registeredViews.get(newView.getName());
        Utils.checkArgument(
            existing == null || newView.equals(existing),
            "A different view with the same name already exists.");
        if (existing == null) {
          registeredViews.put(newView.getName(), newView);
        }
      }
    }

    @Override
    public Set<View> getAllRegisteredViews() {
      Set<View> views = exportedViews;
      if (views == null) {
        synchronized (registeredViews) {
          exportedViews = views = filterExportedViews(registeredViews.values());
        }
      }
      return views;
    }

    // Returns the subset of the given views that should be exported
    @SuppressWarnings("deprecation")
    private static Set<View> filterExportedViews(Collection<View> allViews) {
      Set<View> views = new HashSet<View>();
      for (View view : allViews) {
        if (view.getWindow() instanceof View.AggregationWindow.Interval) {
          continue;
        }
        views.add(view);
      }
      return Collections.unmodifiableSet(views);
    }
  }
}
