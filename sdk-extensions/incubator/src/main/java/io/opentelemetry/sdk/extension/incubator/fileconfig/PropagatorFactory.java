/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.PropagatorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.TextMapPropagatorModel;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

final class PropagatorFactory implements Factory<PropagatorModel, ContextPropagators> {

  private static final PropagatorFactory INSTANCE = new PropagatorFactory();

  private PropagatorFactory() {}

  static PropagatorFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public ContextPropagators create(
      PropagatorModel model, SpiHelper spiHelper, List<Closeable> closeables) {
    List<TextMapPropagatorModel> textMapPropagatorModels = model.getComposite();
    Set<String> propagatorNames = new HashSet<>();
    List<TextMapPropagator> textMapPropagators = new ArrayList<>();
    if (textMapPropagatorModels != null && !textMapPropagatorModels.isEmpty()) {
      textMapPropagatorModels.forEach(
          textMapPropagatorModel -> {
            TextMapPropagatorAndName propagatorAndName =
                TextMapPropagatorFactory.getInstance()
                    .create(textMapPropagatorModel, spiHelper, closeables);
            textMapPropagators.add(propagatorAndName.getTextMapPropagator());
            propagatorNames.add(propagatorAndName.getName());
          });
    }

    String compositeList = model.getCompositeList();
    if (compositeList != null) {
      List<String> propagatorNamesList =
          // Process string list same as we process OTEL_PROPAGATORS, trimming and filtering empty
          // and 'none'
          Arrays.asList(compositeList.split(",")).stream()
              .map(String::trim)
              .filter(s -> !s.isEmpty())
              .filter(s -> !s.equals("none"))
              .collect(Collectors.toList());
      for (String propagatorName : propagatorNamesList) {
        // Only add entries which weren't already previously added
        if (!propagatorNames.add(propagatorName)) {
          continue;
        }
        textMapPropagators.add(
            TextMapPropagatorFactory.getPropagator(spiHelper, propagatorName)
                .getTextMapPropagator());
      }
    }

    return ContextPropagators.create(TextMapPropagator.composite(textMapPropagators));
  }
}
