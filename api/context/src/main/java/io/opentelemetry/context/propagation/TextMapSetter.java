/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.context.propagation;

import javax.annotation.Nullable;

/**
 * Class that allows a {@code TextMapPropagator} to set propagated fields into a carrier.
 *
 * <p>{@code Setter} is stateless and allows to be saved as a constant to avoid runtime allocations.
 *
 * @param <C> carrier of propagation fields, such as an http request
 */
public interface TextMapSetter<C> {

  /**
   * Replaces a propagated field with the given value.
   *
   * <p>For example, a setter for an {@link java.net.HttpURLConnection} would be the method
   * reference {@link java.net.HttpURLConnection#addRequestProperty(String, String)}
   *
   * @param carrier holds propagation fields. For example, an outgoing message or http request. To
   *     facilitate implementations as java lambdas, this parameter may be null.
   * @param key the key of the field.
   * @param value the value of the field.
   */
  void set(@Nullable C carrier, String key, String value);
}
