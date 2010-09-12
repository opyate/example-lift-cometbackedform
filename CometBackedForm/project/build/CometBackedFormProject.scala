
import sbt._

class CometBackedFormProject(info: ProjectInfo) extends DefaultWebProject(info) {
  val liftVersion = "2.1-M1"

  override def libraryDependencies = Set(
    "net.liftweb" % "lift-webkit_2.8.0" % liftVersion % "compile->default",
    "net.liftweb" % "lift-mapper_2.8.0" % liftVersion % "compile->default",
    "org.mortbay.jetty" % "jetty" % "6.1.25" % "test",
    "junit" % "junit" % "4.5" % "test->default",
    "org.scala-tools.testing" % "specs" % "1.6.2.1" % "test->default",
    "com.h2database" % "h2" % "1.2.138"
  ) ++ super.libraryDependencies
}
