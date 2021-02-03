/*
  File: Channel.java

  Originally written by Doug Lea and released into the public domain.
  This may be used for any purposes whatsoever without acknowledgment.
  Thanks for the assistance and support of Sun Microsystems Labs,
  and everyone contributing, testing, and using this code.

  History:
  Date       Who                What
  11Jun1998  dl               Create public version
  25aug1998  dl               added peek
*/

package de.simplicit.vjdbc.server.concurrent;

/** 
 * Main interface for buffers, queues, pipes, conduits, etc. <p>
 * 缓冲区、队列、管道、管道等的主接口。
 * <p>
 * A Channel represents anything that you can put items
 * into and take them out of. As with the Sync 
 * interface, both
 * blocking (put(x), take),
 * and timeouts (offer(x, msecs), poll(msecs)) policies
 * are provided. Using a
 * zero timeout for offer and poll results in a pure balking policy. <p>
 * 通道表示可以放入并带出任何东西。与同步接口一样，
 * 提供了blocking (put(x), take),和timeouts (offer(x, msecs), poll(msecs))策略。
 * 对offer和poll使用零超时会导致纯阻塞策略。
 * <p>
 * To aid in efforts to use Channels in a more typesafe manner,
 * this interface extends Puttable and Takable. You can restrict
 * arguments of instance variables to this type as a way of
 * guaranteeing that producers never try to take, or consumers put.
 * for example: <p>
 * 为了帮助大家以更安全的方式使用通道Channel的方法，这个接口扩展了Puttable和Takable。
 * 您可以将实例变量的参数限制为这种类型，以保证生产者从不尝试take或消费者从不尝试put，例子：
 * <pre>
 * class Producer implements Runnable {
 *   final Puttable chan;
 *   Producer(Puttable channel) { chan = channel; }
 *   public void run() {
 *     try {
 *       for(;;) { chan.put(produce()); }
 *     }
 *     catch (InterruptedException ex) {}
 *   }
 *   Object produce() { ... }
 * }
 *
 *
 * class Consumer implements Runnable {
 *   final Takable chan;
 *   Consumer(Takable channel) { chan = channel; }
 *   public void run() {
 *     try {
 *       for(;;) { consume(chan.take()); }
 *     }
 *     catch (InterruptedException ex) {}
 *   }
 *   void consume(Object x) { ... }
 * }
 *
 * class Setup {
 *   void main() {
 *     Channel chan = new SomeChannelImplementation();
 *     Producer p = new Producer(chan);
 *     Consumer c = new Consumer(chan);
 *     new Thread(p).start();
 *     new Thread(c).start();
 *   }
 * }
 * </pre>
 * <p>
 * A given channel implementation might or might not have bounded
 * capacity or other insertion constraints, so in general, you cannot tell if
 * a given put will block. However,
 * Channels that are designed to 
 * have an element capacity (and so always block when full)
 * should implement the BoundedChannel subinterface. <p>
 * 给定的通道实现可能具有或不具有容量限制或其他插入数据的规则，因此通常无法确定给定的put是否会阻塞。
 * 然而，被设计为具有元素容量的通道（因此在装满时始终会阻塞）应该实现BoundedChannel子接口。
 * <p>
 * Channels may hold any kind of item. However,
 * insertion of null is not in general supported. Implementations
 * may (all currently do) throw IllegalArgumentExceptions upon attempts to
 * insert null. <p>
 * 通道可以容纳任何类型的对象。但是，一般不支持插入null。当前实现（所有当前执行）在尝试插入null时可能会引发非法存在异常(IllegalArgumentException)
 * <p>
 * By design, the Channel interface does not support any methods to determine
 * the current number of elements being held in the channel.
 * This decision reflects the fact that in
 * concurrent programming, such methods are so rarely useful
 * that including them invites misuse; at best they could
 * provide a snapshot of current
 * state, that could change immediately after being reported.
 * It is better practice to instead use poll and offer to try
 * to take and put elements without blocking. For example,
 * to empty out the current contents of a channel, you could write: <p>
 * 根据设计，通道接口不支持任何方法来确定通道中当前持有的元素数量。
 * 此决定反映了以下事实：在并发编程中，此类方法很少有用，以至于包括它们在内会引起滥用。
 * 最好的情况是，它们可以提供当前状态的快照，该快照可以在报告后立即更改。
 * 更好的做法是改为使用poll并尝试在不阻塞的情况下获取和放置元素。
 * 例如，要清空通道的当前内容，可以这样编写：
 * <pre>
 *  try {
 *    for (;;) {
 *       Object item = channel.poll(0);
 *       if (item != null)
 *         process(item);
 *       else
 *         break;
 *    }
 *  }
 *  catch(InterruptedException ex) { ... }
 * </pre>
 * <p>
 * However, it is possible to determine whether an item
 * exists in a Channel via <code>peek</code>, which returns
 * but does NOT remove the next item that can be taken (or null
 * if there is no such item). The peek operation has a limited
 * range of applicability, and must be used with care. Unless it
 * is known that a given thread is the only possible consumer
 * of a channel, and that no time-out-based <code>offer</code> operations
 * are ever invoked, there is no guarantee that the item returned
 * by peek will be available for a subsequent take. <P>
 * 但是，可以通过<code>peek</code>确定某个项目是否存在于Channel中，
 * 该项目将返回但不会删除下一个可以提取的项目（如果没有这样的项目，则为null）。
 * 窥视操作的适用范围有限，必须小心使用。
 * 除非已知线程是通道的唯一可能使用者，并且不曾调用过基于超时的<code>offer</code>操作，否则无法保证peek返回的项将被可用于后续获取。
 * <p>
 * When appropriate, you can define an isEmpty method to
 * return whether <code>peek</code> returns null. <p>
 * 在适当的时候，您可以定义一个isEmpty方法来确定<code>peek</code>是否返回null。
 * <p>
 * Also, as a compromise, even though it does not appear in interface,
 * implementation classes that can readily compute the number
 * of elements support a <code>size()</code> method. This allows careful
 * use, for example in queue length monitors, appropriate to the
 * particular implementation constraints and properties. <p>
 * 另外，作为一种折衷，即使它没有出现在接口中，可以轻松计算元素数量的实现类也支持<code>size()</code>方法。
 * 这允许谨慎使用，例如在队列长度监视器中，以适合特定的实现约束和属性。
 * <p>
 * All channels allow multiple producers and/or consumers.
 * They do not support any kind of <em>close</em> method
 * to shut down operation or indicate completion of particular
 * producer or consumer threads. 
 * If you need to signal completion, one way to do it is to
 * create a class such as <p>
 * 所有通道都允许多个生产者和/或使用者。
 * 它们不支持任何<em>close</em>方法来关闭操作或指示特定生产者或使用者线程的完成。
 * 如果您需要发出完成信号，一种方法是创建一个类，例如
 * <pre>
 * class EndOfStream { 
 *    // Application-dependent field/methods
 * }
 * </pre>
 * And to have producers put an instance of this class into
 * the channel when they are done. The consumer side can then
 * check this via <p>
 * 并且让生产者在完成时将此类的实例放入通道中。然后，消费者可以通过
 * <pre>
 *   Object x = aChannel.take();
 *   if (x instanceof EndOfStream) 
 *     // special actions; perhaps terminate
 *   else
 *     // process normally
 * </pre>
 * <p>
 * In time-out based methods (poll(msecs) and offer(x, msecs), 
 * time bounds are interpreted in
 * a coarse-grained, best-effort fashion. Since there is no
 * way in Java to escape out of a wait for a synchronized
 * method/block, time bounds can sometimes be exceeded when
 * there is a lot contention for the channel. Additionally,
 * some Channel semantics entail a ``point of
 * no return'' where, once some parts of the operation have completed,
 * others must follow, regardless of time bound.
 * <p>
 * 在基于超时的方法(poll(msecs)和offer(x, msecs)中，时间范围以粗粒度，
 * 尽力而为的方式进行解释，因为Java中没有办法逃脱等待同步的方法/块，
 * 有时在通道争用时可能会超时。此外，某些通道语义包含“point of return”，
 * 一旦操作的某些部分完成，其他操作必须遵循， 不受时间限制。
 * <p>
 * Interruptions are in general handled as early as possible
 * in all methods. Normally, InterruptionExceptions are thrown
 * in put/take and offer(msec)/poll(msec) if interruption
 * is detected upon entry to the method, as well as in any
 * later context surrounding waits.  <p>
 * 中断一般在所有方法中都尽可能早处理。
 * 通常，如果在进入方法时以及在以后的任何等待上下文中检测到中断，
 * 则在put/take和offer(msec)/poll(msec)中引发InterruptionExceptions。
 * <p>
 * If a put returns normally, an offer
 * returns true, or a put or poll returns non-null, the operation
 * completed successfully. 
 * In all other cases, the operation fails cleanly -- the
 * element is not put or taken.
 * <p>
 * 如果放值正常返回，则要约返回 true，或者放项或轮询返回非空，则操作成功完成。
 * 在所有其他情况下，操作完全失败 -- 元素不放或取。
 * <p>
 * As with Sync classes, spinloops are not directly supported,
 * are not particularly recommended for routine use, but are not hard 
 * to construct. For example, here is an exponential backoff version:
 * <p>
 * 与Sync类一样，不直接支持自旋循环，不建议日常使用，也不难构造。例如，这是一个指数补偿版本：
 * <pre>
 * Object backOffTake(Channel q) throws InterruptedException {
 *   long waitTime = 0;
 *   for (;;) {
 *      Object x = q.poll(0);
 *      if (x != null)
 *        return x;
 *      else {
 *        Thread.sleep(waitTime);
 *        waitTime = 3 * waitTime / 2 + 1;
 *      }
 *    }
 * </pre>
 * <p>
 * <b>Sample Usage</b>. Here is a producer/consumer design
 * where the channel is used to hold Runnable commands representing
 * background tasks.
 * <p>
 * <b>示例用法</b>。
 * 下面是一个生产者/使用者设计，其中通道用于保存表示后台任务的 Runable 命令。
 * <pre>
 * class Service {
 *   private final Channel channel = ... some Channel implementation;
 *  
 *   private void backgroundTask(int taskParam) { ... }
 *
 *   public void action(final int arg) {
 *     Runnable command = 
 *       new Runnable() {
 *         public void run() { backgroundTask(arg); }
 *       };
 *     try { channel.put(command) }
 *     catch (InterruptedException ex) {
 *       Thread.currentThread().interrupt(); // ignore but propagate
 *     }
 *   }
 * 
 *   public Service() {
 *     Runnable backgroundLoop = 
 *       new Runnable() {
 *         public void run() {
 *           for (;;) {
 *             try {
 *               Runnable task = (Runnable)(channel.take());
 *               task.run();
 *             }
 *             catch (InterruptedException ex) { return; }
 *           }
 *         }
 *       };
 *     new Thread(backgroundLoop).start();
 *   }
 * }
 *    
 * </pre>
 * <p>[<a href="http://gee.cs.oswego.edu/dl/classes/EDU/oswego/cs/dl/util/concurrent/intro.html"> Introduction to this package. </a>]
 * <p>[<a href="http://gee.cs.oswego.edu/dl/classes/EDU/oswego/cs/dl/util/concurrent/intro.html"> 此包简介 </a>]
 * @see Sync
 * @see BoundedChannel 
**/

