/*
  File: Executor.java

  Originally written by Doug Lea and released into the public domain.
  This may be used for any purposes whatsoever without acknowledgment.
  Thanks for the assistance and support of Sun Microsystems Labs,
  and everyone contributing, testing, and using this code.

  History:
  Date       Who                What
  19Jun1998  dl               Create public version
*/

package de.simplicit.vjdbc.server.concurrent;

/**
 * Interface for objects that execute Runnables,
 * as well as various objects that can be wrapped
 * as Runnables. <p>
 * 执行Runnable的对象以及可以包装为Runnable的各种对象的接口。<p>
 * The main reason to use Executor throughout a program or
 * subsystem is to provide flexibility: You can easily
 * change from using thread-per-task to using pools or
 * queuing, without needing to change most of your code that
 * generates tasks.
 * <p>
 * 在整个程序或子系统中使用Executor的主要原因是为了提供灵活性：
 * 您可以轻松地从使用每个任务线程更改为使用池或排队，而无需更改生成任务的大多数代码。 <p>
 * The general intent is that execution be asynchronous,
 * or at least independent of the caller. For example,
 * one of the simplest implementations of <code>execute</code>
 * (as performed in ThreadedExecutor)
 * is <code>new Thread(command).start();</code>.
 * However, this interface allows implementations that instead
 * employ queueing or pooling, or perform additional
 * bookkeeping.
 * <p>
 * 一般的意图是让执行是异步的，或者至少是独立于调用者的。例如，
 * <code>execute</code>(在threaddexecutor中执行)的一个最简单的实现是
 * <code>new Thread(command).start();</code>
 * 然而，该接口允许使用排队或池，或执行其他簿记的实现。
 *
 * <p>[<a href="http://gee.cs.oswego.edu/dl/classes/EDU/oswego/cs/dl/util/concurrent/intro.html"> Introduction to this package. </a>]
 * <p>[<a href="http://gee.cs.oswego.edu/dl/classes/EDU/oswego/cs/dl/util/concurrent/intro.html"> 此软件包的简介 </a>]
 **/
public interface Executor {
  /** 
   * Execute the given command. This method is guaranteed
   * only to arrange for execution, that may actually
   * occur sometime later; for example in a new
   * thread. However, in fully generic use, callers
   * should be prepared for execution to occur in
   * any fashion at all, including immediate direct
   * execution.
   * <p>执行给定的命令。保证只安排执行此方法，这可能在以后的某个时间实际发生。
   * 例如在新线程中。但是，在完全通用的使用中，调用者应准备好以任何方式发生执行，包括立即直接执行。
   * <p>
   * The method is defined not to throw 
   * any checked exceptions during execution of the command. Generally,
   * any problems encountered will be asynchronous and
   * so must be dealt with via callbacks or error handler
   * objects. If necessary, any context-dependent 
   * catastrophic errors encountered during
   * actions that arrange for execution could be accompanied
   * by throwing context-dependent unchecked exceptions.
   * <p>
   * 该方法定义为在命令执行期间不引发任何检查的异常。
   * 通常，遇到的任何问题都是异步的，因此必须通过回调或错误处理程序对象进行处理。
   * 如有必要，在安排执行操作的过程中遇到任何与上下文相关的灾难性错误，
   * 可能会引发与上下文相关的未经检查的异常。
   * <p>
   * However, the method does throw InterruptedException:
   * It will fail to arrange for execution
   * if the current thread is currently interrupted.
   * Further, the general contract of the method is to avoid,
   * suppress, or abort execution if interruption is detected
   * in any controllable context surrounding execution.
   * <p>但是，该方法确实会抛出InterruptedException:
   * 如果当前线程当前被中断，它将无法安排执行。此外，该方法的一般约定是，
   * 如果在任何可控制的执行上下文中检测到中断，则避免、抑制或中止执行。
   **/
  public void execute(Runnable command) throws InterruptedException;

}
