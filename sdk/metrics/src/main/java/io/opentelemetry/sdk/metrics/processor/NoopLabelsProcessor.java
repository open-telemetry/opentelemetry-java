package io.opentelemetry.sdk.metrics.processor;

import io.opentelemetry.api.metrics.common.Labels;
import io.opentelemetry.context.Context;

public class NoopLabelsProcessor implements LabelsProcessor {

  @Override
  public Labels onLabelsBound(Context c, Labels labels) {
    return labels;
  }
}