public interface Channel extends Puttable, Takable {

  /** 
   * Place item in the channel, possibly waiting indefinitely until
   * it can be accepted. Channels implementing the BoundedChannel
   * subinterface are generally guaranteed to block on puts upon
   * reaching capacity, but other implementations may or may not block.
   * <p>将item放置在通道中，可能无限期等待直到它可以被接受。
   * 实现BoundedChannel子接口的通道通常保证在达到容量时阻塞，但其他实现可能阻塞，也可能不阻塞。
   * @param item the element to be inserted. Should be non-null. <p>要插入的元素不应该为空。
   * @exception InterruptedException if the current thread has
   * been interrupted at a point at which interruption
   * is detected, in which case the element is guaranteed not
   * to be inserted. Otherwise, on normal return, the element is guaranteed
   * to have been inserted.
   * <p>如果当前线程在检测到中断时中断，则保证不会插入该元素。否则，在正常返回时，保证已插入元素。
  **/
  public void put(Object item) throws InterruptedException;

  /** 
   * Place item in channel only if it can be accepted within
   * msecs milliseconds. The time bound is interpreted in
   * a coarse-grained, best-effort fashion.
   * <p>只有当item可以在毫秒内被接受时，才将其放置在通道中。时间限制是用粗粒度的、尽力而为的方式解释的。
   * @param item the element to be inserted. Should be non-null. <p>要插入的元素不应该为空。
   * @param msecs the number of milliseconds to wait. If less than
   * or equal to zero, the method does not perform any timed waits,
   * but might still require
   * access to a synchronization lock, which can impose unbounded
   * delay if there is a lot of contention for the channel.
   * <p>等待的毫秒数。如果小于或等于0，该方法不执行任何定时等待，但可能仍然需要访问同步锁，
   * 如果有很多通道争用，这可能会造成无限的延迟。
   * @return true if accepted, else false <p>如果被接受则为true，否则为false
   * @exception InterruptedException if the current thread has
   * been interrupted at a point at which interruption
   * is detected, in which case the element is guaranteed not
   * to be inserted (i.e., is equivalent to a false return).
   * <p>如果当前线程在检测到中断的点被中断，在这种情况下，元素保证不会被插入(即等同于返回false)。
  **/
  public boolean offer(Object item, long msecs) throws InterruptedException;

