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

package io.opentelemetry.sdk.contrib.trace.aws;

import com.amazonaws.xray.ThreadLocalStorage;
import io.opentelemetry.sdk.trace.IdsGenerator;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.TraceId;

/**
 * Generates tracing ids compatible with the AWS X-Ray tracing service. In the X-Ray system the
 * first 32 bits of the trace id are the Unix epoch time in secords. Spans (AWS calls them segments)
 * submit with trace id timestamps outside of the last 30 days are rejected.
 *
 * @see <a
 *     href="https://docs.aws.amazon.com/xray/latest/devguide/xray-api-sendingdata.html#xray-api-traceids">Generating
 *     Trace IDs</a>
 */
public class AwsXRayIdsGenerator implements IdsGenerator {

  @Override
  public SpanId generateSpanId() {
    String awsIdStr = generateId();
    return SpanId.fromLowerBase16(awsIdStr, 0);
  }

  @Override
  public TraceId generateTraceId() {
    String traceIdStr = new com.amazonaws.xray.entities.TraceID().toString();
    return TraceId.fromLowerBase16(traceIdStr.substring(2, 10) + traceIdStr.substring(11, 35), 0);
  }

  // Copied from com.amazonaws.xray.entities.Entity
  private static String generateId() {
    String id = Long.toString(ThreadLocalStorage.getRandom().nextLong() >>> 1, 16);
    while (id.length() < 16) {
      id = '0' + id;
    }
    return id;
  }
}
