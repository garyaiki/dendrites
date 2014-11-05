package org.gs.akka.aggregator
import akka.contrib.pattern.WorkList
import org.scalatest.FunSuiteLike
import scala.annotation.tailrec

final case class TestEntry(id: Int)

class WorkListSpec extends FunSuiteLike {

  val workList = WorkList.empty[TestEntry]
  var entry2: TestEntry = null
  var entry4: TestEntry = null

  test("Processing empty WorkList") {
    // ProcessAndRemove something in the middle
    val processed = workList process {
      case TestEntry(9) ⇒ true
      case _            ⇒ false
    }
    assert(!processed)
  }

  test("Insert temp entries") {
    assert(workList.head === workList.tail)

    val entry0 = TestEntry(0)
    workList.add(entry0, permanent = false)

    assert(workList.head.next != null)
    assert(workList.tail === workList.head.next)
    assert(workList.tail.ref.get === entry0)

    val entry1 = TestEntry(1)
    workList.add(entry1, permanent = false)

    assert(workList.head.next != workList.tail)
    assert(workList.head.next.ref.get === entry0)
    assert(workList.tail.ref.get === entry1)

    entry2 = TestEntry(2)
    workList.add(entry2, permanent = false)

    assert(workList.tail.ref.get === entry2)

    val entry3 = TestEntry(3)
    workList.add(entry3, permanent = false)

    assert(workList.tail.ref.get === entry3)
  }

  test("Process temp entries") {

    // ProcessAndRemove something in the middle
    assert(workList process {
      case TestEntry(2) ⇒ true
      case _            ⇒ false
    })

    // ProcessAndRemove the head
    assert(workList process {
      case TestEntry(0) ⇒ true
      case _            ⇒ false
    })

    // ProcessAndRemove the tail
    assert(workList process {
      case TestEntry(3) ⇒ true
      case _            ⇒ false
    })
  }

  test("Re-insert permanent entry") {
    entry4 = TestEntry(4)
    workList.add(entry4, permanent = true)

    assert(workList.tail.ref.get === entry4)
  }

  test("Process permanent entry") {
    assert(workList process {
      case TestEntry(4) ⇒ true
      case _            ⇒ false
    })
  }

  test("Remove permanent entry") {
    val removed = workList remove entry4
    assert(removed)
  }

  test("Remove temp entry already processed") {
    val removed = workList remove entry2
    assert(!removed)
  }

  test("Process non-matching entries") {

    val processed =
      workList process {
        case TestEntry(2) ⇒ true
        case _            ⇒ false
      }

    assert(!processed)

    val processed2 =
      workList process {
        case TestEntry(5) ⇒ true
        case _            ⇒ false
      }

    assert(!processed2)

  }

  test("Append two lists") {
    workList.removeAll()
    0 to 4 foreach { id ⇒ workList.add(TestEntry(id), permanent = false) }

    val l2 = new WorkList[TestEntry]
    5 to 9 foreach { id ⇒ l2.add(TestEntry(id), permanent = true) }

    workList addAll l2

    @tailrec
    def checkEntries(id: Int, entry: WorkList.Entry[TestEntry]): Int = {
      if (entry == null) id
      else {
        assert(entry.ref.get.id === id)
        checkEntries(id + 1, entry.next)
      }
    }

    assert(checkEntries(0, workList.head.next) === 10)
  }

  test("Clear list") {
    workList.removeAll()
    assert(workList.head.next === null)
    assert(workList.tail === workList.head)
  }

  val workList2 = WorkList.empty[PartialFunction[Any, Unit]]

  val fn1: PartialFunction[Any, Unit] = {
    case s: String ⇒
      val result1 = workList2 remove fn1
      assert(result1 === true, "First remove must return true")
      val result2 = workList2 remove fn1
      assert(result2 === false, "Second remove must return false")
  }

  val fn2: PartialFunction[Any, Unit] = {
    case s: String ⇒
      workList2.add(fn1, permanent = true)
  }

  test("Reentrant insert") {
    workList2.add(fn2, permanent = false)
    assert(workList2.head.next != null)
    assert(workList2.tail == workList2.head.next)

    // Processing inserted fn1, reentrant adding fn2
    workList2 process { fn ⇒
      var processed = true
      fn.applyOrElse("Foo", (_: Any) ⇒ processed = false)
      processed
    }
  }

  test("Reentrant delete") {
    // Processing inserted fn2, should delete itself
    workList2 process { fn ⇒
      var processed = true
      fn.applyOrElse("Foo", (_: Any) ⇒ processed = false)
      processed
    }
  }
}