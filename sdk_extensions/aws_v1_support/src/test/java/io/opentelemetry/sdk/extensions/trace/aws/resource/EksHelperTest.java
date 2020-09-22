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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1ConfigMap;
import io.kubernetes.client.openapi.models.V1ConfigMapList;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import org.junit.Test;

public class EksHelperTest {

  @Test
  public void testEks() throws ApiException {
    V1ObjectMeta objectMeta1 = new V1ObjectMeta();
    objectMeta1.setName("aws-auth");
    V1ConfigMap configMap1 = new V1ConfigMap();
    configMap1.setMetadata(objectMeta1);

    V1ObjectMeta objectMetaA = new V1ObjectMeta();
    objectMetaA.setName("a");
    V1ConfigMap configMapA = new V1ConfigMap();
    configMapA.setMetadata(objectMetaA);

    V1ConfigMapList configMapList1 = new V1ConfigMapList();
    configMapList1.addItemsItem(configMap1);
    configMapList1.addItemsItem(configMapA);

    V1ObjectMeta objectMeta2 = new V1ObjectMeta();
    objectMeta2.setName("cluster-info");
    V1ConfigMap configMap2 = new V1ConfigMap();
    configMap2.putDataItem("b.b", "bb");
    configMap2.putDataItem("cluster.name", "my-cluster");
    configMap2.setMetadata(objectMeta2);

    V1ConfigMapList configMapList2 = new V1ConfigMapList();
    configMapList2.addItemsItem(configMap2);
    configMapList2.addItemsItem(configMapA);

    CoreV1Api mockK8sApi = mock(CoreV1Api.class);
    when(mockK8sApi.listNamespacedConfigMap(
            "kube-system", null, null, null, null, null, null, null, null, null))
        .thenReturn(configMapList1);
    when(mockK8sApi.listNamespacedConfigMap(
            "amazon-cloudwatch", null, null, null, null, null, null, null, null, null))
        .thenReturn(configMapList2);

    EksHelper eksHelper = new EksHelper(mockK8sApi);

    assertThat(eksHelper.isEks()).isTrue();
    assertThat(eksHelper.getClusterName()).isEqualTo("my-cluster");
  }

  @Test
  public void testNoAwsAuth() throws ApiException {
    CoreV1Api mockK8sApi = mock(CoreV1Api.class);
    when(mockK8sApi.listNamespacedConfigMap(
            "kube-system", null, null, null, null, null, null, null, null, null))
        .thenReturn(null);

    EksHelper eksHelper = new EksHelper(mockK8sApi);
    assertThat(eksHelper.isEks()).isFalse();
    assertThat(eksHelper.getClusterName()).isEmpty();
  }
}
