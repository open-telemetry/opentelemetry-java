/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.trace;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.io.SegmentedStringWriter;
import com.google.protobuf.util.JsonFormat;
import com.google.rpc.Status;
import io.opentelemetry.exporter.otlp.internal.HexEncodingStringJsonGenerator;
import io.opentelemetry.exporter.otlp.trace.OtlpHttpSpanExporter.RequestResponseHandler;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.tls.HandshakeCertificates;
import okio.BufferedSink;
import okio.GzipSink;
import okio.Okio;
import org.curioswitch.common.protobuf.json.MessageMarshaller;

final class OtlpHttpUtil {

  static final RequestResponseHandler PROTOBUF_REQUEST_RESPONSE_HANDLER =
      new ProtobufRequestResponseHandler();
  static final RequestResponseHandler JSON_REQUEST_RESPONSE_HANDLER =
      new JsonRequestResponseHandler();
  static final Interceptor GZIP_INTERCEPTOR = new GzipInterceptor();

  /**
   * Extract X.509 certificates from the bytes.
   *
   * @param trustedCertificatesPem bytes containing an X.509 certificate collection in PEM format.
   * @return a HandshakeCertificates with the certificates
   * @throws CertificateException if an error occurs extracting certificates
   */
  static HandshakeCertificates toHandshakeCertificates(byte[] trustedCertificatesPem)
      throws CertificateException {
    InputStream is = new ByteArrayInputStream(trustedCertificatesPem);
    CertificateFactory factory = CertificateFactory.getInstance("X.509");
    HandshakeCertificates.Builder certBuilder = new HandshakeCertificates.Builder();
    X509Certificate cert;
    while ((cert = (X509Certificate) factory.generateCertificate(is)) != null) {
      certBuilder.addTrustedCertificate(cert);
    }
    return certBuilder.build();
  }

  private static class ProtobufRequestResponseHandler implements RequestResponseHandler {

    @Override
    public RequestBody build(ExportTraceServiceRequest exportTraceServiceRequest) {
      return RequestBody.create(
          exportTraceServiceRequest.toByteArray(), MediaType.parse("application/x-protobuf"));
    }

    @Override
    public Status extractErrorStatus(ResponseBody responseBody) throws IOException {
      return Status.parseFrom(responseBody.bytes());
    }
  }

  private static class JsonRequestResponseHandler implements RequestResponseHandler {

    private static final JsonFactory JSON_FACTORY = new JsonFactory();
    private static final MessageMarshaller MARSHALLER =
        MessageMarshaller.builder()
            .register(ExportTraceServiceRequest.class)
            .omittingInsignificantWhitespace(true)
            .build();

    @Override
    public RequestBody build(ExportTraceServiceRequest exportTraceServiceRequest) {
      SegmentedStringWriter sw = new SegmentedStringWriter(JSON_FACTORY._getBufferRecycler());
      try (JsonGenerator gen = HexEncodingStringJsonGenerator.create(sw, JSON_FACTORY)) {
        MARSHALLER.writeValue(exportTraceServiceRequest, gen);
      } catch (IOException e) {
        // Shouldn't happen in practice, just skip it.
      }

      return RequestBody.create(sw.getAndClear(), MediaType.parse("application/json"));
    }

    @Override
    public Status extractErrorStatus(ResponseBody responseBody) throws IOException {
      Status.Builder builder = Status.newBuilder();
      JsonFormat.parser().ignoringUnknownFields().merge(responseBody.charStream(), builder);
      return builder.build();
    }
  }

  private static class GzipInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
      Request originalRequest = chain.request();
      Request compressedRequest =
          originalRequest
              .newBuilder()
              .header("Content-Encoding", "gzip")
              .method(originalRequest.method(), gzipCompress(originalRequest.body()))
              .build();
      return chain.proceed(compressedRequest);
    }

    private static RequestBody gzipCompress(RequestBody body) {
      return new RequestBody() {
        @Override
        public MediaType contentType() {
          return body.contentType();
        }

        @Override
        public long contentLength() {
          return -1;
        }

        @Override
        public void writeTo(BufferedSink bufferedSink) throws IOException {
          BufferedSink gzipSink = Okio.buffer(new GzipSink(bufferedSink));
          body.writeTo(gzipSink);
          gzipSink.close();
        }
      };
    }
  }

  private OtlpHttpUtil() {}
}
