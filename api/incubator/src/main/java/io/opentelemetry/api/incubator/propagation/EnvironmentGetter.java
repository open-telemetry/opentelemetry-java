/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.propagation;

import io.opentelemetry.context.propagation.TextMapGetter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

/**
 * A {@link TextMapGetter} that extracts context from a map carrier containing environment
 * variables.
 *
 * <p>This is useful when a child process extracts propagated context from environment variables
 * that an application placed in the child process environment. For example:
 *
 * <pre>{@code
 * Map<String, String> env = System.getenv();
 * Context context = contextPropagators.getTextMapPropagator()
 *     .extract(Context.current(), env, EnvironmentGetter.getInstance());
 * }</pre>
 *
 * <p>This getter automatically normalizes keys as environment variable names:
 *
 * <ul>
 *   <li>Replaces an empty key with a single underscore ({@code _})
 *   <li>Converts ASCII letters to uppercase (e.g., {@code traceparent} becomes {@code TRACEPARENT})
 *   <li>Replaces every character that is not an ASCII letter, digit, or underscore with an
 *       underscore
 *   <li>Prepends an underscore if the result would otherwise start with an ASCII digit
 * </ul>
 *
 * <p>Values are treated as opaque strings. Any propagation-format-specific validation or parsing is
 * the responsibility of the propagator, not this carrier.
 *
 * <p>If the underlying carrier performs case-insensitive lookup, as Windows environment variable
 * lookup does, reading a normalized key may resolve an entry whose stored name differs only by
 * case.
 *
 * @see <a href=
 *     "https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/context/env-carriers.md#format-restrictions">Environment
 *     Variable Format Restrictions</a>
 */
public final class EnvironmentGetter implements TextMapGetter<Map<String, String>> {

  private static final AtomicBoolean LOG_KEYS_CALLED = new AtomicBoolean(false);
  private static final Logger LOGGER = Logger.getLogger(EnvironmentGetter.class.getName());
  private static final EnvironmentGetter INSTANCE = new EnvironmentGetter();

  private EnvironmentGetter() {}

  /** Returns the singleton instance of {@link EnvironmentGetter}. */
  public static EnvironmentGetter getInstance() {
    return INSTANCE;
  }

  @Override
  public Iterable<String> keys(Map<String, String> carrier) {
    if (LOG_KEYS_CALLED.compareAndSet(false, true)) {
      LOGGER.log(
          Level.WARNING,
          "keys() called on EnvironmentGetter. "
              + "This getter returns only normalized environment variable names, which may produce unexpected results with propagators that depend on original key casing or special characters.",
          new Throwable());
    }
    if (carrier == null) {
      return Collections.emptyList();
    }
    List<String> result = new ArrayList<>(carrier.size());
    for (String key : carrier.keySet()) {
      if (EnvironmentSetter.isNormalizedKey(key)) {
        result.add(key);
      }
    }
    return Collections.unmodifiableList(result);
  }

  @Nullable
  @Override
  public String get(@Nullable Map<String, String> carrier, String key) {
    if (carrier == null || key == null) {
      return null;
    }
    String normalizedKey = EnvironmentSetter.normalizeKey(key);
    return carrier.get(normalizedKey);
  }

  @Override
  public String toString() {
    return "EnvironmentGetter";
  }
}
