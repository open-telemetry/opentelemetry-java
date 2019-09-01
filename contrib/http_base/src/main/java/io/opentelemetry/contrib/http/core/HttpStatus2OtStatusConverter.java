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

package io.opentelemetry.contrib.http.core;

import io.opentelemetry.trace.Status;
import javax.annotation.Nullable;

/** Provides standard translation from HTTP status codes to Open Telemetry trace status values. */
public class HttpStatus2OtStatusConverter {

  /**
   * Translates the supplied HTTP status code to equivalent Open Telemetry status.
   *
   * @param httpStatus the source value
   * @return the target value
   */
  public Status convert(int httpStatus) {
    Status status = doCustomConvert(httpStatus);
    if (status != null) {
      return status;
    }
    if (httpStatus >= 200 && httpStatus < 400) {
      status = Status.OK;
    } else if (httpStatus == 401) {
      status = Status.UNAUTHENTICATED;
    } else if (httpStatus == 403) {
      status = Status.PERMISSION_DENIED;
    } else if (httpStatus == 404) {
      status = Status.NOT_FOUND;
    } else if (httpStatus == 408) {
      status = Status.ABORTED;
    } else if (httpStatus == 409 || httpStatus == 412 || httpStatus == 402) {
      status = Status.FAILED_PRECONDITION;
    } else if (httpStatus == 416) {
      status = Status.OUT_OF_RANGE;
    } else if (httpStatus == 429) {
      status = Status.RESOURCE_EXHAUSTED;
    } else if (httpStatus >= 400 && httpStatus < 500) {
      status = Status.INVALID_ARGUMENT;
    } else if (httpStatus == 501) {
      status = Status.UNIMPLEMENTED;
    } else if (httpStatus == 503) {
      status = Status.UNAVAILABLE;
    } else if (httpStatus == 504) {
      status = Status.DEADLINE_EXCEEDED;
    } else if (httpStatus >= 500 && httpStatus < 600) {
      status = Status.INTERNAL;
    } else {
      status = Status.UNKNOWN;
    }
    return status;
  }

  /**
   * Provides non-standard HTTP status mappings if this method is overridden in a sub-class.
   *
   * @param httpStatus the source value
   * @return the target value
   */
  @Nullable
  protected Status doCustomConvert(int httpStatus) {
    return null; // NoOp
  }
}
