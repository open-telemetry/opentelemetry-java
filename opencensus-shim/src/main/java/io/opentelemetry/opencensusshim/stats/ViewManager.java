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

package io.opentelemetry.opencensusshim.stats;

import java.util.Set;
import javax.annotation.Nullable;

/**
 * Provides facilities to register {@link View}s for collecting stats and retrieving stats data as a
 * {@link ViewData}.
 *
 * @since 0.1.0
 */
public abstract class ViewManager {
  /**
   * Pull model for stats. Registers a {@link View} that will collect data to be accessed via {@link
   * #getView(View.Name)}.
   *
   * @param view the {@code View} to be registered.
   * @since 0.1.0
   */
  public abstract void registerView(View view);

  /**
   * Returns the current stats data, {@link ViewData}, associated with the given view name.
   *
   * <p>Returns {@code null} if the {@code View} is not registered.
   *
   * @param view the name of {@code View} for the current stats.
   * @return {@code ViewData} for the {@code View}, or {@code null} if the {@code View} is not
   *     registered.
   * @since 0.1.0
   */
  @Nullable
  public abstract ViewData getView(View.Name view);

  /**
   * Returns all registered views that should be exported.
   *
   * <p>This method should be used by any stats exporter that automatically exports data for views
   * registered with the {@link ViewManager}.
   *
   * @return all registered views that should be exported.
   * @since 0.1.0
   */
  public abstract Set<View> getAllExportedViews();
}
