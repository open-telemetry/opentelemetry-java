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

import io.opentelemetry.trace.AttributeValue;

/** Constants for standard Open Telemetry HTTP attributes. */
public class HttpTraceConstants {

  public static final String COMPONENT = "component";
  public static final String COMPONENT_VALUE = "http";
  public static final String HTTP_METHOD = "http.method";
  public static final String HTTP_URL = "http.url";
  public static final String HTTP_ROUTE = "http.route";
  public static final String HTTP_STATUS_CODE = "http.status_code";
  public static final String HTTP_STATUS_TEXT = "http.status_text";
  public static final AttributeValue COMPONENT_ATTR_VALUE =
      AttributeValue.stringAttributeValue(COMPONENT_VALUE);

  public static final String EVENT_NAME = "message";
  public static final String EVENT_ATTR_TYPE = "message.type";
  public static final String EVENT_TYPE_SENT = "SENT";
  public static final String EVENT_TYPE_RECEIVED = "RECEIVED";
  public static final String EVENT_ATTR_ID = "message.id";
  public static final String EVENT_ATTR_COMPRESSED = "message.compressed_size";
  public static final String EVENT_ATTR_UNCOMPRESSED = "message.uncompressed_size";
  public static final AttributeValue EVENT_ATTR_SENT =
      AttributeValue.stringAttributeValue(EVENT_TYPE_SENT);
  public static final AttributeValue EVENT_ATTR_RECEIVED =
      AttributeValue.stringAttributeValue(EVENT_TYPE_RECEIVED);

  public static final String LINK_TYPE = "link.type";
  public static final String LINK_ORIGINATING = "originating";
  public static final AttributeValue LINK_ATTR_ORIGINATING =
      AttributeValue.stringAttributeValue(LINK_ORIGINATING);

  public static final String MEASURE_COUNT = "http_server_requests_count_";
  public static final String MEASURE_DURATION = "http_server_requests_seconds";
  public static final String MEASURE_REQ_SIZE = "http_server_requests_bytes";
  public static final String MEASURE_RESP_SIZE = "http_server_responses_bytes";

  private HttpTraceConstants() {}
}
