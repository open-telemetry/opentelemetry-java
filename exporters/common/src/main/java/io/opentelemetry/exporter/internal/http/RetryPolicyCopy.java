/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.http;

import java.time.Duration;

class RetryPolicyCopy {

  final int maxAttempts;
  final Duration initialBackoff;
  final Duration maxBackoff;
  final double backoffMultiplier;

  RetryPolicyCopy(
      int maxAttempts, Duration initialBackoff, Duration maxBackoff, double backoffMultiplier) {
    this.maxAttempts = maxAttempts;
    this.initialBackoff = initialBackoff;
    this.maxBackoff = maxBackoff;
    this.backoffMultiplier = backoffMultiplier;
  }
}
