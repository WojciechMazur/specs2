package org.specs2
package form

import control.{ Property }
import execute._
import matcher._

import scala.xml.NodeSeq

/**
 * Utility methods to build Fields, Props and Forms and insert them in other Forms or
 * Fragments.
 */
private[specs2]
trait FormsBuilder {

  /** anything can be added on a Form row as a Field */
  implicit def anyIsField[T](t: =>T): Field[T] = Field(t)
  /** anything can be added on a Form row as a TextCell */
  implicit def anyIsFieldCell(t: =>Any): FieldCell = fieldIsTextCell(Field(t))
  /** any seq of object convertible to cells */
  implicit def anyCellableSeq[T : ToCell](seq: Seq[T]): Seq[Cell] = seq.map(t => ToCell[T](t))
  /** any xml can be injected as a cell */
  implicit def xmlIsACell[T](xml: =>NodeSeq): XmlCell = new XmlCell(xml)
  /** a Field can be added on a Form row as a FieldCell */
  implicit def fieldIsTextCell(t: Field[?]): FieldCell = new FieldCell(t)
  /** a Effect can be added on a Form row as a EffectCell */
  implicit def effectIsTextCell(t: Effect[?]): EffectCell = new EffectCell(t)
  /** a Prop can be added on a Form row as a PropCell */
  implicit def propIsCell(t: Prop[?, ?]): PropCell = new PropCell(t)
  /** a Form can be added on a Form row as a FormCell */
  implicit def formIsCell(t: =>Form): FormCell = new FormCell(t)
  /** a Form can be implicitly executed if necessary */
  implicit def formIsExecutable(f: Form): Result = f.execute

  trait ToCell[T] {
    def toCell(t: =>T): Cell
  }

  object ToCell {

    def apply[T : ToCell](t: => T): Cell =
      implicitly[ToCell[T]].toCell(t)

    implicit def toCellField[T]: ToCell[Field[T]] = new ToCell[Field[T]] {
      def toCell(t: =>Field[T]): Cell =
        fieldIsTextCell(t)
    }
    implicit def toCellXml: ToCell[NodeSeq] = new ToCell[NodeSeq] {
      def toCell(t: =>NodeSeq): Cell =
        xmlIsACell(t)
    }
    implicit def toCellEffect[T]: ToCell[Effect[T]] = new ToCell[Effect[T]] {
      def toCell(t: =>Effect[T]): Cell =
        effectIsTextCell(t)
    }
    implicit def toCellProp[T, S]: ToCell[Prop[T, S]] = new ToCell[Prop[T, S]] {
      def toCell(t: =>Prop[T, S]): Cell =
        propIsCell(t)
    }
    implicit def toCellForm: ToCell[Form] = new ToCell[Form] {
      def toCell(t: =>Form): Cell =
        formIsCell(t)
    }
  }

  /** a cell can be added lazily to a row. It will only be evaluated when necessary */
  def lazify(c: =>Cell) = new LazyCell(c)

  /** @return a new Form with the given title */
  def form(title: String) = Form(title)

  /** @return a new Field with no label and a value */
  def field[T](value: =>T): Field[T] = Field(value)

  /** @return a new Field with a label and a value */
  def field[T](label: String, value: =>T): Field[T] = Field(label, value)

  /** @return a new Effect with a label and a value */
  def effect[T](label: String, value: =>T): Effect[T] = Effect(label, value)

  /** @return a new Field with a label and several values */
  def field(label: String, value1: Field[?], values: Field[?]*): Field[String] = Field(label, value1, values:_*)

  /** @return a new Prop with an actual value only */
  def prop[T](act: =>T) = new Prop[T, T](actual = Property(act))

  /** @return a new Prop with a label and an actual value only */
  def prop[T](label: String, actual: =>T): Prop[T, T] = Prop[T](label, actual)

  /** @return a new Prop with a label, an actual value and expected value */
  def prop[T, S](label: String, actual: =>T, exp: =>S) =
    new Prop[T, S](label, new Property(() => Some(actual)), new Property(() => Some(exp)))

  /** @return a new Prop with a label, an actual value and a constraint to apply to values */
  def prop[T, S](label: String, actual: =>T, c: (T, S) => Result) = Prop(label, actual, c)

  /** @return a new Prop with a label, an actual value and a matcher to apply to values */
  def prop[T, S](label: String, actual: =>T, c: (S) => Matcher[T]) = Prop[T, S](label, actual, c)

  /** @return a new Prop with a label, an actual value and a matcher to apply to the actual value */
  def prop[T](label: String, actual: =>T, c: Matcher[T]) = Prop[T](label, actual, c)

  /** @return a new Prop with no label, an actual value and a matcher to apply to the actual value */
  def prop[T](actual: =>T, c: Matcher[T]) = Prop[T]("", actual, c)

  /** @return a new Prop with a label, an actual value and a matcher to apply to the actual value */
  def prop[T, S](label: String, actual: =>T, expected: => S, c: Matcher[T]) = Prop[T, S](label, actual, expected, c)

  /** @return a new Prop with a label, which has the same actual and expected value to test the result of an action */
  def action[T](label: String, a: =>T): Prop[T, T] = {
    lazy val act = a
    prop(label, act)(act)
  }

  /** @return a new Tabs object */
  def tabs = new Tabs()

  /** @return a new Tabs object with a first tab */
  def tab(label: String, form: Form) = tabs.tab(label, form)

}
private[specs2]
object FormsBuilder extends FormsBuilder
