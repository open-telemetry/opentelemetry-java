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

package io.opentelemetry.sdk.stats;

import java.util.List;

/**
 * Provides facilities to register {@link View}s for aggregating stats and exporting {@code
 * Metric}s.
 *
 * @since 0.1.0
 */
public abstract class ViewManager {
  /**
   * Pull model for stats. Registers a {@link View} that will aggregate data.
   *
   * @param view the {@code View} to be registered.
   * @since 0.1.0
   */
  public abstract void registerView(View view);

  /**
   * Returns all registered views.
   *
   * <p>This method should be used by any stats exporter that automatically exports data for views
   * registered with the {@link ViewManager}.
   *
   * @return all registered views that should be exported.
   * @since 0.1.0
   */
  public abstract List<View> getAllRegisteredViews();
}
