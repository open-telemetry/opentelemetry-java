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
import openconsensus.stats.view.ViewComponent;
import openconsensus.stats.view.ViewManager;
import openconsensus.stats.view.data.View;
import openconsensus.tags.TagMap;
import openconsensus.trace.SpanContext;

/**
 * No-op implementations of stats classes.
 *
 * @since 0.1.0
 */
public final class NoopStats {

  private NoopStats() {}

  /**
   * Returns a {@code StatsRecorder} that is no-op implementation for {@link StatsRecorder}.
   *
   * @return a {@code StatsRecorder} that is no-op implementation for {@code StatsRecorder}.
   * @since 0.1.0
   */
  public static StatsRecorder newNoopStatsRecorder() {
    return new NoopStatsRecorder();
  }

  /**
   * Returns a {@code ViewComponent} that has a no-op implementation for {@link ViewManager}.
   *
   * @return a {@code ViewComponent} that has a no-op implementation for {@code ViewManager}.
   * @since 0.1.0
   */
  public static ViewComponent newNoopViewComponent() {
    return new NoopViewComponent();
  }

  @ThreadSafe
  private static final class NoopViewComponent extends ViewComponent {
    private static final ViewManager VIEW_MANAGER = new NoopViewManager();

    @Override
    public ViewManager getViewManager() {
      return VIEW_MANAGER;
    }
  }

  @Immutable
  private static final class NoopStatsRecorder extends StatsRecorder {

    @Override
    public void record(List<Measurement> measurements) {
      Utils.checkNotNull(measurements, "measurements");
    }

    @Override
    public void record(TagMap tags, List<Measurement> measurements) {
      Utils.checkNotNull(tags, "tags");
      Utils.checkNotNull(measurements, "measurements");
    }

    @Override
    public void record(TagMap tags, List<Measurement> measurements, SpanContext spanContext) {
      Utils.checkNotNull(tags, "tags");
      Utils.checkNotNull(measurements, "measurements");
      Utils.checkNotNull(spanContext, "spanContext");
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
