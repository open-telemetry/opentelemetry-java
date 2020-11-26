/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.aws.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.resources.ResourceAttributes;
import io.opentelemetry.sdk.resources.ResourceProvider;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EcsResourceTest {
  private static final String ECS_METADATA_KEY_V4 = "ECS_CONTAINER_METADATA_URI_V4";
  private static final String ECS_METADATA_KEY_V3 = "ECS_CONTAINER_METADATA_URI";

  @Mock private DockerHelper mockDockerHelper;

  @Test
  void testCreateAttributes() throws UnknownHostException {
    when(mockDockerHelper.getContainerId()).thenReturn("0123456789A");
    Map<String, String> mockSysEnv = new HashMap<>();
    mockSysEnv.put(ECS_METADATA_KEY_V3, "ecs_metadata_v3_uri");
    EcsResource populator = new EcsResource(mockSysEnv, mockDockerHelper);
    Attributes attributes = populator.getAttributes();
    assertThat(attributes)
        .isEqualTo(
            Attributes.of(
                ResourceAttributes.CLOUD_PROVIDER,
                "aws",
                ResourceAttributes.CONTAINER_NAME,
                InetAddress.getLocalHost().getHostName(),
                ResourceAttributes.CONTAINER_ID,
                "0123456789A"));
  }

  @Test
  void testNotOnEcs() {
    Map<String, String> mockSysEnv = new HashMap<>();
    mockSysEnv.put(ECS_METADATA_KEY_V3, "");
    mockSysEnv.put(ECS_METADATA_KEY_V4, "");
    EcsResource populator = new EcsResource(mockSysEnv, mockDockerHelper);
    Attributes attributes = populator.getAttributes();
    assertThat(attributes.isEmpty()).isTrue();
  }

  @Test
  void testContainerIdMissing() throws UnknownHostException {
    when(mockDockerHelper.getContainerId()).thenReturn("");
    Map<String, String> mockSysEnv = new HashMap<>();
    mockSysEnv.put(ECS_METADATA_KEY_V4, "ecs_metadata_v4_uri");
    EcsResource populator = new EcsResource(mockSysEnv, mockDockerHelper);
    Attributes attributes = populator.getAttributes();
    assertThat(attributes)
        .isEqualTo(
            Attributes.of(
                ResourceAttributes.CLOUD_PROVIDER,
                "aws",
                ResourceAttributes.CONTAINER_NAME,
                InetAddress.getLocalHost().getHostName()));
  }

  @Test
  void inServiceLoader() {
    // No practical way to test the attributes themselves so at least check the service loader picks
    // it up.
    assertThat(ServiceLoader.load(ResourceProvider.class)).anyMatch(EcsResource.class::isInstance);
  }
}
