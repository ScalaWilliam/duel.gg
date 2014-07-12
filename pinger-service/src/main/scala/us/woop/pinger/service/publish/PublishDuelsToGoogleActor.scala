package us.woop.pinger.service.publish

class PublishDuelsToGoogleActor {

}

object PublishDuelsToGoogleActor extends App {
//
//  import scala.collection.JavaConverters._
//
//  val secrets = new GoogleClientSecrets().setWeb(new JacksonFactory().fromReader(new InputStreamReader(getClass.getResourceAsStream("/client_secrets.json")), classOf[GoogleClientSecrets.Details]))
//
//  val credential = new GoogleCredential.Builder()
//    .setServiceAccountScopes(Vector(DatastoreScopes.DATASTORE).asJava)
//    .setJsonFactory(new JacksonFactory)
//    .setTransport(new NetHttpTransport)
//    .setClientSecrets(secrets)
//    .setServiceAccountPrivateKey(secrets.getDetails.)
//    .build()
//
//  val datastore =
//    new Datastore.Builder(new NetHttpTransport, new JacksonFactory, credential).build()
//
//  //"platinum-logic-639"
//
////  val key = Key.newBuilder().addPathElement(Key.PathElement.newBuilder().setKind("whut").build())
////
////  val entity = Entity.newBuilder()
////    .setKey(key)
////    .addProperty(
////      Property.newBuilder()
////        .setName("question")
////        .setValue(Value.newBuilder().setStringValue("Meaning of Life?"))
////    )
////    .addProperty(
////      Property.newBuilder()
////        .setName("answer")
////        .setValue(Value.newBuilder().setIntegerValue(42))
////    )
////    .build()
////
////  val mutation = Mutation.newBuilder().addInsertAutoId(entity)
////
////  val creq = CommitRequest.newBuilder()
////    .setMutation(mutation)
////    .setMode(CommitRequest.Mode.NON_TRANSACTIONAL)
////    .build()
//
////  val kk = datastore.commit(creq)
//
//  val datasetId = "platinum-logic-639"
////  val datasetId = "duelgg-1357as9"
//
////  val res = datastore.datasets().lookup(datasetId, new LookupRequest).asScala
//
//  val ent = new Entity().setKey(new Key().set("wat", "wut")).set("ok", "good")
//
//  val mutation = new Mutation().setInsertAutoId(Vector(ent).asJava)
//
//  val cr = new CommitRequest().setMutation(mutation)
//
//  println(cr)
//
////  val out = datastore.datasets().commit(datasetId, cr).execute()
//  val out = datastore.datasets().lookup(datasetId, new LookupRequest()).execute()
//
//  out.toString
//
////  println(res)
//
//  println(out)


}