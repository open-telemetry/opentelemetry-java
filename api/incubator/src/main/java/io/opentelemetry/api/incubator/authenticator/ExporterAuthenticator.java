/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.authenticator;

import java.util.Map;

public interface ExporterAuthenticator {
  Map<String, String> getAuthenticationHeaders();
}
