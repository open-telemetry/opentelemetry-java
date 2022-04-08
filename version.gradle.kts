val snapshot = true

allprojects {
  var v = "1.14.0"
  val release = findProperty("otel.release")
  if (release != null) {
    v += "-" + release
  }
  if (snapshot) {
    v += "-SNAPSHOT"
  }
  version = v
}
