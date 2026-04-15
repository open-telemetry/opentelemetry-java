/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry;

import java.util.ArrayList;
import java.util.List;
import org.crac.Context;
import org.crac.Resource;

/**
 * A test-only {@link Context} that allows simulating CRaC checkpoint and restore lifecycle events
 * without requiring a CRaC-enabled JDK. Register resources with {@link #register(Resource)}, then
 * call {@link #simulateCheckpoint()} and {@link #simulateRestore()} to drive the lifecycle.
 *
 * <p>Notification order follows the CRaC specification: checkpoint callbacks fire in reverse
 * registration order; restore callbacks fire in forward registration order.
 */
final class MockCracContext extends Context<Resource> {

  private final List<Resource> resources = new ArrayList<>();

  @Override
  public void register(Resource resource) {
    resources.add(resource);
  }

  /**
   * Simulates a CRaC checkpoint by invoking {@link Resource#beforeCheckpoint} on all registered
   * resources in reverse registration order, as the CRaC spec requires.
   */
  void simulateCheckpoint() throws Exception {
    for (int i = resources.size() - 1; i >= 0; i--) {
      resources.get(i).beforeCheckpoint(this);
    }
  }

  /**
   * Simulates a CRaC restore by invoking {@link Resource#afterRestore} on all registered resources
   * in forward registration order, as the CRaC spec requires.
   */
  void simulateRestore() throws Exception {
    for (Resource resource : resources) {
      resource.afterRestore(this);
    }
  }

  // Not used: this context is not itself registered with a parent context.
  @Override
  public void beforeCheckpoint(Context<? extends Resource> context) {}

  @Override
  public void afterRestore(Context<? extends Resource> context) {}
}
