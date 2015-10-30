package controllers

import org.apache.commons.codec.digest.DigestUtils

package object authentication {

  def hash(stuff: String): String = {
    DigestUtils.sha256Hex(stuff)
  }

}
