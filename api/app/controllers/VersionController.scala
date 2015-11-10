package controllers

import play.api.mvc.{Action, Controller}

object VersionController extends Controller {
  def getVersion = Action {
    {
      for {
        cl <- Option(getClass.getClassLoader)
        mf <- Option(cl.getResourceAsStream("META-INF/MANIFEST.MF"))
        manifest = new java.util.jar.Manifest(mf)
        attributes <- Option(manifest.getMainAttributes)
        gitVersion <- Option(attributes.getValue("Git-Head-Rev"))
      } yield gitVersion
    } match {
      case Some(version) => Ok(version)
      case None => NotFound("Not found - maybe you're developing?")
    }
  }
}
