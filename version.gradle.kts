allprojects {
  val release = findProperty("otel.release")
  if (release != null) {
    version = "1.13.0-" + release + "-SNAPSHOT"
  } else {
    version = "1.13.0"
  }
}