  /** 
   * Return and remove an item from channel, 
   * possibly waiting indefinitely until
   * such an item exists.
   * <p>返回并从频道中删除一个item，可能无限期地等待直到该项目存在。
   * @return  some item from the channel. Different implementations
   *  may guarantee various properties (such as FIFO) about that item
   *  <p>来自通道的一些item。不同的实现可能会保证该项的各种属性(比如FIFO)
   * @exception InterruptedException if the current thread has
   * been interrupted at a point at which interruption
   * is detected, in which case state of the channel is unchanged.
   * <p>如果当前线程在检测到中断时被中断，在这种情况下通道的状态不会改变。
   *
  **/
  public Object take() throws InterruptedException;


  /** 
   * Return and remove an item from channel only if one is available within
   * msecs milliseconds. The time bound is interpreted in a coarse
   * grained, best-effort fashion.
   * <p>只有当一个项目在毫秒内可用时，才从通道中返回并删除。时间范围以粗粒度，尽力而为的方式进行解释。
   * @param msecs the number of milliseconds to wait. If less than
   *  or equal to zero, the operation does not perform any timed waits,
   * but might still require
   * access to a synchronization lock, which can impose unbounded
   * delay if there is a lot of contention for the channel.
   * <p>等待的毫秒数。如果小于或等于0，该方法不执行任何定时等待，但可能仍然需要访问同步锁，
   * 如果有很多通道争用，这可能会造成无限的延迟。
   * @return some item, or null if the channel is empty. <p>某些项目；如果通道为空，则返回null。
   * @exception InterruptedException if the current thread has
   * been interrupted at a point at which interruption
   * is detected, in which case state of the channel is unchanged
   * (i.e., equivalent to a null return).
   * <p>如果当前线程在检测到中断的点被中断，在这种情况下，元素保证不会被插入(即等同于返回false)。
  **/

  public Object poll(long msecs) throws InterruptedException;

  /**
   * Return, but do not remove object at head of Channel,
   * or null if it is empty.
   * 返回，但不要移除通道头部的对象，如果为空则为null。
   **/

  public Object peek();

}

