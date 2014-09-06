//import com.orientechnologies.orient.core.record.impl.ODocument
//import com.orientechnologies.orient.graph.gremlin.OGremlinHelper
//
//object ODBEx extends App {
//  val db = new OGraphDatabase("memory:database2"): OGraphDatabase
//  OGremlinHelper.global().create()
//
//  if (!db.exists()) {
//    db.create();
//
//    db.createVertexType("Student")
//    db.createVertexType("Course")
//    db.createEdgeType("Attends")
//
//    val student1 = db.createVertex("Student"): ODocument
//    student1.field("name", "John")
//    student1.save()
//
//    val student2 = db.createVertex("Student"): ODocument
//    student2.field("name", "Betty")
//    student2.save()
//
//    val course1 = db.createVertex("Course"): ODocument
//    course1.field("title", "Algebra")
//    course1.save()
//
//    val course2 = db.createVertex("Course"): ODocument
//    course2.field("title", "Literature")
//    course2.save()
//
//    val attends = db.createEdge (student1, course1, "Attends"): ODocument
//    attends.save()
//    val attends2 = db.createEdge(student2, course1, "Attends"): ODocument
//    attends2.save()
//    val attends3 = db.createEdge(student2, course2, "Attends"): ODocument
//    attends3.save()
//
//
//  } else {
//    db.open("admin", "admin")
//  }
//
//  //List<ODocument> results = db.query(new OSQLSynchQuery("select GREMLIN('current.outE.filter{it['@class']=='Attends'}.inV').title as value from Student"));
////  List<ODocument> results = db.query(new OSQLSynchQuery("select from Student"));
////  for (ODocument result: results) {
//    log.info("result.json(): "+result.toJSON());
////    log.info("result.field: "+result.field("name"));
////  }
//
//  db.close()
//
//}
