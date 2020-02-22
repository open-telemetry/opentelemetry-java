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

package io.opentelemetry.sdk.metrics.export;

import io.opentelemetry.sdk.metrics.data.MetricData;
import java.util.Collection;

/**
 * {@code MetricExporter} is the interface that all "push based" metric libraries should use to
 * config metrics to the OpenTelemetry exporters.
 *
 * <p>All OpenTelemetry exporters should allow access to a {@code MetricExporter} instance.
 *
 * @since 0.1.0
 */
public interface MetricExporter {

  /**
   * The possible results for the config method.
   *
   * @since 0.1.0
   */
  // TODO: extract this enum and unify it with SpanExporter.ResultCode
  enum ResultCode {
    /** The config operation finished successfully. */
    SUCCESS,

    /** The config operation finished with an error, but retrying may succeed. */
    FAILED_RETRYABLE,

    /**
     * The config operation finished with an error, the caller should not try to config the same
     * data again.
     */
    FAILED_NOT_RETRYABLE
  }

  /**
   * Exports the collection of given {@link MetricData}.
   *
   * @param metrics the collection of {@link MetricData} to be exported.
   * @return the result of the config.
   * @since 0.1.0
   */
  ResultCode export(Collection<MetricData> metrics);
}
