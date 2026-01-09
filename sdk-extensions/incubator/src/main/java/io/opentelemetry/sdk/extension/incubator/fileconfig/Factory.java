/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

interface Factory<ModelT, ResultT> {

  /**
   * Interpret the model and create {@link ResultT} with corresponding configuration.
   *
   * @param model the configuration model
   * @param context the configuration context
   * @return the {@link ResultT}
   */
  ResultT create(ModelT model, DeclarativeConfigContext context);
}
