/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.aws.resource;

import static io.opentelemetry.sdk.extension.aws.resource.EksResource.AUTH_CONFIGMAP_PATH;
import static io.opentelemetry.sdk.extension.aws.resource.EksResource.CW_CONFIGMAP_PATH;
import static io.opentelemetry.sdk.extension.aws.resource.EksResource.K8S_SVC_URL;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.autoconfigure.spi.ResourceProvider;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import java.io.File;
import java.io.IOException;
import java.util.ServiceLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class EksResourceTest {

  @Mock private DockerHelper mockDockerHelper;

  @Mock private SimpleHttpClient httpClient;

  @Test
  void testEks(@TempDir File tempFolder) throws IOException {
    File mockK8sTokenFile = new File(tempFolder, "k8sToken");
    String token = "token123";
    Files.write(token.getBytes(Charsets.UTF_8), mockK8sTokenFile);
    File mockK8sKeystoreFile = new File(tempFolder, "k8sCert");
    String truststore = "truststore123";
    Files.write(truststore.getBytes(Charsets.UTF_8), mockK8sKeystoreFile);

    when(httpClient.fetchString(any(), Mockito.eq(K8S_SVC_URL + AUTH_CONFIGMAP_PATH), any(), any()))
        .thenReturn("not empty");
    when(httpClient.fetchString(any(), Mockito.eq(K8S_SVC_URL + CW_CONFIGMAP_PATH), any(), any()))
        .thenReturn("{\"data\":{\"cluster.name\":\"my-cluster\"}}");
    when(mockDockerHelper.getContainerId()).thenReturn("0123456789A");

    Resource eksResource =
        EksResource.buildResource(
            httpClient,
            mockDockerHelper,
            mockK8sTokenFile.getPath(),
            mockK8sKeystoreFile.getPath());
    Attributes attributes = eksResource.getAttributes();

    assertThat(eksResource.getSchemaUrl()).isEqualTo(ResourceAttributes.SCHEMA_URL);
    assertThat(attributes)
        .containsOnly(
            entry(ResourceAttributes.CLOUD_PROVIDER, "aws"),
            entry(ResourceAttributes.CLOUD_PLATFORM, "aws_eks"),
            entry(ResourceAttributes.K8S_CLUSTER_NAME, "my-cluster"),
            entry(ResourceAttributes.CONTAINER_ID, "0123456789A"));
  }

  @Test
  void testNotEks() {
    Resource eksResource = EksResource.buildResource(httpClient, mockDockerHelper, "", "");
    Attributes attributes = eksResource.getAttributes();
    assertThat(attributes).isEmpty();
  }

  @Test
  void inServiceLoader() {
    // No practical way to test the attributes themselves so at least check the service loader picks
    // it up.
    assertThat(ServiceLoader.load(ResourceProvider.class))
        .anyMatch(EksResourceProvider.class::isInstance);
  }
}
