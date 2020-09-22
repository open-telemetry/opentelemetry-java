/*
 * Copyright 2020, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.sdk.extensions.trace.aws.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import io.opentelemetry.common.Attributes;
import io.opentelemetry.sdk.resources.ResourceAttributes;
import io.opentelemetry.sdk.resources.ResourceProvider;
import java.util.ServiceLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class EksResourceTest {
  @Mock private EksHelper mockEksHelper;

  @Mock private DockerHelper mockDockerHelper;

  @Test
  void testCreateAttributes() {
    when(mockDockerHelper.getContainerId()).thenReturn("0123456789A");
    when(mockEksHelper.isEks()).thenReturn(true);
    when(mockEksHelper.getClusterName()).thenReturn("my-cluster");

    EksResource populator = new EksResource(mockDockerHelper, mockEksHelper);
    Attributes attributes = populator.getAttributes();
    assertThat(attributes)
        .isEqualTo(
            Attributes.of(
                ResourceAttributes.K8S_CLUSTER,
                "my-cluster",
                ResourceAttributes.CONTAINER_ID,
                "0123456789A"));
  }

  @Test
  void testNotEks() {
    when(mockEksHelper.isEks()).thenReturn(false);

    EksResource populator = new EksResource(mockDockerHelper, mockEksHelper);
    Attributes attributes = populator.getAttributes();
    assertThat(attributes.isEmpty()).isTrue();
  }

  @Test
  void inServiceLoader() {
    // No practical way to test the attributes themselves so at least check the service loader picks
    // it up.
    assertThat(ServiceLoader.load(ResourceProvider.class)).anyMatch(EksResource.class::isInstance);
  }
}
