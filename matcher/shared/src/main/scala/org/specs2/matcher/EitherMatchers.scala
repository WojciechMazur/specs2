package org.specs2
package matcher

import org.specs2.control.ImplicitParameters._
import org.specs2.matcher.describe.Diffable

/**
 * Matchers for the Either datatype
 */
trait EitherMatchers extends EitherBaseMatchers with EitherBeHaveMatchers
object EitherMatchers extends EitherMatchers

private[specs2]
trait EitherBaseMatchers {

  def beRight[T](t: ValueCheck[T]) = RightCheckedMatcher(t)
  def beRight[T](implicit p: ImplicitParam = implicitParameter) = use(p)(new RightMatcher[T])

  def right[T : Diffable](t: T) = beRight(ValueChecks.valueIsTypedValueCheck(t))
  def right[T](t: ValueCheck[T]) = beRight(t)
  def right[T](implicit p: ImplicitParam = implicitParameter) = beRight(p)

  def beLeft[T](t: ValueCheck[T]): LeftCheckedMatcher[T] = LeftCheckedMatcher(t)
  def beLeft[T](implicit p: ImplicitParam = implicitParameter): LeftMatcher[T] = use(p)(LeftMatcher[T]())

  def left[T : Diffable](t: T) = beLeft(ValueChecks.valueIsTypedValueCheck(t))
  def left[T](t: ValueCheck[T]) = beLeft(t)
  def left[T](implicit p: ImplicitParam = implicitParameter) = beLeft(p)
}

private[specs2]
trait EitherBeHaveMatchers extends BeHaveMatchers { outer: EitherBaseMatchers =>
  implicit class EitherResultMatcher[L : Diffable, R : Diffable](result: MatchResult[Either[L, R]]) {
    def right(r: =>R): MatchResult[Either[L, R]] = result(outer.beRight(r))
    def left(l: =>L): MatchResult[Either[L, R]] = result(outer.beLeft(l))
    def beRight(r: =>R): MatchResult[Either[L, R]] = result(outer.beRight(r))
    def beLeft(l: =>L): MatchResult[Either[L, R]] = result(outer.beLeft(l))

    def right: MatchResult[Either[L, R]] = result(outer.beRight)
    def left: MatchResult[Either[L, R]] = result(outer.beLeft)
    def beRight: MatchResult[Either[L, R]] = result(outer.beRight)
    def beLeft: MatchResult[Either[L, R]] = result(outer.beLeft)
  }
}

case class RightMatcher[T]() extends OptionLikeMatcher[({type l[a]=Either[?, a]})#l, T, T]("Right", (_:Either[Any, T]).right.toOption)
case class RightCheckedMatcher[T](check: ValueCheck[T]) extends OptionLikeCheckedMatcher[({type l[a]=Either[?, a]})#l, T, T]("Right", (_:Either[Any, T]).right.toOption, check)

case class LeftMatcher[T]() extends OptionLikeMatcher[({type l[a]=Either[a, ?]})#l, T, T]("Left", (_:Either[T, Any]).left.toOption)
case class LeftCheckedMatcher[T](check: ValueCheck[T]) extends OptionLikeCheckedMatcher[({type l[a]=Either[a, ?]})#l, T, T]("Left", (_:Either[T, Any]).left.toOption, check)
