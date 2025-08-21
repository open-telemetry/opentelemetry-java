package io.opentelemetry.api.incubator.authenticator;

import java.util.Map;

public interface ExporterAuthenticator {
  Map<String, String> getAuthenticationHeaders();
}
