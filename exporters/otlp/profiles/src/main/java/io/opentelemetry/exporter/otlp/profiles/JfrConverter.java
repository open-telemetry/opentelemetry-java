package io.opentelemetry.exporter.otlp.profiles;

import jdk.jfr.consumer.RecordingFile;
import java.io.File;
import java.io.IOException;

public class JfrConverter {

  public static void main(String[] args) throws IOException {

    File jfrFile = new File(args[0]);
    RecordingFile recordingFile = new RecordingFile(jfrFile.toPath());

  }
}
