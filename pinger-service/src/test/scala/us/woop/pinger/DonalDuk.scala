package us.woop.pinger

import com.hazelcast.client.HazelcastClient
import com.hazelcast.client.config.ClientConfig
import com.hazelcast.core.{Message, MessageListener}

object DonalDuk extends App {

  val clientConfig = new ClientConfig()
  clientConfig.getGroupConfig.setName("dev").setPassword("WTF")
  clientConfig.getNetworkConfig.addAddress("scala.contractors", "scalawilliam.com")
  val client = HazelcastClient.newHazelcastClient(clientConfig)
  import scala.collection.JavaConverters._
  client.getCluster.getMembers.asScala foreach println
  val topic = client.getTopic[String]("what")
  topic.addMessageListener(new MessageListener[String] {
    def onMessage(message: Message[String]): Unit = {
      println(s"Got message ${message.getMessageObject}, ${message.getPublishingMember}")
    }
  })

  topic.publish("YAY")

//
//  val jsch = new JSch
//  jsch.addIdentity(new File(scala.util.Properties.userHome, ".ssh/id_rsa").getCanonicalPath)
//  val session = jsch.getSession("win", "scalawilliam.com")
//  session.setConfig("StrictHostKeyChecking", "no")
//  session.connect(5000)
//
//  val channel = session.openChannel("exec").asInstanceOf[ChannelExec]
//  channel.setCommand("ps ax")
//  channel.setInputStream(null)
//  channel.setErrStream(System.err)
//  channel.setOutputStream(System.out)
//  channel.connect()
//  scala.io.Source.fromInputStream(channel.getInputStream).getLines() foreach println
//  channel.disconnect()
//  session.disconnect()

//
//  val config = new Config()
//  val networkConfig = config.getNetworkConfig
//  val joinConfig = networkConfig.getJoin
//  val tcpConfig = joinConfig.getTcpIpConfig
//  tcpConfig.addMember()
//
//  val hi = Hazelcast.newHazelcastInstance(new XmlConfigBuilder().getProperties.build())
//  hi.getQueue()
//  hi.getCluster.addMembershipListener()
//  val topic = hi.getTopic[Int]("Wat")
//  topic.addMessageListener(new MessageListener[Int] {
//    def onMessage(message: Message[Int]): Unit = {
//      println(message)
//      message.
//    }
//  })
//  topic.publish(123412)
}
