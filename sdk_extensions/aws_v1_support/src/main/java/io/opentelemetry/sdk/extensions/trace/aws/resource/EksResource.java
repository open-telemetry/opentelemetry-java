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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import io.opentelemetry.common.Attributes;
import io.opentelemetry.sdk.resources.ResourceAttributes;
import io.opentelemetry.sdk.resources.ResourceProvider;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EksResource extends ResourceProvider {

  static final String K8S_SVC_URL = "https://kubernetes.default.svc";
  static final String AUTH_CONFIGMAP_PATH = "/api/v1/namespaces/kube-system/configmaps/aws-auth";
  static final String CW_CONFIGMAP_PATH =
      "/api/v1/namespaces/amazon-cloudwatch/configmaps/cluster-info";
  private static final String K8S_TOKEN_PATH =
      "/var/run/secrets/kubernetes.io/serviceaccount/token";
  private static final String K8S_KEYSTORE_PATH =
      "/var/run/secrets/kubernetes.io/serviceaccount/ca.crt";
  private static final Logger logger = Logger.getLogger(EksResource.class.getName());

  private final RestfulClient restfulClient;
  private final DockerHelper dockerHelper;
  private final String k8sTokenPath;
  private final String k8sKeystorePath;

  public EksResource() {
    this(new RestfulClient(), new DockerHelper(), K8S_TOKEN_PATH, K8S_KEYSTORE_PATH);
  }

  @VisibleForTesting
  EksResource(
      RestfulClient restfulClient,
      DockerHelper dockerHelper,
      String k8sTokenPath,
      String k8sKeystorePath) {
    this.restfulClient = restfulClient;
    this.dockerHelper = dockerHelper;
    this.k8sTokenPath = k8sTokenPath;
    this.k8sKeystorePath = k8sKeystorePath;
  }

  @Override
  protected Attributes getAttributes() {
    if (!isEks()) {
      return Attributes.empty();
    }

    logger.log(Level.INFO, "Retrieving aws eks attributes.");
    Attributes.Builder attrBuilders = Attributes.newBuilder();

    String clusterName = getClusterName();
    if (!Strings.isNullOrEmpty(clusterName)) {
      attrBuilders.setAttribute(ResourceAttributes.K8S_CLUSTER, clusterName);
    }
    logger.log(Level.INFO, String.format("clusterName %s", clusterName));

    String containerId = dockerHelper.getContainerId();
    if (!Strings.isNullOrEmpty(containerId)) {
      attrBuilders.setAttribute(ResourceAttributes.CONTAINER_ID, containerId);
    }
    logger.log(Level.INFO, String.format("containerId %s", containerId));

    return attrBuilders.build();
  }

  private boolean isEks() {
    if (!isK8s()) {
      logger.log(Level.INFO, "Not running on k8s.");
      return false;
    }

    Map<String, String> requestProperties = new HashMap<>();
    requestProperties.put("Authorization", restfulClient.getK8sCredHeader(K8S_TOKEN_PATH));
    String awsAuth =
        restfulClient.fetchString(
            "GET", K8S_SVC_URL + AUTH_CONFIGMAP_PATH, requestProperties, K8S_KEYSTORE_PATH);

    return !Strings.isNullOrEmpty(awsAuth);
  }

  private boolean isK8s() {
    File k8sTokeyFile = new File(this.k8sTokenPath);
    File k8sKeystoreFile = new File(this.k8sKeystorePath);
    return k8sTokeyFile.exists() && k8sKeystoreFile.exists();
  }

  private String getClusterName() {
    Map<String, String> requestProperties = new HashMap<>();
    requestProperties.put("Authorization", restfulClient.getK8sCredHeader(K8S_TOKEN_PATH));
    String json =
        restfulClient.fetchString(
            "GET", K8S_SVC_URL + CW_CONFIGMAP_PATH, requestProperties, K8S_KEYSTORE_PATH);

    try {
      ObjectMapper mapper = new ObjectMapper();
      return mapper.readTree(json).at("/data/cluster.name").asText();
    } catch (JsonProcessingException e) {
      logger.log(Level.WARNING, String.format("Can't get cluster name on EKS: %s", e));
    }
    return "";
  }
}
