/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.processor;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;

public class NoopLabelsProcessor implements LabelsProcessor {

  @Override
  public Attributes onLabelsBound(Context c, Attributes labels) {
    return labels;
  }
}
