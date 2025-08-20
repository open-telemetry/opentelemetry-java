package io.opentelemetry.api.incubator.authenticator;

import java.util.Map;

public interface ExporterAuthenticator {
  String getName();

  Map<String, String> getAuthenticationHeaders();
}
