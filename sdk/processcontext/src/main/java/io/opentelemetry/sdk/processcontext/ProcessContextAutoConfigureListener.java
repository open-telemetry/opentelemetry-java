/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.processcontext;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.spi.internal.AutoConfigureListener;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.internal.ExtendedOpenTelemetrySdk;
// import io.opentelemetry.sdk.internal.SdkConfigProvider;
import io.opentelemetry.sdk.processcontext.data.ProcessContextData;
// import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.resources.internal.SdkResourceProvider;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@link AutoConfigureListener} SPI implementation for publishing process context.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public class ProcessContextAutoConfigureListener implements AutoConfigureListener {

  private static final Logger LOGGER =
      Logger.getLogger(ProcessContextAutoConfigureListener.class.getName());

  @Override
  public void afterAutoConfigure(OpenTelemetrySdk sdk) {

    ExtendedOpenTelemetrySdk extendedOpenTelemetrySdk = (ExtendedOpenTelemetrySdk) sdk;
    //    ExtendedOpenTelemetrySdk.create(sdk,
    // SdkConfigProvider.create(DeclarativeConfigProperties.empty())).getSdkResourceProvider().getResource();

    SdkResourceProvider sdkResourceProvider = extendedOpenTelemetrySdk.getSdkResourceProvider();

    ProcessContextData processContextData =
        ProcessContextData.create(sdkResourceProvider.getResource(), Attributes.empty());

    ProcessContextPublisher processContextPublisher =
        ProcessContextPublisherFactory.getInstance().create();

    try {
      processContextPublisher.publish(processContextData, Clock.getDefault());
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Failed to publish process context", e);
    }
  }
}
