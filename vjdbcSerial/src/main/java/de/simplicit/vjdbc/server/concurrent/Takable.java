/*
  File: Takable.java

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
 * in a consumer object can be declared as only a Takable
 * rather than a Channel, in which case a Java compiler
 * will disallow put operations.
 * <p>此接口的存在是为了对通道启用更严格的类型检查。
 * 使用者对象中的方法参数或实例变量可以声明为仅可执行，而不是通道，在这种情况下，Java 编译器将不允许put操作。
 * <p>
 * Full method descriptions appear in the Channel interface.
 * <p>完整方法描述将显示在通道接口中
 * <p>[<a href="http://gee.cs.oswego.edu/dl/classes/EDU/oswego/cs/dl/util/concurrent/intro.html"> Introduction to this package. </a>]
 * <p>[<a href="http://gee.cs.oswego.edu/dl/classes/EDU/oswego/cs/dl/util/concurrent/intro.html"> 此包简介 </a>]
 * @see Channel
 * @see Puttable
 **/

public interface Takable {

    /**
     * Return and remove an item from channel,
     * possibly waiting indefinitely until
     * such an item exists.
     * <p>从通道返回并删除项，可能无限期等待直到该项存在
     * @return  some item from the channel. Different implementations
     *  may guarantee various properties (such as FIFO) about that item
     *  <p>通道中的某些东西，不同的实现可能会保证该项目的各种属性（例如FIFO）
     * @exception InterruptedException if the current thread has
     * been interrupted at a point at which interruption
     * is detected, in which case state of the channel is unchanged.
     * <p>如果当前线程已在检测到中断的点处中断，则通道的状态不变。
     *
     **/
    public Object take() throws InterruptedException;


    /**
     * Return and remove an item from channel only if one is available within
     * msecs milliseconds. The time bound is interpreted in a coarse
     * grained, best-effort fashion. <p>
     * 仅当某个项目在毫秒内可用时，才从通道中退回并删除该项目。时间范围以粗粒度，尽力而为的方式进行解释。
     * @param msecs the number of milliseconds to wait. If less than
     *  or equal to zero, the operation does not perform any timed waits,
     * but might still require
     * access to a synchronization lock, which can impose unbounded
     * delay if there is a lot of contention for the channel. <p>
     * 等待的毫秒数。如果小于或等于0，则操作不执行任何定时等待，但可能仍然需要访问同步锁，如果有大量通道争用，这可能会造成无限延迟。
     * @return some item, or null if the channel is empty. <p>某些项，如果通道为空，则为空
     * @exception InterruptedException if the current thread has
     * been interrupted at a point at which interruption
     * is detected, in which case state of the channel is unchanged
     * (i.e., equivalent to a false return). <p>
     * 如果当前线程已在检测到中断的位置被中断，则在这种情况下通道的状态保持不变（即等同于错误的返回）。
     **/

    public Object poll(long msecs) throws InterruptedException;

}
