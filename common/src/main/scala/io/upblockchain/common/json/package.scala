package io.upblockchain.common

import org.json4s.DefaultFormats

package object json {

  class StringOptionFormats extends DefaultFormats {
    override val strictOptionParsing: Boolean = true
  }

}