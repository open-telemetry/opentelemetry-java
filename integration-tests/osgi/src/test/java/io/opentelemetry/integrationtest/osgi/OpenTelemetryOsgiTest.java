/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.integrationtest.osgi;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.context.Context;
import javax.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.BundleContext;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
public class OpenTelemetryOsgiTest {

  @InjectBundleContext @Nullable BundleContext bundleContext;

  @Test
  public void contextApiAvailable() {
    // Verify we're running in OSGi
    assertThat(bundleContext).isNotNull();

    // Verify Context API is available
    Context current = Context.current();
    assertThat(current).isNotNull();

    // Intentionally fail to verify test execution works
    assertThat(1).isEqualTo(2);
  }
}
