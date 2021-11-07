package org.specs2
package specification
package core

import process.DefaultExecutor
import execute._
import org.specs2.matcher.ActionMatchers

class FragmentsContinuationSpec(val env: Env) extends Specification with ActionMatchers with OwnEnv { def is = s2"""

 A fragment continuation must
   return other fragments if the previous result is a success                $continuationAfterSuccess
   not return other fragments if the previous result is not a success        $continuationAfterFailure
   return an error fragment if the continuation fragments fail to be created $continuationError


"""

  def continuationAfterSuccess =
    runContinuation(ok, "ko" ! ko) must haveSize(2)

  def continuationAfterFailure =
    runContinuation(ko, "ko" ! ko) must haveSize(1)

  def continuationError = {
    val fragments = runContinuation(ok, "ko" ! {sys.error("boom"); ko})
    (fragments must haveSize(2)) and
    (fragments(1).description.show must beMatching("Could not create fragments after the previous successful result"))
  }

  def runContinuation[R : AsResult](r: =>R, fs: =>Fragments): List[Fragment] =
    Fragments(DefaultExecutor.execute(ownEnv)(Fragments("test" ! FragmentsContinuation.continueWith(r, fs)).contents)).
      fragmentsList(ownEnv.executionEnv)
}
