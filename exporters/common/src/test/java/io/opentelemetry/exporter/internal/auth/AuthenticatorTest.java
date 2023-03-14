/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.auth;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.opentelemetry.exporter.internal.grpc.GrpcExporterBuilder;
import io.opentelemetry.exporter.internal.grpc.MarshalerServiceStub;
import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.internal.okhttp.OkHttpExporterBuilder;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;

class AuthenticatorTest {

  @Test
  void getHeaders() {
    Map<String, String> input = new HashMap<>();
    input.put("key1", "value1");
    input.put("key2", "value2");

    Authenticator authenticator = () -> new HashMap<>(input);
    assertThat(authenticator.getHeaders()).isEqualTo(input);
  }

  @Test
  void okHttpSetAuthenticatorOnDelegate_Success() {
    OkHttpExporterBuilder<?> builder =
        new OkHttpExporterBuilder<>("otlp", "test", "http://localhost:4318/test");

    assertThat(builder).extracting("authenticator").isNull();

    Authenticator authenticator = Collections::emptyMap;

    Authenticator.setAuthenticatorOnDelegate(new WithDelegate(builder), authenticator);

    assertThat(builder)
        .extracting("authenticator", as(InstanceOfAssertFactories.type(Authenticator.class)))
        .isSameAs(authenticator);
  }

  @Test
  @SuppressWarnings("unchecked")
  void grpcSetAuthenticatorOnDelegate_Success() {
    Supplier<BiFunction<Channel, String, MarshalerServiceStub<Marshaler, ?, ?>>> grpcStubFactory =
        mock(Supplier.class);
    when(grpcStubFactory.get())
        .thenReturn(
            (c, s) -> new AuthenticatorTest.TestMarshalerServiceStub(c, CallOptions.DEFAULT));
    GrpcExporterBuilder<?> builder =
        new GrpcExporterBuilder<>(
            "otlp", "test", 0, URI.create("http://localhost:4317"), grpcStubFactory, "/test");

    assertThat(builder).extracting("authenticator").isNull();

    Authenticator authenticator = Collections::emptyMap;

    Authenticator.setAuthenticatorOnDelegate(new WithDelegate(builder), authenticator);

    assertThat(builder)
        .extracting("authenticator", as(InstanceOfAssertFactories.type(Authenticator.class)))
        .isSameAs(authenticator);
  }

  @Test
  void setAuthenticatorOnDelegate_Fail() {
    Authenticator authenticator = Collections::emptyMap;

    assertThatThrownBy(() -> Authenticator.setAuthenticatorOnDelegate(new Object(), authenticator))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(
            () ->
                Authenticator.setAuthenticatorOnDelegate(
                    new WithDelegate(new Object()), authenticator))
        .isInstanceOf(IllegalArgumentException.class);
  }

  private final class TestMarshalerServiceStub
      extends MarshalerServiceStub<Marshaler, Void, AuthenticatorTest.TestMarshalerServiceStub> {

    private TestMarshalerServiceStub(Channel channel, CallOptions callOptions) {
      super(channel, callOptions);
    }

    @Override
    protected AuthenticatorTest.TestMarshalerServiceStub build(
        Channel channel, CallOptions callOptions) {
      return new AuthenticatorTest.TestMarshalerServiceStub(channel, callOptions);
    }

    @Override
    public ListenableFuture<Void> export(Marshaler request) {
      return Futures.immediateVoidFuture();
    }
  }

  @SuppressWarnings({"UnusedVariable", "FieldCanBeLocal"})
  private static class WithDelegate {

    private final Object delegate;

    private WithDelegate(Object delegate) {
      this.delegate = delegate;
    }
  }
}
