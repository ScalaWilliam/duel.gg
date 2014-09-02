package us.woop.pinger.app

import java.util

import com.hazelcast.config.Config
import com.hazelcast.core.Hazelcast
import com.hazelcast.util.FilteringClassLoader
import org.scalatest.FunSuite

class StartHazelcastTest extends FunSuite {
  test("this") {
    val hz = Hazelcast.newHazelcastInstance()
    Thread.sleep(2000)
    hz.shutdown()
  }

}
