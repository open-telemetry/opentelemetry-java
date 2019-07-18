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

package io.opentelemetry.opentracingshim;

import io.opentelemetry.context.propagation.HttpTextFormat;
import io.opentracing.propagation.Binary;
import io.opentracing.propagation.TextMapExtract;
import io.opentracing.propagation.TextMapInject;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

final class Propagation {
  private final TelemetryInfo telemetryInfo;

  Propagation(TelemetryInfo telemetryInfo) {
    this.telemetryInfo = telemetryInfo;
  }

  public void injectTextFormat(SpanContextShim contextShim, TextMapInject carrier) {
    telemetryInfo
        .tracer()
        .getHttpTextFormat()
        .inject(contextShim.getSpanContext(), carrier, TextMapSetter.INSTANCE);
    telemetryInfo
        .contextManager()
        .getHttpTextFormat()
        .inject(contextShim.getDistributedContext(), carrier, TextMapSetter.INSTANCE);
  }

  public SpanContextShim extractTextFormat(TextMapExtract carrier) {
    Map<String, String> carrierMap = new HashMap<String, String>();
    for (Map.Entry<String, String> entry : carrier) {
      carrierMap.put(entry.getKey(), entry.getValue());
    }

    io.opentelemetry.trace.SpanContext context =
        telemetryInfo.tracer().getHttpTextFormat().extract(carrierMap, TextMapGetter.INSTANCE);
    io.opentelemetry.distributedcontext.DistributedContext distContext =
        telemetryInfo
            .contextManager()
            .getHttpTextFormat()
            .extract(carrierMap, TextMapGetter.INSTANCE);

    return new SpanContextShim(telemetryInfo, context, distContext);
  }

  static final class TextMapSetter implements HttpTextFormat.Setter<TextMapInject> {
    private TextMapSetter() {}

    public static final TextMapSetter INSTANCE = new TextMapSetter();

    @Override
    public void put(TextMapInject carrier, String key, String value) {
      carrier.put(key, value);
    }
  }

  // We use Map<> instead of TextMap as we need to query a specified key, and iterating over
  // *all* values per key-query *might* be a bad idea.
  static final class TextMapGetter implements HttpTextFormat.Getter<Map<String, String>> {
    private TextMapGetter() {}

    public static final TextMapGetter INSTANCE = new TextMapGetter();

    @Override
    public String get(Map<String, String> carrier, String key) {
      return carrier.get(key);
    }
  }

  public void injectBinaryFormat(SpanContextShim context, Binary carrier) {
    byte[] contextBuff =
        telemetryInfo.tracer().getBinaryFormat().toByteArray(context.getSpanContext());
    ByteBuffer byteBuff = carrier.injectionBuffer(contextBuff.length);
    byteBuff.put(contextBuff);
  }

  public SpanContextShim extractBinaryFormat(Binary carrier) {

    ByteBuffer byteBuff = carrier.extractionBuffer();
    byte[] buff = new byte[byteBuff.remaining()];
    byteBuff.get(buff);

    return new SpanContextShim(
        telemetryInfo, telemetryInfo.tracer().getBinaryFormat().fromByteArray(buff));
  }
}
