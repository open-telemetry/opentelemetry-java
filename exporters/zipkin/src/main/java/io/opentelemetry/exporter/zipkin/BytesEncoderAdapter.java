/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.zipkin;

import zipkin2.Span;
import zipkin2.reporter.BytesEncoder;
import zipkin2.reporter.Encoding;

/**
 * This supports the deprecated method {@link
 * ZipkinSpanExporterBuilder#setEncoder(zipkin2.codec.BytesEncoder)}.
 */
final class BytesEncoderAdapter implements BytesEncoder<Span> {
  private final zipkin2.codec.BytesEncoder<Span> delegate;
  private final Encoding encoding;

  @SuppressWarnings("deprecation") // we have to use the deprecated thrift encoding to return it
  BytesEncoderAdapter(zipkin2.codec.BytesEncoder<Span> delegate) {
    this.delegate = delegate;
    switch (delegate.encoding()) {
      case JSON:
        this.encoding = Encoding.JSON;
        break;
      case PROTO3:
        this.encoding = Encoding.PROTO3;
        break;
      case THRIFT:
        this.encoding = Encoding.THRIFT;
        break;
      default:
        // Only possible if zipkin2 adds an encoding besides above, which is very unlikely.
        throw new UnsupportedOperationException("unsupported encoding " + delegate.encoding());
    }
  }

  @Override
  public Encoding encoding() {
    return encoding;
  }

  @Override
  public int sizeInBytes(Span span) {
    return delegate.sizeInBytes(span);
  }

  @Override
  public byte[] encode(Span span) {
    return delegate.encode(span);
  }

  @Override
  public String toString() {
    return delegate.toString();
  }
}
