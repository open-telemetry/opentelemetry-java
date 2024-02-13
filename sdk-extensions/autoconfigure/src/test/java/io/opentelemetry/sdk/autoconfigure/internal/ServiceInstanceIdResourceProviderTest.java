/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.internal;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ServiceInstanceIdResourceProviderTest {

  @ParameterizedTest(name = "[{index}]: {0} with {1} from {2}")
  @CsvSource(
      delimiter = '|',
      value = {
        " | none | service.instance.id=custom,service.name=service,service.namespace=ns,host.id=host",
        "random | uuid1 | service.instance.id=uuid1,service.name=service,service.namespace=ns,host.id=host",
        "8ded023e-2034-5633-a0c2-f86ab54f8909 | telemetry.sdk.name,telemetry.sdk.language,k8s.namespace.name,k8s.pod.name,k8s.container.name | k8s.pod.name=vendors-pqr-jh7d2,k8s.namespace.name=accounting,k8s.container.name=some-sidecar,service.name=customers,host.id=graviola",
        "b6c5414c-1aae-5e72-aeea-866f47b7ea64 | telemetry.sdk.name,telemetry.sdk.language,service.namespace,service.name,host.id | service.name=service,service.namespace=ns,host.id=host",
        "17ffc8fd-6ed7-5069-a5fb-2fed78f5455f | telemetry.sdk.name,telemetry.sdk.language,service.name,host.id | service.name=customers,host.id=graviola",
        "random | | service.name=unknown_service:java,service.namespace=ns,host.id=host",
        "random | | service.name=service,service.namespace=ns",
      })
  void createResource(String expected, String expectedVariant, String stringAttributes) {
    // use "go" to make it comparable to the spec
    // https://github.com/open-telemetry/semantic-conventions/pull/312
    Attributes attributes =
        parseAttributes(
            stringAttributes + ",telemetry.sdk.name=opentelemetry,telemetry.sdk.language=go");

    Optional<ServiceInstanceIdResourceProvider.Variant> variant =
        ServiceInstanceIdResourceProvider.findVariant(attributes);
    assertThat(variant.map(ServiceInstanceIdResourceProvider.Variant::toString).orElse(null))
        .isEqualTo(expectedVariant);

    Resource resource = ServiceInstanceIdResourceProvider.createResource(attributes);

    String actual =
        resource.getAttributes().get(ServiceInstanceIdResourceProvider.SERVICE_INSTANCE_ID);
    if ("random".equals(expected)) {
      assertThat(actual).isNotNull();
    } else {
      assertThat(actual).isEqualTo(expected);
    }
  }

  private static Attributes parseAttributes(String stringAttributes) {
    Map<String, String> map =
        DefaultConfigProperties.createFromMap(Collections.singletonMap("key", stringAttributes))
            .getMap("key");

    AttributesBuilder builder = Attributes.builder();
    for (Map.Entry<String, String> entry : map.entrySet()) {
      builder.put(entry.getKey(), entry.getValue());
    }

    return builder.build();
  }
}
