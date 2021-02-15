package review

import scala.concurrent.Future

object ThreadModelLimitations extends App {
  /*
  Limitation #1: OOP encapsulation is only valid in the SINGLE THREADED MODEL
   */
  class BankAccount(private var amount: Int) {
    override def toString: String = "" + amount

    def withdraw(money: Int) = this.amount -= money
    def deposit(money: Int): Unit = this.amount += money
    def getAmount = amount

    def withdrawWithSync(money: Int) = this.synchronized{
      this.amount -= money
    }

    def depositWithSync(money: Int): Unit = this.synchronized{
      this.amount += money
    }

  }

  val account = new BankAccount(2000)

  /*
  for(_ <- 1 to 1000) {
    new Thread(() => account.withdraw(1)).start()
  }

  for(_ <- 1 to 1000) {
    new Thread(() => account.deposit(1)).start()
  }
  println(account.getAmount) // not 2000 because withdraw and deposit aren't thread-safe
  */
  // OOP encapsulation is broken in multi-threaded env

  // synchronization! Locks to the rescue
  // But this poses other problems such as deadlocks, livelocks

  /**
   * Limitation #2: delegating something to a thread is not very straightforward
   */
  // you have a running thread and you want to pass a runnable to that thread.
  var task: Runnable = null
  val runningThread: Thread = new Thread(() => {
    while (true) {
      while (task == null) {
        runningThread.synchronized {
          println("[background] waiting for a task...")
          runningThread.wait()
        }
      }
      task.synchronized {
        println("[background] I have a task!")
        task.run()
        task = null
      }
    }
  })

  def delegateToBackgroundThread(r: Runnable) = {
    if (task == null) task = r

    runningThread.synchronized {
      runningThread.notify()
    }
  }

  runningThread.start()
  Thread.sleep(500)
  delegateToBackgroundThread(() => println(42))
  Thread.sleep(1000)
  delegateToBackgroundThread(() => println("this should run in the background"))

  /**
   * Limitation #3: tracing and dealing with errors in multi-threaded is a pain
   */
  // 1M numbers in between 10 threads
  import scala.concurrent.ExecutionContext.Implicits.global

  val futures = (0 to 9)
    .map(i => 100000 * i until 100000 * (i + 1)) // 0 - 9999, 100000 - 199999, 200000 - 299999 etc
    .map(range => Future {
      if (range.contains(543425)) throw new RuntimeException("invalid number")
      range.sum
    })

  val sumFuture = Future.reduceLeft(futures)(_ + _) // Future with the sum of all the numbers
  sumFuture.onComplete(println)
}
