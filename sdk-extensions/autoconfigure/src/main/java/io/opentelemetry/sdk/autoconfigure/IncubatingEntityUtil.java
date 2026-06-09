/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.extension.incubator.resources.EntityDetector;
import io.opentelemetry.sdk.extension.incubator.resources.internal.ExtendedEntityUtil;
import io.opentelemetry.sdk.resources.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

final class IncubatingEntityUtil {

  private IncubatingEntityUtil() {}

  @Nullable
  static Resource configureEntityResource(
      ConfigProperties config,
      SpiHelper spiHelper,
      Set<String> enabledProviders,
      Set<String> disabledProviders) {

    List<EntityDetector> detectors = new ArrayList<>();
    for (EntityDetector detector : spiHelper.loadOrdered(EntityDetector.class)) {
      String fqcn = detector.getClass().getName();
      String shortName = detector.getName();
      if (!enabledProviders.isEmpty()
          && !enabledProviders.contains(fqcn)
          && !enabledProviders.contains(shortName)) {
        continue;
      }
      if (disabledProviders.contains(fqcn) || disabledProviders.contains(shortName)) {
        continue;
      }
      detectors.add(detector);
    }

    if (detectors.isEmpty()) {
      return null;
    }

    return ExtendedEntityUtil.runDetection(detectors, config);
  }
}
