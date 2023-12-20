/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.incubator.propagation;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapGetter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;

/**
 * Utility class to simplify context propagation.
 *
 * <p>The <a
 * href="https://github.com/open-telemetry/opentelemetry-java-contrib/blob/main/extended-tracer/README.md">README</a>
 * explains the use cases in more detail.
 */
public final class ExtendedContextPropagators {

  private ExtendedContextPropagators() {}

  private static final TextMapGetter<Map<String, String>> TEXT_MAP_GETTER =
      new TextMapGetter<Map<String, String>>() {
        @Override
        public Set<String> keys(Map<String, String> carrier) {
          return carrier.keySet();
        }

        @Override
        @Nullable
        public String get(@Nullable Map<String, String> carrier, String key) {
          return carrier == null ? null : carrier.get(key);
        }
      };

  /**
   * Injects the current context into a string map, which can then be added to HTTP headers or the
   * metadata of a message.
   *
   * @param propagators provide the propagators from {@link OpenTelemetry#getPropagators()}
   */
  public static Map<String, String> getTextMapPropagationContext(ContextPropagators propagators) {
    Map<String, String> carrier = new HashMap<>();
    propagators
        .getTextMapPropagator()
        .inject(
            Context.current(),
            carrier,
            (map, key, value) -> {
              if (map != null) {
                map.put(key, value);
              }
            });

    return Collections.unmodifiableMap(carrier);
  }

  /**
   * Extract the context from a string map, which you get from HTTP headers of the metadata of a
   * message you're processing.
   *
   * @param carrier the string map
   * @param propagators provide the propagators from {@link OpenTelemetry#getPropagators()}
   */
  public static Context extractTextMapPropagationContext(
      Map<String, String> carrier, ContextPropagators propagators) {
    Context current = Context.current();
    if (carrier == null) {
      return current;
    }
    CaseInsensitiveMap caseInsensitiveMap = new CaseInsensitiveMap(carrier);
    return propagators.getTextMapPropagator().extract(current, caseInsensitiveMap, TEXT_MAP_GETTER);
  }
}
