/*
 * Copyright 2019, OpenConsensus Authors
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

package openconsensus.opentracingshim;

import io.opentracing.propagation.Binary;
import io.opentracing.propagation.TextMap;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

final class Propagation {
  private final TracerShim tracerShim;

  Propagation(TracerShim tracerShim) {
    this.tracerShim = tracerShim;
  }

  public void injectTextFormat(SpanContextShim contextShim, TextMap carrier) {
    tracerShim
        .getTracer()
        .getTextFormat()
        .inject(contextShim.getSpanContext(), carrier, TextMapSetter.INSTANCE);
    tracerShim
        .getTagger()
        .getTextFormat()
        .inject(contextShim.getTagMap(), carrier, TextMapSetter.INSTANCE);
  }

  public SpanContextShim extractTextFormat(TextMap carrier) {
    Map<String, String> carrierMap = new HashMap<String, String>();
    for (Map.Entry<String, String> entry : carrier) {
      carrierMap.put(entry.getKey(), entry.getValue());
    }

    openconsensus.trace.SpanContext context =
        tracerShim.getTracer().getTextFormat().extract(carrierMap, TextMapGetter.INSTANCE);
    openconsensus.tags.TagMap tagMap =
        tracerShim.getTagger().getTextFormat().extract(carrierMap, TextMapGetter.INSTANCE);
    return new SpanContextShim(tracerShim, context, tagMap);
  }

  static final class TextMapSetter
      extends openconsensus.context.propagation.TextFormat.Setter<TextMap> {
    private TextMapSetter() {}

    public static final TextMapSetter INSTANCE = new TextMapSetter();

    @Override
    public void put(TextMap carrier, String key, String value) {
      carrier.put(key, value);
    }
  }

  // We use Map<> instead of TextMap as we need to query a specified key, and iterating over
  // *all* values per key-query *might* be a bad idea.
  static final class TextMapGetter
      extends openconsensus.context.propagation.TextFormat.Getter<Map<String, String>> {
    private TextMapGetter() {}

    public static final TextMapGetter INSTANCE = new TextMapGetter();

    @Override
    public String get(Map<String, String> carrier, String key) {
      return carrier.get(key);
    }
  }

  // TODO - add baggage support for the Binary format.
  public void injectBinaryFormat(SpanContextShim context, Binary carrier) {

    byte[] contextBuff =
        tracerShim.getTracer().getBinaryFormat().toByteArray(context.getSpanContext());
    ByteBuffer byteBuff = carrier.injectionBuffer(contextBuff.length);
    byteBuff.put(contextBuff);
  }

  public SpanContextShim extractBinaryFormat(Binary carrier) {

    ByteBuffer byteBuff = carrier.extractionBuffer();
    byte[] buff = new byte[byteBuff.remaining()];
    byteBuff.get(buff);
    return new SpanContextShim(
        tracerShim, tracerShim.getTracer().getBinaryFormat().fromByteArray(buff));
  }
}
