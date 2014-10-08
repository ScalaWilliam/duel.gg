package plugins

import com.hazelcast.core.Hazelcast
import play.api._
class HazelcastPlugin(implicit app: Application) extends Plugin {

  lazy val hazelcast = {
    Hazelcast.newHazelcastInstance()
  }

  override def onStop(): Unit = {
    hazelcast.shutdown()
  }

}
object HazelcastPlugin {
  def hazelcastPlugin: HazelcastPlugin = Play.current.plugin[HazelcastPlugin]
    .getOrElse(throw new RuntimeException("HazelcastPlugin plugin not loaded"))
}