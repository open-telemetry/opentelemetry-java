/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.aws.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.io.Files;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.sdk.resources.ResourceAttributes;
import io.opentelemetry.sdk.resources.ResourceProvider;
import java.io.File;
import java.io.IOException;
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
  private static final String K8S_CERT_PATH =
      "/var/run/secrets/kubernetes.io/serviceaccount/ca.crt";
  private static final Logger logger = Logger.getLogger(EksResource.class.getName());

  private final JdkHttpClient jdkHttpClient;
  private final DockerHelper dockerHelper;
  private final String k8sTokenPath;
  private final String k8sKeystorePath;

  public EksResource() {
    this(new JdkHttpClient(), new DockerHelper(), K8S_TOKEN_PATH, K8S_CERT_PATH);
  }

  @VisibleForTesting
  EksResource(
      JdkHttpClient jdkHttpClient,
      DockerHelper dockerHelper,
      String k8sTokenPath,
      String k8sKeystorePath) {
    this.jdkHttpClient = jdkHttpClient;
    this.dockerHelper = dockerHelper;
    this.k8sTokenPath = k8sTokenPath;
    this.k8sKeystorePath = k8sKeystorePath;
  }

  @Override
  protected Attributes getAttributes() {
    if (!isEks()) {
      return Attributes.empty();
    }

    AttributesBuilder attrBuilders = Attributes.builder();

    String clusterName = getClusterName();
    if (!Strings.isNullOrEmpty(clusterName)) {
      attrBuilders.put(ResourceAttributes.K8S_CLUSTER, clusterName);
    }

    String containerId = dockerHelper.getContainerId();
    if (!Strings.isNullOrEmpty(containerId)) {
      attrBuilders.put(ResourceAttributes.CONTAINER_ID, containerId);
    }

    return attrBuilders.build();
  }

  private boolean isEks() {
    if (!isK8s()) {
      logger.log(Level.FINE, "Not running on k8s.");
      return false;
    }

    Map<String, String> requestProperties = new HashMap<>();
    requestProperties.put("Authorization", getK8sCredHeader());
    String awsAuth =
        jdkHttpClient.fetchString(
            "GET", K8S_SVC_URL + AUTH_CONFIGMAP_PATH, requestProperties, K8S_CERT_PATH);

    return !Strings.isNullOrEmpty(awsAuth);
  }

  private boolean isK8s() {
    File k8sTokeyFile = new File(this.k8sTokenPath);
    File k8sKeystoreFile = new File(this.k8sKeystorePath);
    return k8sTokeyFile.exists() && k8sKeystoreFile.exists();
  }

  private String getClusterName() {
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
      File file = new File(K8S_TOKEN_PATH);
      String content = Files.asCharSource(file, Charsets.UTF_8).read();
      return "Bearer " + content;
    } catch (IOException e) {
      logger.log(Level.WARNING, "Unable to load K8s client token.", e);
    }
    return "";
  }
}
