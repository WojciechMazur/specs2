package org.specs2
package reporter

import io._
import main.Arguments
import org.specs2.specification.core.{OwnEnv, Env, SpecificationStructure}
import matcher._
import control._

class HtmlPrinterSpec(val env: Env) extends Specification with ActionMatchers with ThrownExpectations with OwnEnv { def is = sequential ^ s2"""

 The Html printer outputs html files for a specification and its linked specification

   if html.search == true then it creates an index contents file $index
   if html.search == true then it creates a search page          $searchPage

"""

  def index = {
    val spec = new Specification { def is = s2""" one example $ok """}
    val env1 = env.setArguments(searchArguments)

    printer.getHtmlOptions(env1.arguments).map(_.search).runOption must beSome(true)

    finalize(env1, spec) must beOk
    FilePathReader.exists(outDir / FilePath.unsafe("javascript/tipuesearch/tipuesearch_contents.js"))
  }


  def searchPage = {
    val spec = new Specification { def is = s2""" one example $ok """}
    val env1 = env.setArguments(searchArguments)

    finalize(env1, spec) must beOk
    FilePathReader.exists(outDir | FileName.unsafe("search.html"))
  }

  def finalize(env: Env, spec: SpecificationStructure): Action[Unit] =
    for {
      options <- printer.getHtmlOptions(env.arguments).toAction
      _       <- printer.copyResources(env, options).toAction
      _       <- printer.finalize(env, List(spec.structure(env)))
    } yield ()


  val printer = HtmlPrinter

  val outDir = DirectoryPath.unsafe("target/test/HtmlPrinterSpec")
  val searchArguments = Arguments.split(s"html.search html.outdir ${outDir.path}")
}
