/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.aws.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A factory for a {@link Resource} which provides information about the current ECS container if
 * running on AWS EKS.
 */
public final class EksResource {
  private static final Logger logger = Logger.getLogger(EksResource.class.getName());

  static final String K8S_SVC_URL = "https://kubernetes.default.svc";
  static final String AUTH_CONFIGMAP_PATH = "/api/v1/namespaces/kube-system/configmaps/aws-auth";
  static final String CW_CONFIGMAP_PATH =
      "/api/v1/namespaces/amazon-cloudwatch/configmaps/cluster-info";
  private static final String K8S_TOKEN_PATH =
      "/var/run/secrets/kubernetes.io/serviceaccount/token";
  private static final String K8S_CERT_PATH =
      "/var/run/secrets/kubernetes.io/serviceaccount/ca.crt";

  private static final Resource INSTANCE = buildResource();

  /**
   * Returns a factory for a {@link Resource} which provides information about the current ECS
   * container if running on AWS EKS.
   */
  public static Resource get() {
    return INSTANCE;
  }

  private static Resource buildResource() {
    return buildResource(new JdkHttpClient(), new DockerHelper(), K8S_TOKEN_PATH, K8S_CERT_PATH);
  }

  // Visible for testing
  static Resource buildResource(
      JdkHttpClient jdkHttpClient,
      DockerHelper dockerHelper,
      String k8sTokenPath,
      String k8sKeystorePath) {
    if (!isEks(k8sTokenPath, k8sKeystorePath, jdkHttpClient)) {
      return Resource.empty();
    }

    AttributesBuilder attrBuilders = Attributes.builder();

    String clusterName = getClusterName(jdkHttpClient);
    if (clusterName != null && !clusterName.isEmpty()) {
      attrBuilders.put(ResourceAttributes.K8S_CLUSTER_NAME, clusterName);
    }

    String containerId = dockerHelper.getContainerId();
    if (containerId != null && !containerId.isEmpty()) {
      attrBuilders.put(ResourceAttributes.CONTAINER_ID, containerId);
    }

    return Resource.create(attrBuilders.build(), ResourceAttributes.SCHEMA_URL);
  }

  private static boolean isEks(
      String k8sTokenPath, String k8sKeystorePath, JdkHttpClient jdkHttpClient) {
    if (!isK8s(k8sTokenPath, k8sKeystorePath)) {
      logger.log(Level.FINE, "Not running on k8s.");
      return false;
    }

    Map<String, String> requestProperties = new HashMap<>();
    requestProperties.put("Authorization", getK8sCredHeader());
    String awsAuth =
        jdkHttpClient.fetchString(
            "GET", K8S_SVC_URL + AUTH_CONFIGMAP_PATH, requestProperties, K8S_CERT_PATH);

    return awsAuth != null && !awsAuth.isEmpty();
  }

  private static boolean isK8s(String k8sTokenPath, String k8sKeystorePath) {
    File k8sTokeyFile = new File(k8sTokenPath);
    File k8sKeystoreFile = new File(k8sKeystorePath);
    return k8sTokeyFile.exists() && k8sKeystoreFile.exists();
  }

  private static String getClusterName(JdkHttpClient jdkHttpClient) {
    Map<String, String> requestProperties = new HashMap<>();
    requestProperties.put("Authorization", getK8sCredHeader());
    String json =
        jdkHttpClient.fetchString(
            "GET", K8S_SVC_URL + CW_CONFIGMAP_PATH, requestProperties, K8S_CERT_PATH);

    try {
      ObjectMapper mapper = new ObjectMapper();
      return mapper.readTree(json).at("/data/cluster.name").asText();
    } catch (JsonProcessingException e) {
      logger.log(Level.WARNING, "Can't get cluster name on EKS.", e);
    }
    return "";
  }

  private static String getK8sCredHeader() {
    try {
      String content =
          new String(Files.readAllBytes(Paths.get(K8S_TOKEN_PATH)), StandardCharsets.UTF_8);
      return "Bearer " + content;
    } catch (IOException e) {
      logger.log(Level.WARNING, "Unable to load K8s client token.", e);
    }
    return "";
  }

  private EksResource() {}
}
