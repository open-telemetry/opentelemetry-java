allprojects {
  val release = findProperty("otel.release")
  if (release != null) {
    version = "1.14.0-" + release + "-SNAPSHOT"
  } else {
    version = "1.14.0-SNAPSHOT"
  }
}
