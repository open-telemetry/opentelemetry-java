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

package io.opentelemetry.errorhandler;

/**
 * An {@code OpenTelemetryException} is an error or exception which is deemed irremediable by an
 * OpenTelemetry component. {@code OpenTelemetryExceptions} should be handled by the registered
 * OpenTelemetry {@link ErrorHandler} delegate. {@code OpenTelemetryExceptions} may be extended to
 * indicate that the exception comes from a specific OpenTelemetry component.
 */
public class OpenTelemetryException extends RuntimeException {
  private static final long serialVersionUID = 0L;

  public OpenTelemetryException(String message) {
    super(message);
  }

  public OpenTelemetryException(String message, Throwable cause) {
    super(message, cause);
  }
}
