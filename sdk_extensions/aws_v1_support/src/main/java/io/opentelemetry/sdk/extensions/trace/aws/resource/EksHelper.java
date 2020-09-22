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

import com.google.common.annotations.VisibleForTesting;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1ConfigMap;
import io.kubernetes.client.openapi.models.V1ConfigMapList;
import io.kubernetes.client.util.Config;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

class EksHelper {

  private static final Logger logger = Logger.getLogger(EksHelper.class.getName());

  private static final String NAMESPACE_KUBE_SYSTEM = "kube-system";
  private static final String NAMESPACE_AWS_CLOUDWATCH = "amazon-cloudwatch";
  private static final String EKS_CONFIGMAP = "aws-auth";
  private static final String CLOUDWATCH_CONFIGMAP = "cluster-info";
  private static final String CLUSTER_NAME_KEY = "cluster.name";

  @Nullable private final CoreV1Api k8sApi;

  public EksHelper() {
    CoreV1Api k8sApi = null;
    try {
      ApiClient client = Config.defaultClient();
      Configuration.setDefaultApiClient(client);
      k8sApi = new CoreV1Api();
    } catch (IOException e) {
      logger.log(Level.WARNING, "Failed to init k8s client API, sdk is not running on EKS.");
    }
    this.k8sApi = k8sApi;
  }

  @VisibleForTesting
  public EksHelper(CoreV1Api k8sApi) {
    this.k8sApi = k8sApi;
  }

  boolean isEks() {
    if (k8sApi == null) {
      return false;
    }

    try {
      V1ConfigMapList configMapList =
          k8sApi.listNamespacedConfigMap(
              NAMESPACE_KUBE_SYSTEM, null, null, null, null, null, null, null, null, null);

      if (configMapList == null) {
        logger.log(Level.INFO, "Failed to get configmap of namespace %s.", NAMESPACE_KUBE_SYSTEM);
        return false;
      }

      for (V1ConfigMap item : configMapList.getItems()) {
        if (item == null || item.getMetadata() == null) {
          continue;
        }

        if (EKS_CONFIGMAP.equals(item.getMetadata().getName())) {
          return true;
        }
      }

    } catch (ApiException e) {
      logger.log(Level.WARNING, "Failed to get configmap of namespace %s.", NAMESPACE_KUBE_SYSTEM);
    }
    return false;
  }

  String getClusterName() {
    if (k8sApi == null) {
      return "";
    }

    try {
      V1ConfigMapList configMapList =
          k8sApi.listNamespacedConfigMap(
              NAMESPACE_AWS_CLOUDWATCH, null, null, null, null, null, null, null, null, null);

      if (configMapList == null) {
        logger.log(
            Level.INFO, "Failed to get configmap of namespace %s.", NAMESPACE_AWS_CLOUDWATCH);
        return "";
      }

      for (V1ConfigMap item : configMapList.getItems()) {
        if (item == null || item.getMetadata() == null || item.getData() == null) {
          continue;
        }

        if (!CLOUDWATCH_CONFIGMAP.equals(item.getMetadata().getName())) {
          continue;
        }

        String clusterName = item.getData().get(CLUSTER_NAME_KEY);
        return clusterName == null ? "" : clusterName;
      }

    } catch (ApiException e) {
      logger.log(
          Level.WARNING, "Failed to get configmap of namespace %s.", NAMESPACE_AWS_CLOUDWATCH);
    }
    return "";
  }
}
