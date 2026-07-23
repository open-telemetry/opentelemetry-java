/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.SamplerModel;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * Provides typed access to experimental properties on {@link SamplerModel}.
 *
 * <p>This class is internal and experimental. Its APIs are unstable and can change at any time. Its
 * APIs (or a version of them) may be promoted to the public stable API in the future, but no
 * guarantees are made.
 */
public final class SamplerModelAccessor {

  private SamplerModelAccessor() {}

  static final String COMPOSITE = "composite/development";
  static final String JAEGER_REMOTE = "jaeger_remote/development";
  static final String PROBABILITY = "probability/development";

  public static final Map<String, Class<?>> EXPERIMENTAL_PROPERTIES;

  static {
    EXPERIMENTAL_PROPERTIES = new HashMap<>();
    EXPERIMENTAL_PROPERTIES.put(COMPOSITE, ExperimentalComposableSamplerModel.class);
    EXPERIMENTAL_PROPERTIES.put(JAEGER_REMOTE, ExperimentalJaegerRemoteSamplerModel.class);
    EXPERIMENTAL_PROPERTIES.put(PROBABILITY, ExperimentalProbabilitySamplerModel.class);
  }

  @Nullable
  public static ExperimentalComposableSamplerModel getComposite(SamplerModel model) {
    return ExtensionPropertyUtil.get(
        COMPOSITE, model.getExtensionProperties(), ExperimentalComposableSamplerModel.class);
  }

  public static SamplerModel withComposite(
      SamplerModel model, ExperimentalComposableSamplerModel value) {
    requireNonNull(value, "value");
    model.withExtensionProperty(COMPOSITE, value);
    return model;
  }

  @Nullable
  public static ExperimentalJaegerRemoteSamplerModel getJaegerRemote(SamplerModel model) {
    return ExtensionPropertyUtil.get(
        JAEGER_REMOTE, model.getExtensionProperties(), ExperimentalJaegerRemoteSamplerModel.class);
  }

  public static SamplerModel withJaegerRemote(
      SamplerModel model, ExperimentalJaegerRemoteSamplerModel value) {
    requireNonNull(value, "value");
    model.withExtensionProperty(JAEGER_REMOTE, value);
    return model;
  }

  @Nullable
  public static ExperimentalProbabilitySamplerModel getProbability(SamplerModel model) {
    return ExtensionPropertyUtil.get(
        PROBABILITY, model.getExtensionProperties(), ExperimentalProbabilitySamplerModel.class);
  }

  public static SamplerModel withProbability(
      SamplerModel model, ExperimentalProbabilitySamplerModel value) {
    requireNonNull(value, "value");
    model.withExtensionProperty(PROBABILITY, value);
    return model;
  }
}
