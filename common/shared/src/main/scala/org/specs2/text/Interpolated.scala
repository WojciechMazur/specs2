package org.specs2
package text

import Trim._

/**
 * This class extracts interpolated expressions from an interpolated string, given the string content and the text
 * pieces in between the interpolated expressions
 */
class Interpolated(stringContent: String, texts: Seq[String]) {

  def expressions = {
    texts.zip(texts.drop(1)).foldLeft((stringContent, Seq[String]())) { case ((content, exps), (text, next)) =>
      val minusText = new String(content.drop(text.length).mkString).
                        replace("$$", "$") // in next, this replacement has already been done
      val textToParse = new String(if (minusText.indexOf(next) > 0) minusText.substring(0, minusText.indexOf(next)) else minusText)

      val expression = textToParse
      (new String(minusText.drop(expression.length)), exps :+ trimVariableDeclaration(expression))
    }._2
  }

  private def trimVariableDeclaration = (_:String).removeStart("$").removeStart("{").removeEnd("}").removeStart("`").removeEnd("`")
}
