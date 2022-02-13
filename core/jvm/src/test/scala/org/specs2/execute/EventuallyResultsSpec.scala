package org.specs2
package execute

import mutable.Specification
import org.specs2.matcher.ResultMatchers
import scala.concurrent._, duration._

class EventuallyResultsSpec extends Specification with ResultMatchers {
  """
  `eventually` can be used to retry any result until a maximum number of times is reached
    or until it succeeds.
  """.txt

  "A success succeeds right away with eventually" in {
    eventually(Success())
  }
  "A failure will always fail" in {
    eventually(Failure()).not
  }
  "A result will be retried automatically until it succeeds" in {
    val iterator = List(Failure(), Failure(), Success()).iterator
    eventually(iterator.next)
  }

  "If all retries fail, the result will eventually fail" in {
    val iterator = Iterator.continually(Failure())
    eventually(iterator.next).not
  }
  "Any object convertible to a result can be used with eventually" in {
    val iterator = List(false, false, true).iterator
    eventually(iterator.next) must beSuccessful
  }
  "Even if a result throws an exception it must be evaluated 'retries' times only" in {
    var eval = 0
    def r = { eval += 1; 1 must_== 2 }

    eventually(retries = 3, sleep = 100.millis)(r) must beFailing
    eval must_== 3
  }

}
