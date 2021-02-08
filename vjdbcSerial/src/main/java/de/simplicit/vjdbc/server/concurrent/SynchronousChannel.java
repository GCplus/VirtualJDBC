/*
  File: SynchronousChannel.java

  Originally written by Doug Lea and released into the public domain.
  This may be used for any purposes whatsoever without acknowledgment.
  Thanks for the assistance and support of Sun Microsystems Labs,
  and everyone contributing, testing, and using this code.

  History:
  Date       Who                What
  11Jun1998  dl               Create public version
  17Jul1998  dl               Disabled direct semaphore permit check
  31Jul1998  dl               Replaced main algorithm with one with
                              better scaling and fairness properties.
  25aug1998  dl               added peek
  24Nov2001  dl               Replaced main algorithm with faster one.
*/

package de.simplicit.vjdbc.server.concurrent;

/**
 * A rendezvous channel, similar to those used in CSP and Ada.  Each
 * put must wait for a take, and vice versa.  Synchronous channels
 * are well suited for handoff designs, in which an object running in
 * one thread must synch up with an object running in another thread
 * in order to hand it some information, event, or task.
 * <p>会合通道，类似于CSP和Ada中使用的通道。
 * 每个put必须等待一个take，反之亦然。 同步通道非常适合切换设计，在该设计中，
 * 一个线程中运行的对象必须与另一个线程中运行的对象同步，以便向其传递一些信息，事件或任务。
 * <p> If you only need threads to synch up without
 * exchanging information, consider using a Barrier. If you need
 * bidirectional exchanges, consider using a Rendezvous.  <p>
 * 如果只需要线程来同步而不交换信息，请考虑使用屏障。如果需要双向交换，请考虑使用集合。
 * <p>[<a href="http://gee.cs.oswego.edu/dl/classes/EDU/oswego/cs/dl/util/concurrent/intro.html"> Introduction to this package. </a>]
 * <p>[<a href="http://gee.cs.oswego.edu/dl/classes/EDU/oswego/cs/dl/util/concurrent/intro.html"> 此包简介 </a>]
 * @see CyclicBarrier
 * @see Rendezvous
 **/

public class SynchronousChannel implements BoundedChannel {

  /*
    This implementation divides actions into two cases for puts:
    该实现将操作分为两种情况：

    * An arriving putter that does not already have a waiting taker
      creates a node holding item, and then waits for a taker to take it.
      一个到达的putter如果还没有等待接受者，就会创建一个持有项目的节点，然后等待接受者拿走它。
    * An arriving putter that does already have a waiting taker fills
      the slot node created by the taker, and notifies it to continue.
      一个到达的putter如果已经有一个等待的接受者，就会填满接受者创建的槽节点，并通知它继续。

   And symmetrically, two for takes:
   对称地，需要两个：

    * An arriving taker that does not already have a waiting putter
      creates an empty slot node, and then waits for a putter to fill it.
      没有等到putter到达的接受者会创建一个空的槽节点，然后等待putter将其填满。
    * An arriving taker that does already have a waiting putter takes
      item from the node created by the putter, and notifies it to continue.
      已经有一个等待的putter的到达接收者从putter创建的节点获取项，并通知它继续。

   This requires keeping two simple queues: waitingPuts and waitingTakes.
   这需要保持两个简单的队列：waitingPuts和waitingTakes

   When a put or take waiting for the actions of its counterpart
   aborts due to interruption or timeout, it marks the node
   it created as "CANCELLED", which causes its counterpart to retry
   the entire put or take sequence.
   当等待其对应对象操作的put或take由于中断或超时而中止时，
   它将创建的节点标记为“CANCELLED”，这将导致其对应对象重试整个put或take序列。
  */

    /**
     * Special marker used in queue nodes to indicate that
     * the thread waiting for a change in the node has timed out
     * or been interrupted.
     * 在队列节点中使用的特殊标记，用于指示等待节点更改的线程超时或被中断。
     **/
    protected static final Object CANCELLED = new Object();

