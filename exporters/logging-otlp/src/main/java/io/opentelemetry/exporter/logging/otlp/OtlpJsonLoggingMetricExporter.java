/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging.otlp;

import static io.opentelemetry.exporter.logging.otlp.HexEncodingStringJsonGenerator.JSON_FACTORY;

import com.fasterxml.jackson.core.Base64Variant;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.io.SegmentedStringWriter;
import com.fasterxml.jackson.core.util.JsonGeneratorDelegate;
import io.opentelemetry.proto.metrics.v1.ResourceMetrics;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.extension.otproto.MetricAdapter;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.curioswitch.common.protobuf.json.MessageMarshaller;

/**
 * A {@link MetricExporter} which writes {@linkplain MetricData spans} to a {@link Logger} in OTLP
 * JSON format. Each log line will include a single {@link ResourceMetrics}.
 */
public class OtlpJsonLoggingMetricExporter implements MetricExporter {

  private static final MessageMarshaller marshaller =
      MessageMarshaller.builder()
          .register(ResourceMetrics.class)
          .omittingInsignificantWhitespace(true)
          .build();

  private static final Logger logger =
      Logger.getLogger(OtlpJsonLoggingMetricExporter.class.getName());

  /** Returns a new {@link OtlpJsonLoggingMetricExporter}. */
  public static MetricExporter create() {
    return new OtlpJsonLoggingMetricExporter();
  }

  private OtlpJsonLoggingMetricExporter() {}

  @Override
  public CompletableResultCode export(Collection<MetricData> metrics) {
    List<ResourceMetrics> allResourceMetrics = MetricAdapter.toProtoResourceMetrics(metrics);
    for (ResourceMetrics resourceMetrics : allResourceMetrics) {
      SegmentedStringWriter sw = new SegmentedStringWriter(JSON_FACTORY._getBufferRecycler());
      try (JsonGenerator gen = HexEncodingStringJsonGenerator.create(sw)) {
        marshaller.writeValue(resourceMetrics, gen);
      } catch (IOException e) {
        // Shouldn't happen in practice, just skip it.
        continue;
      }
      logger.log(Level.INFO, sw.getAndClear());
    }
    return CompletableResultCode.ofSuccess();
  }

  @Override
  public CompletableResultCode flush() {
    return CompletableResultCode.ofSuccess();
  }

  @Override
  public CompletableResultCode shutdown() {
    return CompletableResultCode.ofSuccess();
  }

  private static final class HexEncodingStringJsonGenerator extends JsonGeneratorDelegate {

    static JsonGenerator create(SegmentedStringWriter stringWriter) {
      final JsonGenerator delegate;
      try {
        delegate = JSON_FACTORY.createGenerator(stringWriter);
      } catch (IOException e) {
        throw new IllegalStateException(
            "Unable to create in-memory JsonGenerator, can't happen.", e);
      }
      return new HexEncodingStringJsonGenerator(delegate);
    }

    private HexEncodingStringJsonGenerator(JsonGenerator delegate) {
      super(delegate);
    }

    @Override
    public void writeBinary(Base64Variant b64variant, byte[] data, int offset, int len)
        throws IOException {
      writeString(bytesToHex(data, offset, len));
    }

    private static final char[] HEX_ARRAY = "0123456789abcdef".toCharArray();

    private static String bytesToHex(byte[] bytes, int offset, int len) {
      char[] hexChars = new char[len * 2];
      for (int i = 0; i < len; i++) {
        int v = bytes[offset + i] & 0xFF;
        hexChars[i * 2] = HEX_ARRAY[v >>> 4];
        hexChars[i * 2 + 1] = HEX_ARRAY[v & 0x0F];
      }
      return new String(hexChars);
    }
  }
}
