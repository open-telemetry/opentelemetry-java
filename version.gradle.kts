val snapshot = true

allprojects {
  val release = findProperty("otel.release")
  if (release != null) {
    version = "1.14.0-" + release
  } else {
    version = "1.14.0"
  }
  if (snapshot) {
    version += "-SNAPSHOT"
  }
}