    /**
     * Simple FIFO queue class to hold waiting puts/takes.
     * 简单的FIFO队列类，用于保存等待的put/take
     **/
    protected static class Queue {
        protected LinkedNode head;
        protected LinkedNode last;

        protected void enq(LinkedNode p) {
            if (last == null)
                last = head = p;
            else
                last = last.next = p;
        }

        protected LinkedNode deq() {
            LinkedNode p = head;
            if (p != null && (head = p.next) == null)
                last = null;
            return p;
        }
    }

    protected final Queue waitingPuts = new Queue();
    protected final Queue waitingTakes = new Queue();

    /**
     * @return zero -- 0
     * Synchronous channels have no internal capacity.
     * <p>同步通道没有内部容量
     **/
    public int capacity() { return 0; }

    /**
     * @return null -- 空值
     * Synchronous channels do not hold contents unless actively taken
     * <p>同步通道不保存内容，除非已主动获取
     **/
    public Object peek() {  return null;  }


    public void put(Object x) throws InterruptedException {
        if (x == null) throw new IllegalArgumentException();

        // This code is conceptually straightforward, but messy
        // because we need to intertwine handling of put-arrives first
        // vs take-arrives first cases.
        // 这段代码在概念上很简单，但是却很混乱，因为我们需要将处理先到达的put与先到达的take交织在一起。

        // Outer loop is to handle retry due to cancelled waiting taker
        // 外部循环用于处理因为取消等待接受器而引起的重试
        for (;;) {

            // Get out now if we are interrupted
            // 一旦中断立刻抛出
            if (Thread.interrupted()) throw new InterruptedException();

            // Exactly one of item or slot will be nonnull at end of
            // synchronized block, depending on whether a put or a take
            // arrived first.
            // 在synchronized block的末尾，item或slot中恰好有一个是非null的，这取决于put或take是先到达的。
            LinkedNode slot;
            LinkedNode item = null;

            synchronized(this) {
                // Try to match up with a waiting taker; fill and signal it below
                // 试着与等待的接受者匹配;填充并发出以下信号
                slot = waitingTakes.deq();

                // If no takers yet, create a node and wait below
                // 如果还没有接受者，创建一个节点并等待
                if (slot == null)
                    waitingPuts.enq(item = new LinkedNode(x));
            }

            if (slot != null) { // There is a waiting taker. 有一个等待的接受者
                // Fill in the slot created by the taker and signal taker to
                // continue.
                // 填写接收者和信号接收者创建的slot以继续
                synchronized(slot) {
                    if (slot.value != CANCELLED) {
                        slot.value = x;
                        slot.notify();
                        return;
                    }
                    // else the taker has cancelled, so retry outer loop
                    // 否则taker已经取消，所以重试外环
                }
            }

            else {
                // Wait for a taker to arrive and take the item.
                // 等待taker到达并拿走item
                synchronized(item) {
                    try {
                        while (item.value != null)
                            item.wait();
                        return;
                    }
                    catch (InterruptedException ie) {
                        // If item was taken, return normally but set interrupt status
                        // 如果已取走item，则正常返回但要设置中断状态
                        if (item.value == null) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                        else {
                            item.value = CANCELLED;
                            throw ie;
                        }
                    }
                }
            }
        }
    }

