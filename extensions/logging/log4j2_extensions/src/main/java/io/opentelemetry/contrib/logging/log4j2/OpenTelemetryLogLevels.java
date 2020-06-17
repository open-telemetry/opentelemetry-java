/*
 * Copyright 2020, OpenTelemetry Authors
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

package io.opentelemetry.contrib.logging.log4j2;

/**
 * This enumerates the log levels defined in the <a
 * href="https://github.com/open-telemetry/oteps/blob/master/text/logs/0097-log-data-model.md">log
 * data model</a>.
 */
public enum OpenTelemetryLogLevels {
  UNSET(0),
  TRACE(1),
  TRACE2(2),
  TRACE3(3),
  TRACE4(4),
  DEBUG(5),
  DEBUG2(6),
  DEBUG3(7),
  DEBUG4(8),
  INFO(9),
  INFO2(10),
  INFO3(11),
  INFO4(12),
  WARN(13),
  WARN2(14),
  WARN3(15),
  WARN4(16),
  ERROR(17),
  ERROR2(18),
  ERROR3(19),
  ERROR4(20),
  FATAL(21),
  FATAL2(22),
  FATAL3(23),
  FATAL4(24),
  ;

  private final int intLevel;

  OpenTelemetryLogLevels(int intLevel) {
    this.intLevel = intLevel;
  }

  public int asInt() {
    return intLevel;
  }
}
