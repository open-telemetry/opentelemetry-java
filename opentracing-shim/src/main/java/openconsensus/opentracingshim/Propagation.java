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

import io.opentracing.SpanContext;
import io.opentracing.propagation.Binary;
import io.opentracing.propagation.TextMap;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import openconsensus.trace.propagation.SpanContextParseException;

final class Propagation {
  private Propagation() {}

  public static void injectTextFormat(
      openconsensus.trace.propagation.TextFormat[] formats,
      openconsensus.trace.SpanContext context,
      TextMap carrier) {
    for (int i = 0; i < formats.length; i++) {
      formats[i].inject(context, carrier, TextMapSetter.INSTANCE);
    }
  }

  @SuppressWarnings({"EmptyCatchBlock", "ReturnMissingNullable"})
  public static SpanContext extractTextFormat(
      openconsensus.trace.propagation.TextFormat[] formats, TextMap carrier) {
    Map<String, String> carrierMap = new HashMap<String, String>();
    for (Map.Entry<String, String> entry : carrier) {
      carrierMap.put(entry.getKey(), entry.getValue());
    }

    for (int i = 0; i < formats.length; i++) {
      try {
        openconsensus.trace.SpanContext context =
            formats[i].extract(carrierMap, TextMapGetter.INSTANCE);
        return new SpanContextShim(context);
      } catch (SpanContextParseException e) {
      }
    }

    return null;
  }

  static final class TextMapSetter
      extends openconsensus.trace.propagation.TextFormat.Setter<TextMap> {
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
      extends openconsensus.trace.propagation.TextFormat.Getter<Map<String, String>> {
    private TextMapGetter() {}

    public static final TextMapGetter INSTANCE = new TextMapGetter();

    @Override
    public String get(Map<String, String> carrier, String key) {
      return carrier.get(key);
    }
  }

  public static void injectBinaryFormat(
      openconsensus.trace.propagation.BinaryFormat format,
      openconsensus.trace.SpanContext context,
      Binary carrier) {

    byte[] buff = format.toByteArray(context);
    ByteBuffer byteBuff = carrier.injectionBuffer(buff.length);
    byteBuff.put(buff);
  }

  @SuppressWarnings({"EmptyCatchBlock", "ReturnMissingNullable"})
  public static SpanContext extractBinaryFormat(
      openconsensus.trace.propagation.BinaryFormat format, Binary carrier) {

    ByteBuffer byteBuff = carrier.extractionBuffer();
    byte[] buff = new byte[byteBuff.remaining()];
    byteBuff.get(buff);

    SpanContext contextShim = null;
    try {
      openconsensus.trace.SpanContext context = format.fromByteArray(buff);
      contextShim = new SpanContextShim(context);
    } catch (SpanContextParseException e) {
    }

    return contextShim;
  }
}