    public Object take() throws InterruptedException {
        // Entirely symmetric to put()
        // 与put()完全对称

        for (;;) {
            if (Thread.interrupted()) throw new InterruptedException();

            LinkedNode item;
            LinkedNode slot = null;

            synchronized(this) {
                item = waitingPuts.deq();
                if (item == null)
                    waitingTakes.enq(slot = new LinkedNode());
            }

            if (item != null) {
                synchronized(item) {
                    Object x = item.value;
                    if (x != CANCELLED) {
                        item.value = null;
                        item.next = null;
                        item.notify();
                        return x;
                    }
                }
            }

            else {
                synchronized(slot) {
                    try {
                        for (;;) {
                            Object x = slot.value;
                            if (x != null) {
                                slot.value = null;
                                slot.next = null;
                                return x;
                            }
                            else
                                slot.wait();
                        }
                    }
                    catch(InterruptedException ie) {
                        Object x = slot.value;
                        if (x != null) {
                            slot.value = null;
                            slot.next = null;
                            Thread.currentThread().interrupt();
                            return x;
                        }
                        else {
                            slot.value = CANCELLED;
                            throw ie;
                        }
                    }
                }
            }
        }
    }

  /*
    Offer and poll are just like put and take, except even messier.
    Offer和poll就像put和take，只不过更混乱。
   */


    public boolean offer(Object x, long msecs) throws InterruptedException {
        if (x == null) throw new IllegalArgumentException();
        long waitTime = msecs;
        long startTime = 0; // lazily initialize below if needed -- 如果需要，在下面延迟初始化

        for (;;) {
            if (Thread.interrupted()) throw new InterruptedException();

            LinkedNode slot;
            LinkedNode item = null;

            synchronized(this) {
                slot = waitingTakes.deq();
                if (slot == null) {
                    if (waitTime <= 0)
                        return false;
                    else
                        waitingPuts.enq(item = new LinkedNode(x));
                }
            }

            if (slot != null) {
                synchronized(slot) {
                    if (slot.value != CANCELLED) {
                        slot.value = x;
                        slot.notify();
                        return true;
                    }
                }
            }

            long now = System.currentTimeMillis();
            if (startTime == 0)
                startTime = now;
            else
                waitTime = msecs - (now - startTime);

            if (item != null) {
                synchronized(item) {
                    try {
                        for (;;) {
                            if (item.value == null)
                                return true;
                            if (waitTime <= 0) {
                                item.value = CANCELLED;
                                return false;
                            }
                            item.wait(waitTime);
                            waitTime = msecs - (System.currentTimeMillis() - startTime);
                        }
                    }
                    catch (InterruptedException ie) {
                        if (item.value == null) {
                            Thread.currentThread().interrupt();
                            return true;
                        }
                        else {
                            item.value = CANCELLED;
                            throw ie;
                        }
                    }
                }
            }
        }
    }

    public Object poll(long msecs) throws InterruptedException {
        long waitTime = msecs;
        long startTime = 0;

        for (;;) {
            if (Thread.interrupted()) throw new InterruptedException();

            LinkedNode item;
            LinkedNode slot = null;

            synchronized(this) {
                item = waitingPuts.deq();
                if (item == null) {
                    if (waitTime <= 0)
                        return null;
                    else
                        waitingTakes.enq(slot = new LinkedNode());
                }
            }

            if (item != null) {
                synchronized(item) {
                    Object x = item.value;
                    if (x != CANCELLED) {
                        item.value = null;
                        item.next = null;
                        item.notify();
                        return x;
                    }
                }
            }

            long now = System.currentTimeMillis();
            if (startTime == 0)
                startTime = now;
            else
                waitTime = msecs - (now - startTime);

            if (slot != null) {
                synchronized(slot) {
                    try {
                        for (;;) {
                            Object x = slot.value;
                            if (x != null) {
                                slot.value = null;
                                slot.next = null;
                                return x;
                            }
                            if (waitTime <= 0) {
                                slot.value = CANCELLED;
                                return null;
                            }
                            slot.wait(waitTime);
                            waitTime = msecs - (System.currentTimeMillis() - startTime);
                        }
                    }
                    catch(InterruptedException ie) {
                        Object x = slot.value;
                        if (x != null) {
                            slot.value = null;
                            slot.next = null;
                            Thread.currentThread().interrupt();
                            return x;
                        }
                        else {
                            slot.value = CANCELLED;
                            throw ie;
                        }
                    }
                }
            }
        }
    }

}
