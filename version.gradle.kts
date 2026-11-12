val snapshot = true
val apidiffBaselineVersion = "1.68.0"

allprojects {
  var ver = "1.69.0"
  val release = findProperty("otel.release")
  if (release != null) {
    ver += "-" + release
  }
  if (snapshot) {
    ver += "-SNAPSHOT"
  }
  version = ver
  extra["apidiffBaselineVersion"] = apidiffBaselineVersion
}
