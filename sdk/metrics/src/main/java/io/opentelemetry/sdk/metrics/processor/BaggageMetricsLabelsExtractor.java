package io.opentelemetry.sdk.metrics.processor;

import io.opentelemetry.api.metrics.common.Labels;
import io.opentelemetry.context.Context;

public interface BaggageMetricsLabelsExtractor {

  Labels fromBaggage(Context ctx);
}
