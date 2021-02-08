/*
  File: Puttable.java

  Originally written by Doug Lea and released into the public domain.
  This may be used for any purposes whatsoever without acknowledgment.
  Thanks for the assistance and support of Sun Microsystems Labs,
  and everyone contributing, testing, and using this code.

  History:
  Date       Who                What
  11Jun1998  dl               Create public version
*/

package de.simplicit.vjdbc.server.concurrent;

/**
 * This interface exists to enable stricter type checking
 * for channels. A method argument or instance variable
 * in a producer object can be declared as only a Puttable
 * rather than a Channel, in which case a Java compiler
 * will disallow take operations. <p>
 * 此接口的存在是为了对通道启用更严格的类型检查。
 * 生产者对象中的方法参数或实例变量可以声明为仅 Puttable 而不是通道，
 * 在这种情况下，Java 编译器将不允许执行操作。
 * <p>
 * Full method descriptions appear in the Channel interface. <p>
 * 完整方法描述将显示在通道接口中。
 * <p>[<a href="http://gee.cs.oswego.edu/dl/classes/EDU/oswego/cs/dl/util/concurrent/intro.html"> Introduction to this package. </a>]
 * <p>[<a href="http://gee.cs.oswego.edu/dl/classes/EDU/oswego/cs/dl/util/concurrent/intro.html"> 此包简介 </a>]
 * @see Channel
 * @see Takable
 **/

public interface Puttable {


    /**
     * Place item in the channel, possibly waiting indefinitely until
     * it can be accepted. Channels implementing the BoundedChannel
     * subinterface are generally guaranteed to block on puts upon
     * reaching capacity, but other implementations may or may not block. <p>
     * 将项目放置在通道中，可能会无限期等待，直到可以接受为止。
     * 通常保证实现BoundedChannel子接口的通道在达到容量时会阻塞放置，但其他实现可能会阻塞也可能不会阻塞。
     * @param item the element to be inserted. Should be non-null. 要插入的元素，应该不为空值
     * @exception InterruptedException if the current thread has
     * been interrupted at a point at which interruption
     * is detected, in which case the element is guaranteed not
     * to be inserted. Otherwise, on normal return, the element is guaranteed
     * to have been inserted. <p>
     * 如果当前线程在检测到中断时中断，则保证不会插入该元素。否则，在正常返回时，保证已插入元素。
     **/
    public void put(Object item) throws InterruptedException;


    /**
     * Place item in channel only if it can be accepted within
     * msecs milliseconds. The time bound is interpreted in
     * a coarse-grained, best-effort fashion.  <p>
     * 仅在可以在毫秒内接受项目时，才将项目放在通道中。时间限制以粗粒度、尽力而为的方式解释。
     * @param item the element to be inserted. Should be non-null. 要插入的元素，应该不为空值
     * @param msecs the number of milliseconds to wait. If less than
     * or equal to zero, the method does not perform any timed waits,
     * but might still require
     * access to a synchronization lock, which can impose unbounded
     * delay if there is a lot of contention for the channel. <p>
     * 等待的毫秒数。如果小于或等于零，则该方法将不执行任何定时的等待，
     * 但可能仍需要访问同步锁，如果信道存在大量争用，则可能会施加无限延迟。
     * @return true if accepted, else false 如果接受则为true，否则为false
     * @exception InterruptedException if the current thread has
     * been interrupted at a point at which interruption
     * is detected, in which case the element is guaranteed not
     * to be inserted (i.e., is equivalent to a false return). <p>
     * 如果当前线程在检测到中断时中断，在这种情况下，保证不插入元素（即等效于错误返回）。
     **/
    public boolean offer(Object item, long msecs) throws InterruptedException;
}
