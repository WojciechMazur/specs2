package org.specs2
package mock
import matcher._
import control.Exceptions._
import org.mockito.InOrder
import org.mockito.stubbing.Answer
import org.mockito.internal.stubbing.StubberImpl
import org.mockito.invocation.InvocationOnMock
import org.mockito.internal.InOrderImpl 
import org.mockito.verification.{ VerificationMode }
import org.mockito.internal.stubbing._
import org.mockito.stubbing.{ OngoingStubbing, Stubber }


trait Mockito extends MocksCreation with CalledMatchers {

}
/**
 * This trait provides methods to declare expectations on mock calls:<code>
 * 
 * there was one(mockedList).get(0)
 * there was no(mockedList).get(0)
 * 
 * there was two(mockedList).get(0)
 * there was three(mockedList).get(0)
 * there was 4.times(mockedList).get(0)
 *
 * there was atLeastOne(mockedList).get(0)
 * there was atLeastTwo(mockedList).get(0)
 * there was atLeastThree(mockedList).get(0)
 * there was atLeast(4)(mockedList).get(0)
 * there was atMostOne(mockedList).get(0)
 * there was atMostTwo(mockedList).get(0)
 * there was atMostThree(mockedList).get(0)
 * there was atMost(4)(mockedList).get(0)
 * 
 * It is also possible to use a different wording:
 * 
 * there were two(mockedList).get(0)
 * got { two(mockedList).get(0) }
 * 
 * </code>
 */
trait CalledMatchers extends NumberOfTimes with TheMockitoMocker with Expectations {
  /** temporary InOrder object to accumulate mocks to verify in order */
  private var inOrder: Option[InOrderImpl] = None
  /** this matcher evaluates an expression containing mockito calls verification */
  private class CallsMatcher[T] extends Matcher[T] {
    def apply[S <: T : Expectable](v: =>S) = {
      tryOr {
    	v
    	result(true, "The mock was called as expected", "The mock was not called as expected")
      } { (e: Exception) =>
          result(false, "The mock was called as expected", "The mock was not called as expected: " + e.getMessage)
      }
    }
  }
  
  /** create an object supporting 'was' and 'were' methods */
  def there = new Calls
  /** 
   * class supporting 'was' and 'were' methods to forward mockito calls to the CallsMatcher matcher 
   */
  class Calls {
    def were[T](calls: =>T): MatchResult[String] = was(calls)
    def was[T](calls: =>T): MatchResult[String] = {
      catchAll { calls } { identity } match {
    	case Right(v) => new MatchSuccess("The mock was called as expected", "The mock was not called as expected", new Expectable(v.toString))
    	case Left(e) => new MatchFailure("The mock was called as expected", "The mock was not called as expected: " + e.getMessage, new Expectable(e.getMessage))
      }
    }
  }
  /**
   * alias for 'there was'
   */
  def got[T](t: =>T) = there was t
  /**
   * implicit definition to be able to declare a number of calls 3.times(m).clear()
   */
  implicit def rangeIntToTimes(r: RangeInt): RangeIntToTimes = new RangeIntToTimes(r)
  /**
   * class providing a apply method to be able to declare a number of calls:
   *   3.times(m).clear() is actually 3.times.apply(m).clear()
   */
  class RangeIntToTimes(r: RangeInt) {
    def apply[T <: AnyRef](mock: =>T) = verify(mock, org.mockito.Mockito.times(r.n))
  }
  /**
   * verify that a mock has been called appropriately
   * if an inOrder object has been previously created (which means we're verifying the mocks calls order),
   * then the mock is added to the inOrder object and the inOrder object is used for the verification.
   * 
   * Otherwise a normal verification is performed
   */
  private def verify[T <: AnyRef](mock: =>T, v: VerificationMode) = {
    inOrder map { ordered => 
      val mocksList = ordered.getMocksToBeVerifiedInOrder()
      if (!mocksList.contains(mock)) {
        mocksList.add(mock)
        inOrder = Some(new InOrderImpl(mocksList))
      }
    }
    mocker.verify(inOrder, mock, v)
  }
  /** no call made to the mock */
  def no[T <: AnyRef](mock: =>T) = verify(mock, org.mockito.Mockito.never())
  /** one call only made to the mock */
  def one[T <: AnyRef](mock: =>T) = verify(mock, org.mockito.Mockito.times(1))
  /** two calls only made to the mock */
  def two[T <: AnyRef](mock: =>T) = verify(mock, org.mockito.Mockito.times(2))
  /** three calls only made to the mock */
  def three[T <: AnyRef](mock: =>T) = verify(mock, org.mockito.Mockito.times(3))
  /** at least n calls made to the mock */
  def atLeast[T <: AnyRef](i: Int)(mock: =>T) = verify(mock, org.mockito.Mockito.atLeast(i))
  /** at least 1 call made to the mock */
  def atLeastOne[T <: AnyRef](mock: =>T) = verify(mock, org.mockito.Mockito.atLeast(1))
  /** at least 2 calls made to the mock */
  def atLeastTwo[T <: AnyRef](mock: =>T) = verify(mock, org.mockito.Mockito.atLeast(2))
  /** at least 3 calls made to the mock */
  def atLeastThree[T <: AnyRef](mock: =>T) = verify(mock, org.mockito.Mockito.atLeast(3))
  /** at most n calls made to the mock */
  def atMost[T <: AnyRef](i: Int)(mock: =>T) = verify(mock, org.mockito.Mockito.atMost(i))
  /** at most 1 call made to the mock */
  def atMostOne[T <: AnyRef](mock: =>T) = verify(mock, org.mockito.Mockito.atMost(1))
  /** at most 2 calls made to the mock */
  def atMostTwo[T <: AnyRef](mock: =>T) = verify(mock, org.mockito.Mockito.atMost(2))
  /** at most 3 calls made to the mock */
  def atMostThree[T <: AnyRef](mock: =>T) = verify(mock, org.mockito.Mockito.atMost(3))
  /** no more calls made to the mock */
  def noMoreCallsTo[T <: AnyRef](mock: =>T) = mocker.verifyNoMoreInteractions(mock)
  /** implicit def supporting calls in order */
  implicit def toInOrderMode[T](calls: =>T): ToInOrderMode[T] = new ToInOrderMode(calls)
  /** 
   * class defining a then method to declare that calls must be made in a specific order.
   * 
   * The orderedBy method can be used to declare the mock order if there are several mocks
   */
  class ToInOrderMode[T](calls: =>T) {
//    def then[U](otherCalls: =>U) = {
//      val newOrder = inOrder match {
//        case Some(o) => Some(o)
//        case None => Some(new InOrderImpl(new java.util.ArrayList[Object]))
//      }
//      val f = () => setTemporarily(inOrder, newOrder, (o:Option[InOrderImpl]) => inOrder = o) {
//        calls
//        otherCalls 
//      }
//      f() must new CallsMatcher
//    }
//    /** specify the mocks which are to be checked in order */
//    def orderedBy(mocks: AnyRef*) = {
//      setTemporarily(inOrder, Some(new InOrderImpl(java.util.Arrays.asList(mocks.toArray: _*) )), (o:Option[InOrderImpl]) => inOrder = o) {
//        calls
//      } 
//    }
  }
}
trait NumberOfTimes {
  /** 
   * This implicit definition allows to declare a number of times
   * <code>3.times</code>
   */
  implicit def integerToRange(n: Int): RangeInt = new RangeInt(n)
  case class RangeInt(n: Int) { 
    def times = this 
  }
}

