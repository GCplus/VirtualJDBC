/*
  File: BoundedChannel.java

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
 * A channel that is known to have a capacity, signifying
 * that <code>put</code> operations may block when the
 * capacity is reached. Various implementations may have
 * intrinsically hard-wired capacities, capacities that are fixed upon
 * construction, or dynamically adjustable capacities.
 * <p>已知具有容量的通道，表示达到容量后，<code>put</code>操作可能会阻塞。
 * 各种实现可能具有操作系统默认的硬连接的容量、在构造时固定的容量或动态可调的容量。
 * @see DefaultChannelCapacity
 * <p>[<a href="http://gee.cs.oswego.edu/dl/classes/EDU/oswego/cs/dl/util/concurrent/intro.html"> Introduction to this package. </a>] <p>
 * [<a href="http://gee.cs.oswego.edu/dl/classes/EDU/oswego/cs/dl/util/concurrent/intro.html"> 此包简介 </a>]
 **/

public interface BoundedChannel extends Channel {
    /**
     * Return the maximum number of elements that can be held.
     * <p>返回可保存元素的最大数目。
     * @return the capacity of this channel. 通道中的容量
     **/
    public int capacity();
}
