package io.opentelemetry.api.incubator.authenticator;

import java.util.Map;

public interface ExporterAuthenticator {
  String getName(); // todo remove this method

  Map<String, String> getAuthenticationHeaders();
}
