package org.specs2.specification.create

import org.specs2.reflect.MacroContext.Context
import org.specs2.reflect.Macros.{methodCall, stringExpr, toAST}
import org.specs2.specification.core.Fragments

object S2Macro {
  def s2Implementation(c: Context)(variables: c.Expr[InterpolatedFragment]*) : c.Expr[Fragments] = {
    import c.{universe => u}
    import u.{Position => _, _}

    val texts = c.prefix.tree match { case Apply(_, List(Apply(_, ts))) => ts; case _ => Seq() }

    val macroPos = c.macroApplication.pos
    val fileContent = macroPos.source.content.mkString

    def contentFrom(pos: c.Position) = fileContent.split("\n").drop(pos.line - 1).mkString("\n").drop(pos.column-1)
    val content = contentFrom(macroPos).drop("s2\"\"\"".length)
    val Yrangepos = macroPos.isRange

    def traceLocation(pos: c.universe.Position): String =
      Seq(pos.source.path, pos.source.file.name, pos.line).mkString("|")

    val textStartPositions = texts.map(t => q"${traceLocation(t.pos)}")

    // if we don't have the range positions we just position start and end on the same line
    val textEndPositions =
      if (Yrangepos) texts.map(t => q"${traceLocation(t.pos.focus.withPoint(t.pos.end))}")
      else           textStartPositions

    val result =
      c.Expr(methodCall(c)("s2",
        q"$content",
        q"$Yrangepos",
        toAST[List[?]](c)(texts:_*),
        toAST[List[?]](c)(textStartPositions:_*),
        toAST[List[?]](c)(textEndPositions:_*),
        toAST[List[?]](c)(variables.map(_.tree):_*),
        toAST[List[?]](c)(variables.map(stringExpr(c)(_)):_*)))

    c.Expr(atPos(c.prefix.tree.pos)(result.tree))

  }

}
