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

import io.opentracing.Scope;
import io.opentracing.ScopeManager;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Binary;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMap;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import openconsensus.trace.propagation.PropagationComponent;
import openconsensus.trace.propagation.SpanContextParseException;

@SuppressWarnings("ReturnMissingNullable")
public final class TracerShim implements Tracer {
  private final openconsensus.trace.Tracer tracer;
  private final PropagationComponent propagation;
  private final ScopeManager scopeManagerShim;

  /**
   * Creates a {@code io.opentracing.Tracer} shim out of the {@code openconsensus.trace.Tracer} and
   * the {@code openconsensus.trace.propagation.PropagationComponent} exposed by {@code
   * openconsensus.trace.Tracing}.
   *
   * @since 0.1.0
   */
  public TracerShim() {
    this(
        openconsensus.trace.Tracing.getTracer(),
        openconsensus.trace.Tracing.getPropagationComponent());
  }

  /**
   * Creates a {@code io.opentracing.Tracer} shim out of a {@code openconsensus.trace.Tracer} and a
   * {@code openconsensus.trace.propagation.PropagationComponent}.
   *
   * @param tracer the {@code openconsensus.trace.Tracer} used by this shim.
   * @param propagation the {@code openconsensus.trace.propagation.PropagationComponent} used for
   *     injection and extraction.
   * @since 0.1.0
   */
  public TracerShim(openconsensus.trace.Tracer tracer, PropagationComponent propagation) {
    this.tracer = tracer;
    this.propagation = propagation;
    this.scopeManagerShim = new ScopeManagerShim(tracer);
  }

  @Override
  public ScopeManager scopeManager() {
    return scopeManagerShim;
  }

  @Override
  public Span activeSpan() {
    return scopeManagerShim.activeSpan();
  }

  @Override
  public Scope activateSpan(Span span) {
    return scopeManagerShim.activate(span);
  }

  @Override
  public SpanBuilder buildSpan(String operationName) {
    return new SpanBuilderShim(tracer, tracer.spanBuilder(operationName));
  }

  @Override
  public <C> void inject(SpanContext context, Format<C> format, C carrier) {
    openconsensus.trace.SpanContext actualContext = getActualContext(context);

    if (format == Format.Builtin.TEXT_MAP || format == Format.Builtin.HTTP_HEADERS) {
      propagation
          .getTraceContextFormat()
          .inject(actualContext, (TextMap) carrier, TextMapSetter.INSTANCE);
      return;
    }

    if (format == Format.Builtin.BINARY) {
      byte[] buff = propagation.getBinaryFormat().toByteArray(actualContext);
      Binary binaryCarrier = (Binary) carrier;
      ByteBuffer byteBuff = binaryCarrier.injectionBuffer(buff.length);
      byteBuff.put(buff);
      return;
    }
  }

  @Override
  public <C> SpanContext extract(Format<C> format, C carrier) {
    Map<String, String> carrierMap = new HashMap<String, String>();
    for (Map.Entry<String, String> entry : (TextMap) carrier) {
      carrierMap.put(entry.getKey(), entry.getValue());
    }

    if (format == Format.Builtin.TEXT_MAP || format == Format.Builtin.HTTP_HEADERS) {
      try {
        openconsensus.trace.SpanContext context =
            propagation.getTraceContextFormat().extract(carrierMap, TextMapGetter.INSTANCE);
        return new SpanContextShim(context);
      } catch (SpanContextParseException e) {
        return null;
      }
    }

    if (format == Format.Builtin.BINARY) {
      ByteBuffer byteBuff = ((Binary) carrier).extractionBuffer();
      byte[] buff = new byte[byteBuff.remaining()];
      byteBuff.get(buff);
      try {
        openconsensus.trace.SpanContext context = propagation.getBinaryFormat().fromByteArray(buff);
        return new SpanContextShim(context);
      } catch (SpanContextParseException e) {
        return null;
      }
    }

    return null;
  }

  @Override
  public void close() {
    // TODO
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

  static final class TextMapGetter
      extends openconsensus.trace.propagation.TextFormat.Getter<Map<String, String>> {
    private TextMapGetter() {}

    public static final TextMapGetter INSTANCE = new TextMapGetter();

    @Override
    public String get(Map<String, String> carrier, String key) {
      return carrier.get(key);
    }
  }

  static openconsensus.trace.SpanContext getActualContext(SpanContext context) {
    if (!(context instanceof SpanContextShim)) {
      throw new IllegalArgumentException("context is not a valid SpanContextShim object");
    }

    return ((SpanContextShim) context).getSpanContext();
  }
}
