package io.opentelemetry.exporter.internal;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.sdk.internal.SemConvAttributes;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
public class ServerAttributesUtil {

  private ServerAttributesUtil() {}

  public static Attributes extractServerAttributes(URI parsed) {
    AttributesBuilder builder = Attributes.builder();
    String host = parsed.getHost();
    if (host != null) {
      builder.put(SemConvAttributes.SERVER_ADDRESS, host);
    }
    int port = parsed.getPort();
    if (port == -1) {
      String scheme = parsed.getScheme();
      if ("https".equals(scheme)) {
        port = 443;
      } else if ("http".equals(scheme)) {
        port = 80;
      }
    }
    if (port != -1) {
      builder.put(SemConvAttributes.SERVER_PORT, port);
    }
    return builder.build();
  }

  public static Attributes extractServerAttributes(String httpEndpoint) {
    try {
      URI parsed = new URI(httpEndpoint);
      return extractServerAttributes(parsed);
    } catch (URISyntaxException e) {
      return Attributes.empty();
    }
  }
}
