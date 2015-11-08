package gg.duel

import com.fasterxml.jackson.databind.node.{ArrayNode, ObjectNode}

package object transformer {
  import collection.JavaConverters._

  implicit class objectEnricher(objectNode: ObjectNode) {
    def getObjectO(name: String) = Option(objectNode.get(name)).collect { case ob: ObjectNode => ob }
    def safeGetObjectValues(name: String) = Option(objectNode.get(name)).toList.collect {
      case ob: ObjectNode => ob.elements().asScala.toList
    }.flatten
    def safeGetObjectValueObjects(name: String) = safeGetObjectValues(name).collect { case on: ObjectNode => on }
    def safeGetArrayObjects(name: String) = Option(objectNode.get(name)).toList.collect{ case an: ArrayNode =>
      an.elements().asScala.collect { case ob: ObjectNode => ob }}.flatten
    def getArrayO(name: String) = Option(objectNode.get(name)).collect { case ob: ArrayNode => ob }
    def getArrayL(name: String) = Option(objectNode.get(name)).toList.collect { case ob: ArrayNode =>
      ob.elements().asScala
    }.flatten
  }
}
