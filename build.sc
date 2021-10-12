import mill._
import mill.scalalib._
import mill.scalalib.publish._
import mill.eval.Evaluator

trait SpinalModule extends ScalaModule{
  def scalaVersion = "2.11.12"

  override def ivyDeps = Agg(
    ivy"com.github.spinalhdl::spinalhdl-core:1.6.0",
    ivy"com.github.spinalhdl::spinalhdl-lib:1.6.0"
  )

  override def scalacPluginIvyDeps = Agg(ivy"com.github.spinalhdl::spinalhdl-idsl-plugin:1.6.0")

  object test extends Tests {
    // dependencies for testing
    override def ivyDeps = Agg(
      ivy"org.scalactic::scalactic:2.2.1",
      ivy"org.scalatest::scalatest:2.2.1"
    )
    def testFrameworks = Seq("org.scalatest.tools.Framework")
  }

  def idea(ev: Evaluator) = T.command{
    mill.scalalib.GenIdea.idea(ev)
  }
}

object demo extends SpinalModule with PublishModule {
  override def mainClass = Some("demo.play1")
  def publishVersion = "0.1"

  //POM(Project Object Model)
  override def pomSettings = PomSettings(
    description = "spinal demo",
    organization = "",
    url = "https://github.com/jijingg",
    licenses = Seq(License.`LGPL-3.0-or-later`),
    versionControl = VersionControl.github("jijing.gjj"),
    developers = Seq(
      Developer("Jijing Guo", "goco.v@163.com")
    )
  )
}
