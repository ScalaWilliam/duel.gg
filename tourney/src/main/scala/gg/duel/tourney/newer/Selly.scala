package gg.duel.tourney.newer


/**
 * Created by William on 23/01/2015.
 */
object Selly extends App {
  // "org.seleniumhq.selenium" % "selenium-java" % "2.44.0"
  import org.openqa.selenium.firefox.FirefoxDriver
  import org.openqa.selenium.{OutputType, TakesScreenshot}
  val driver = new FirefoxDriver()
  val ha = driver.get("http://inkflash.com/")
  Thread.sleep(5000L)
  driver.findElementById("btnBeginBrowsing").click()
  Thread.sleep(5000L)
  val scrFile = driver.getScreenshotAs(OutputType.FILE)
  println(scrFile)
//  driver.close()
}
