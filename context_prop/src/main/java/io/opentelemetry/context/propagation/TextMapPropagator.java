/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.context.propagation;

import io.opentelemetry.context.Context;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Injects and extracts a value as text into carriers that travel in-band across process boundaries.
 * Encoding is expected to conform to the HTTP Header Field semantics. Values are often encoded as
 * RPC/HTTP request headers.
 *
 * <p>The carrier of propagated data on both the client (injector) and server (extractor) side is
 * usually an http request. Propagation is usually implemented via library- specific request
 * interceptors, where the client-side injects values and the server-side extracts them.
 *
 * <p>Specific concern values (traces, correlations, etc) will be read from the specified {@code
 * Context}, and resulting values will be stored in a new {@code Context} upon extraction. It is
 * recommended to use a single {@code Context.Key} to store the entire concern data:
 *
 * <pre>{@code
 * public static final Context.Key CONCERN_KEY = Context.key("my-concern-key");
 * public MyConcernPropagator implements TextMapPropagator {
 *   public <C> void inject(Context context, C carrier, Setter<C> setter) {
 *     Object concern = CONCERN_KEY.get(context);
 *     // Use concern in the specified context to propagate data.
 *   }
 *   public <C> Context extract(Context context, C carrier, Getter<C> getter) {
 *     // Use getter to get the data from the carrier.
 *     return context.withValue(CONCERN_KEY, concern);
 *   }
 * }
 * }</pre>
 */
@ThreadSafe
public interface TextMapPropagator {
  /**
   * The propagation fields defined. If your carrier is reused, you should delete the fields here
   * before calling {@link #inject(Context, Object, Setter)} )}.
   *
   * <p>For example, if the carrier is a single-use or immutable request object, you don't need to
   * clear fields as they couldn't have been set before. If it is a mutable, retryable object,
   * successive calls should clear these fields first.
   *
   * @return list of fields that will be used by this formatter.
   */
  // The use cases of this are:
  // * allow pre-allocation of fields, especially in systems like gRPC Metadata
  // * allow a single-pass over an iterator
  List<String> fields();

  /**
   * Injects the value downstream, for example as HTTP headers. The carrier may be null to
   * facilitate calling this method with a lambda for the {@link Setter}, in which case that null
   * will be passed to the {@link Setter} implementation.
   *
   * @param context the {@code Context} containing the value to be injected.
   * @param carrier holds propagation fields. For example, an outgoing message or http request.
   * @param setter invoked for each propagation key to add or remove.
   * @param <C> carrier of propagation fields, such as an http request
   */
  <C> void inject(Context context, @Nullable C carrier, Setter<C> setter);

  /**
   * Class that allows a {@code TextMapPropagator} to set propagated fields into a carrier.
   *
   * <p>{@code Setter} is stateless and allows to be saved as a constant to avoid runtime
   * allocations.
   *
   * @param <C> carrier of propagation fields, such as an http request
   */
  interface Setter<C> {

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

  /**
   * Extracts the value from upstream. For example, as http headers.
   *
   * <p>If the value could not be parsed, the underlying implementation will decide to set an object
   * representing either an empty value, an invalid value, or a valid value. Implementation must not
   * set {@code null}.
   *
   * @param context the {@code Context} used to store the extracted value.
   * @param carrier holds propagation fields. For example, an outgoing message or http request.
   * @param getter invoked for each propagation key to get.
   * @param <C> carrier of propagation fields, such as an http request.
   * @return the {@code Context} containing the extracted value.
   */
  <C> Context extract(Context context, @Nullable C carrier, Getter<C> getter);

  /**
   * Interface that allows a {@code TextMapPropagator} to read propagated fields from a carrier.
   *
   * <p>{@code Getter} is stateless and allows to be saved as a constant to avoid runtime
   * allocations.
   *
   * @param <C> carrier of propagation fields, such as an http request.
   */
  interface Getter<C> {

    /**
     * Returns all the keys in the given carrier.
     *
     * @param carrier carrier of propagation fields, such as an http request.
     * @since 0.10.0
     */
    Collection<String> keys(C carrier);

    /**
     * Returns the first value of the given propagation {@code key} or returns {@code null}.
     *
     * @param carrier carrier of propagation fields, such as an http request.
     * @param key the key of the field.
     * @return the first value of the given propagation {@code key} or returns {@code null}.
     */
    @Nullable
    String get(@Nullable C carrier, String key);
  }
}
