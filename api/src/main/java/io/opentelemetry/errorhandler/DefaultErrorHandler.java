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

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Default implementation of {@link ErrorHandler}. By default, logs the full string of the error
 * with a level of "Warning".
 *
 * @since 0.1.0
 */
@ThreadSafe
public final class DefaultErrorHandler implements ErrorHandler {
  private static final Logger logger = Logger.getLogger(DefaultErrorHandler.class.getName());
  private static final ErrorHandler instance = new DefaultErrorHandler();

  /**
   * Returns a {@code ErrorHandler} singleton that is the default implementation for {@link
   * ErrorHandler}.
   *
   * @return a {@code ErrorHandler} singleton that is the default implementation for {@link
   *     ErrorHandler}.
   */
  public static ErrorHandler getInstance() {
    return instance;
  }

  @Override
  public void handle(OpenTelemetryException e) {
    logger.log(Level.WARNING, e.getMessage(), e);
  }

  private DefaultErrorHandler() {}
}