/**
 * This trait provides methods to create mocks and spies.
 */
trait MocksCreation extends TheMockitoMocker {
  /**
   * create a mock object: val m = mock[java.util.List[String]]
   */
  def mock[T : ClassManifest]: T = mocker.mock(implicitly[ClassManifest[T]])
  /**
   * create a mock object with a name: val m = mockAs[java.util.List[String]]("name")
   */
  def mockAs[T : ClassManifest](name: String): T = mocker.mock(name)
  /**
   * implicit allowing the following syntax for a named mock: val m = mock[java.util.List[String]],as("name")
   */
  implicit def mockToAs[T : ClassManifest](t: =>T) = new NamedMock(t)
  
  /** support class to create a mock object with a name */
  class NamedMock[T : ClassManifest](t: =>T) {
    def as(name: String): T = mockAs[T](name)
  }

  /**
   * create a mock object with smart return values: val m = smartMock[java.util.List[String]]
   * 
   * This is the equivalent of Mockito.mock(List.class, SMART_NULLVALUES) but testing shows that it is not working well with Scala.
   */
  def smartMock[T : ClassManifest]: T = mocker.smartMock
  /**
   * create a spy on an object. 
   * 
   * A spy is a real object but can still have some of its methods stubbed. However the syntax for stubbing a spy is a bit different than 
   * with a mock:<code>
   * 
   * val s = spy(new LinkedList[String])
   * doReturn("one").when(s).get(0) // instead of s.get(0) returns "one" which would throw an exception
   * 
   * </code>
   */
  def spy[T](m: T): T = mocker.spy(m)
}

/**
 * shortcuts to standard Mockito functions
 */
trait MockitoFunctions extends TheMockitoMocker {
    /** delegate to MockitoMocker doReturn. */
  def doReturn[T](t: T) = mocker.doReturn(t)
  /** delegate to MockitoMocker doAnswer. */
  def doAnswer[T](a: Answer[T]) = mocker.doAnswer(a)
  /** delegate to MockitoMocker doThrow. */
  def doThrow[E <: Throwable](e: E) = mocker.doThrow(e)
  /** delegate to MockitoMocker doNothing. */
  def doNothing = mocker.doNothing
}

/** delegate to Mockito static methods with appropriate type inference. */
trait TheMockitoMocker {
  private[specs2] val mocker = new MockitoMocker {}	
}