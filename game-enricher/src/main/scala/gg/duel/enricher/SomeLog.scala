package gg.duel.enricher

import com.fasterxml.jackson.databind.node.{ArrayNode, IntNode}

case class SomeLog(arrayNode: ArrayNode) {
  import collection.JavaConverters._

  def transform(): Unit = {
    for {
      (logItem, index) <- arrayNode.elements().asScala.zipWithIndex
      logValue <- Option(logItem.get("_2")).collect{case i: IntNode => i}
    } arrayNode.set(index, logValue)
  }
}
