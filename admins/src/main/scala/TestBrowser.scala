//import com.hazelcast.client.HazelcastClient
//import com.hazelcast.core.{Message, MessageListener, ItemEvent, ItemListener}
//import com.vaadin.annotations.Push
//import us.woop.pinger.data.ParsedPongs.ParsedMessage
////import us.woop.pinger.data.Stuff.Server
//import us.woop.pinger.service.individual.ServerMonitor.{ServerStateChanged, ServerState}
//import vaadin.scala._
//
//import scala.util.Try
//
//class TestBrowser extends UI(pushMode = PushMode.Automatic) {
////  val hazelcast = HazelcastClient.newHazelcastClient()
////
////  val serversSet = hazelcast.getSet[String]("servers")
////  val statusMap = hazelcast.getMap[Server, ServerState]("server-states")
////  val stateChanges = hazelcast.getTopic[ServerStateChanged]("server-states-changes")
////  val parsedMessages = hazelcast.getTopic[ParsedMessage]("parsed-messages")
////  val receivedBytes = hazelcast.getTopic[SerializableBytes]("received-bytes")
////
////  content = {
////    new VerticalLayout {
////      sizeFull()
//////      add(new HorizontalLayout {
//////        val serverField = add(new TextField {
//////          caption = "Add new server"
//////        })
//////
//////        add(serverField)
//////        add(Button("Add server",
//////          for { v <- serverField.value }
//////            serversSet.add(v)
//////        ))
//////      })
//////      add(Button("Click meAAAA!", Notification.show("Hello, Scaladin!")))
//////      add(Button("Click meBBBB!", Notification.show("Hello, Scaladin!")))
////      add{
////
////        val table = new Table() {
////          sizeFull()
////          addContainerProperty("Server", classOf[String], None)
////          addContainerProperty("Last status", classOf[String], None)
////          addContainerProperty("Last parsed message",  classOf[String], None)
////          addContainerProperty("Last client", classOf[String], None)
////          addContainerProperty("Remove", classOf[com.vaadin.ui.Button], Option {
////            val btn = Button("Remove",
////              (e) => {
////                println(e.getComponent.parent)
////              })
////
////            btn.p
////          })
////          import collection.JavaConverters._
////
////
////          for {
////            (server, status) <- statusMap.asScala
//////            server <- serversSet.asScala
////          }
////          {
////
////            val item = addItem(server.toString).get
////            item.getProperty("Server").value = server.toString
////            item.getProperty("Last status").value = status.toString
////          }
////        }
////
////        val serverSetListener = serversSet.addItemListener(new ItemListener[String] {
////          override def itemAdded(item: ItemEvent[String]): Unit = {
////            table.addItem(item.getItem).get.getProperty("Server").value = item.getItem
////          }
////          override def itemRemoved(item: ItemEvent[String]): Unit = {
////            table.removeItem(item.getItem)
////          }
////        }, true)
////
////        detachListeners.add(event => {
////          serversSet.removeItemListener(serverSetListener)
////        })
////
////
////        val parsedMessageListener = parsedMessages.addMessageListener(new MessageListener[ParsedMessage] {
////          override def onMessage(message: Message[ParsedMessage]): Unit = {
////            ui.access{
////                val pm= message.getMessageObject
////                val serverId = pm.server.toString
////
////                val item = Option(table.getItem(serverId)).orElse {
////                  table.addItem(serverId)
////                } foreach { i =>
////                  i.getProperty("Server").value = serverId
////                  i.getProperty("Last parsed message").value = pm.toString
////                  //              i.getProperty("Server").value = serverId
////                  //              i.getProperty("Server").value = serverId
////                  //              i.getProperty("Last parsed message").value = pm.toString
////                  i.getProperty("Remove").value = {
////                    val btn = Button("Remove",
////                      (e) => {
////                        println(e.getComponent.parent)
////                      })
////
////                    btn.p
////                  }
////                }
////
////                ui.push()
////              }
////          }
////        })
////
////
////        detachListeners.add(event => {
////          parsedMessages.removeMessageListener(parsedMessageListener)
////        })
////
////table
////      }
////    }
//////    Button("Click me!", Notification.show("Hello, Scaladin!"))
////  }
//}