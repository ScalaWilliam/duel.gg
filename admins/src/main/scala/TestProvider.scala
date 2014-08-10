import com.vaadin.server.UIProviderEvent
import vaadin.scala.server.ScaladinUIProvider

class TestProvider extends ScaladinUIProvider {
  protected def createScaladinUiInstance(e: UIProviderEvent) = new TestBrowser()
}
