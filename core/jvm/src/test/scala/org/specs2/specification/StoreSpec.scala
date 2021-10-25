package org.specs2
package specification

import control._
import io._

import org.specs2.fp.syntax._
import execute.AsResult
import process._

class StoreSpec extends Specification { def is = sequential ^ s2"""
 The file store stores values in files where the name of the file is
   defined by the key $e1

 The store can be reset $e2

"""

  def e1 = {
    val store = DirectoryStore(DirectoryPath.unsafe("target/test"))
    val key = SpecificationStatsKey("name")
    (store.set(key, Stats(1)) >> store.get(key)).map(_ must beSome(Stats(1)))
  }

  def e2 = {
    val store = DirectoryStore(DirectoryPath.unsafe("target/test"))
    AsResult(e1)

    val key = SpecificationStatsKey("name")
    (store.reset >> store.get(key)).map(_ must beNone)
  }

}
