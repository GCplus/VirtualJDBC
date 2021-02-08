/*
  File: PooledExecutor.java

  Originally written by Doug Lea and released into the public domain.
  This may be used for any purposes whatsoever without acknowledgment.
  Thanks for the assistance and support of Sun Microsystems Labs,
  and everyone contributing, testing, and using this code.

  History:
  Date       Who                What
  19Jun1998  dl               Create public version
  29aug1998  dl               rely on ThreadFactoryUser,
                              remove ThreadGroup-based methods
                              adjusted locking policies
   3mar1999  dl               Worker threads sense decreases in pool size
  31mar1999  dl               Allow supplied channel in constructor;
                              add methods createThreads, drain
  15may1999  dl               Allow infinite keepalives
  21oct1999  dl               add minimumPoolSize methods
   7sep2000  dl               BlockedExecutionHandler now an interface,
                              new DiscardOldestWhenBlocked policy
  12oct2000  dl               add shutdownAfterProcessingCurrentlyQueuedTasks
  13nov2000  dl               null out task ref after run
  08apr2001  dl               declare inner class ctor protected
  12nov2001  dl               Better shutdown support
                              Blocked exec handlers can throw IE
                              Simplify locking scheme
  25jan2001  dl               {get,set}BlockedExecutionHandler now public
  17may2002  dl               null out task var in worker run to enable GC.
  30aug2003  dl               check for new tasks when timing out
  18feb2004  dl               replace dead thread if no others left
*/

package de.simplicit.vjdbc.server.concurrent;
import java.util.*;

/**
 * A tunable, extensible thread pool class. The main supported public
 * method is <code>execute(Runnable command)</code>, which can be
 * called instead of directly creating threads to execute commands.<p>
 *
 * 可调整的，可扩展的线程池类。
 * 主要支持的公共方法是<code>execute(Runnable command)</code>，可以通过调用它来执行命令，不需要直接创建线程。
 * <p>
 * Thread pools can be useful for several, usually intertwined
 * reasons:<p>
 *
 * 线程池之所以有用，通常是由于以下几个原因：
 * <ul>
 *
 *    <li> To bound resource use. A limit can be placed on the maximum
 *    number of simultaneously executing threads.<p>
 *    绑定资源使用。 可以对同时执行的线程最大数量设置限制。
 *
 *    <li> To manage concurrency levels. A targeted number of threads
 *    can be allowed to execute simultaneously.<p>
 *    管理并发级别。 可以允许目标数量的线程同时执行。
 *
 *    <li> To manage a set of threads performing related tasks.<p>
 *    管理执行相关任务的一组线程。
 *
 *    <li> To minimize overhead, by reusing previously constructed
 *    Thread objects rather than creating new ones.  (Note however
 *    that pools are hardly ever cure-alls for performance problems
 *    associated with thread construction, especially on JVMs that
 *    themselves internally pool or recycle threads.)<p>
 *    为了最大限度地减少开销，通过重复使用先前构造线程对象，而不是创造新的线程对象。
 *    （但是请注意，池几乎永远无法解决与线程构造相关的性能问题，尤其是在本身在内部池或回收线程的JVM上。）
 *
 * </ul>
 *
 * These goals introduce a number of policy parameters that are
 * encapsulated in this class. All of these parameters have defaults
 * and are tunable, either via get/set methods, or, in cases where
 * decisions should hold across lifetimes, via methods that can be
 * easily overridden in subclasses.  The main, most commonly set
 * parameters can be established in constructors.  Policy choices
 * across these dimensions can and do interact.  Be careful, and
 * please read this documentation completely before using!  See also
 * the usage examples below.<p>
 * 这些目标引入了封装在此类中的许多策略参数。
 * 所有这些参数都具有默认值，并且可以通过get/set方法进行调整，
 * 或者可以通过在子类中轻松覆盖的方法来调整决策（如果决策应在整个生命周期中保留）。
 * 可以在构造函数中建立主要的，最常用的设置参数。这些方面的政策选择确实可以相互作用。
 * 请小心，使用前请完整阅读本文档！ 另请参见下面的用法示例。
 *
 * <dl>
 *   <dt> Queueing 队列
 *
 *   <dd> By default, this pool uses queueless synchronous channels to
 *   to hand off work to threads. This is a safe, conservative policy
 *   that avoids lockups when handling sets of requests that might
 *   have internal dependencies. (In these cases, queuing one task
 *   could lock up another that would be able to continue if the
 *   queued task were to run.)  If you are sure that this cannot
 *   happen, then you can instead supply a queue of some sort (for
 *   example, a BoundedBuffer or LinkedQueue) in the constructor.
 *   This will cause new commands to be queued in cases where all
 *   MaximumPoolSize threads are busy. Queues are sometimes
 *   appropriate when each task is completely independent of others,
 *   so tasks cannot affect each others execution.  For example, in an
 *   http server.  <p>
 *   默认情况下，该池使用无队列同步通道将工作移交给线程。
 *   这是一种安全，保守的策略，在处理可能具有内部依赖项的请求集时避免锁定。
 *   （在这些情况下，对一个任务进行排队可能会锁定另一个任务，如果该队列中的任务正在运行，则该任务将能够继续。）
 *   如果您确定不会发生这种情况，则可以提供某种队列（例如， BoundedBuffer或LinkedQueue）。
 *   在所有MaximumPoolSize线程繁忙的情况下，这将导致新命令排队。
 *   当每个任务完全独立于其他任务时，队列有时是合适的，因此任务不会影响彼此的执行。 例如，在http服务器中。<p>
 *
 *   When given a choice, this pool always prefers adding a new thread
 *   rather than queueing if there are currently fewer than the
 *   current getMinimumPoolSize threads running, but otherwise always
 *   prefers queuing a request rather than adding a new thread. Thus,
 *   if you use an unbounded buffer, you will never have more than
 *   getMinimumPoolSize threads running. (Since the default
 *   minimumPoolSize is one, you will probably want to explicitly
 *   setMinimumPoolSize.)  <p>
 *   当有选择权时，如果当前运行的线程少于当前的getMinimumPoolSize线程，则该池总是更喜欢添加新线程而不是排队，
 *   否则，总是更喜欢排队请求而不是添加新线程。因此，如果您使用无限制的缓冲区，则运行的getMinimumPoolSize线程将永远不会超过。
 *   （由于默认的minimumPoolSize为1，因此您可能需要显式设置setMinimumPoolSize。）<p>
 *
 *   While queuing can be useful in smoothing out transient bursts of
 *   requests, especially in socket-based services, it is not very
 *   well behaved when commands continue to arrive on average faster
 *   than they can be processed.  Using bounds for both the queue and
 *   the pool size, along with run-when-blocked policy is often a
 *   reasonable response to such possibilities.  <p>
 *   尽管排队在消除请求的瞬时突发中很有用，尤其是在基于套接字的服务中，但当命令平均继续以比处理命令更快的速度到达时，
 *   它的行为表现就不太好。使用范围为队列和池的大小都与运行时阻断政策一起往往是这种可能性的合理安排。 <p>
 *
 *   Queue sizes and maximum pool sizes can often be traded off for
 *   each other. Using large queues and small pools minimizes CPU
 *   usage, OS resources, and context-switching overhead, but can lead
 *   to artifically low throughput. Especially if tasks frequently
 *   block (for example if they are I/O bound), a JVM and underlying
 *   OS may be able to schedule time for more threads than you
 *   otherwise allow. Use of small queues or queueless handoffs
 *   generally requires larger pool sizes, which keeps CPUs busier but
 *   may encounter unacceptable scheduling overhead, which also
 *   decreases throughput.  <p>
 *   队列大小和最大池大小通常可以相互权衡。 使用大队列和小池可最大程度地减少CPU使用率，OS资源和上下文切换开销，但可能导致吞吐量过低。
 *   尤其是如果任务频繁阻塞（例如，如果它们受I/O限制），那么JVM和底层OS可能能够安排比您原先允许的线程更多的线程的时间。
 *   使用小队列或无队列切换通常需要更大的池大小，这会使CPU繁忙，但可能会遇到不可接受的调度开销，这也会降低吞吐量。<p>
 *
 *   <dt> Maximum Pool size 池最大值大小
 *
 *   <dd> The maximum number of threads to use, when needed.  The pool
 *   does not by default preallocate threads.  Instead, a thread is
 *   created, if necessary and if there are fewer than the maximum,
 *   only when an <code>execute</code> request arrives.  The default
 *   value is (for all practical purposes) infinite --
 *   <code>Integer.MAX_VALUE</code>, so should be set in the
 *   constructor or the set method unless you are just using the pool
 *   to minimize construction overhead.  Because task handoffs to idle
 *   worker threads require synchronization that in turn relies on JVM
 *   scheduling policies to ensure progress, it is possible that a new
 *   thread will be created even though an existing worker thread has
 *   just become idle but has not progressed to the point at which it
 *   can accept a new task. This phenomenon tends to occur on some
 *   JVMs when bursts of short tasks are executed.  <p>
 *   需要时使用的最大线程数。 默认情况下，该池不预分配线程。
 *   相反，只有在<code>execute</code>请求到达时，才会在必要时创建线程，并且该线程数少于最大线程数。
 *   缺省值是（出于所有实际目的）无限值——<code>Integer.MAX_VALUE</code>，
 *   因此，除非您仅使用池来最大程度地减少构造开销，否则应在构造函数或set方法中进行设置。
 *   由于将任务切换到空闲工作线程需要同步，而同步又依赖于JVM调度策略来确保进度，因此即使现有工作线程刚刚变为空闲但尚未进展到某个程度，
 *   也可能会创建一个新线程。 它可以接受新任务。 当执行短任务突发时，这种现象倾向于在某些JVM上发生。<p>
 *
 *   <dt> Minimum Pool size 池最小值大小
 *
 *   <dd> The minimum number of threads to use, when needed (default
 *   1).  When a new request is received, and fewer than the minimum
 *   number of threads are running, a new thread is always created to
 *   handle the request even if other worker threads are idly waiting
 *   for work. Otherwise, a new thread is created only if there are
 *   fewer than the maximum and the request cannot immediately be
 *   queued.  <p>
 *   需要时使用的最小线程数（默认为1）。当接收到一个新的请求并且少于正在运行的最小线程数时，即使其他工作线程空闲地等待工作，也会始终创建一个新线程来处理该请求。
 *   否则，仅当少于最大线程数且无法立即将请求排队时，才创建新线程。<p>
 *
 *   <dt> Preallocation 预分配
 *
 *   <dd> You can override lazy thread construction policies via
 *   method createThreads, which establishes a given number of warm
 *   threads. Be aware that these preallocated threads will time out
 *   and die (and later be replaced with others if needed) if not used
 *   within the keep-alive time window. If you use preallocation, you
 *   probably want to increase the keepalive time.  The difference
 *   between setMinimumPoolSize and createThreads is that
 *   createThreads immediately establishes threads, while setting the
 *   minimum pool size waits until requests arrive.  <p>
 *   您可以通过方法createThreads覆盖懒加载线程构造策略，该方法将建立给定数量的热线程。
 *   请注意，如果未在keep-alive时间窗口内使用这些预分配的线程，它们将超时并回收（如果需要，以后将被其他线程替换）。
 *   如果使用预分配，则可能要增加保留时间。
 *   setMinimumPoolSize和createThreads之间的区别在于，createThreads立即建立线程，而setMinimumPoolSize则等待请求到达才会创建线程。<p>
 *
 *   <dt> Keep-alive time 保持活动时间
 *
 *   <dd> If the pool maintained references to a fixed set of threads
 *   in the pool, then it would impede garbage collection of otherwise
 *   idle threads. This would defeat the resource-management aspects
 *   of pools. One solution would be to use weak references.  However,
 *   this would impose costly and difficult synchronization issues.
 *   Instead, threads are simply allowed to terminate and thus be
 *   GCable if they have been idle for the given keep-alive time.  The
 *   value of this parameter represents a trade-off between GCability
 *   and construction time. In most current Java VMs, thread
 *   construction and cleanup overhead is on the order of
 *   milliseconds. The default keep-alive value is one minute, which
 *   means that the time needed to construct and then GC a thread is
 *   expended at most once per minute.
 *   <p>
 *   如果池维护了对池中一组固定线程的引用，那么它将阻止垃圾收集原本空闲的线程。 这将破坏池的资源管理方面。 一种解决方案是使用弱引用。
 *   但是，这将带来昂贵且困难的同步问题。
 *   取而代之的是，只要允许线程在给定的“保持活动”时间内处于空闲状态，就可以简单地终止它们并因此可以将其设为GCable。
 *   此参数的值表示GCability与构建时间之间的权衡。 在大多数当前的Java VM中，线程构造和清理开销约为毫秒。
 *   默认的“保持活动状态”值是一分钟，这意味着构造线程然后再GC所需的时间每分钟最多消耗一次。<p>
 *
 *   To establish worker threads permanently, use a <em>negative</em>
 *   argument to setKeepAliveTime.  <p>
 *   要永久建立工作线程，请使用 <em>negative</em>参数设置setKeepAliveTime。 <p>
 *
 *   <dt> Blocked execution policy 阻止执行策略
 *
 *   <dd> If the maximum pool size or queue size is bounded, then it
 *   is possible for incoming <code>execute</code> requests to
 *   block. There are four supported policies for handling this
 *   problem, and mechanics (based on the Strategy Object pattern) to
 *   allow others in subclasses: <p>
 *   如果最大池大小或队列大小是有界的，则传入的<code>execute</code>请求可能会阻塞。
 *   有四种支持的策略用于处理此问题，机制（基于“策略对象”模式）允许子类中的其他策略：
 *
 *   <dl>
 *     <dt> Run (the default) <p> 运行（默认）
 *     <dd> The thread making the <code>execute</code> request
 *          runs the task itself. This policy helps guard against lockup. <p>
 *          发出<code>execute</code>请求的线程将运行任务本身。 此策略有助于防止锁定。
 *     <dt> Wait <p>等待
 *     <dd> Wait until a thread becomes available.  This
 *          policy should, in general, not be used if the minimum number of
 *          of threads is zero, in which case a thread may never become
 *          available.<p>
 *          等待线程可用。通常，如果最小线程数为零，则不应使用此策略，在这种情况下，可能永远不会有线程可用。
 *     <dt> Abort <p>中止
 *     <dd> Throw a RuntimeException <p>抛出一个运行时异常
 *     <dt> Discard <p>丢弃
 *     <dd> Throw away the current request and return. <p>丢弃当前请求并返回。
 *     <dt> DiscardOldest <p>丢弃最早的
 *     <dd> Throw away the oldest request and return. <p>丢弃最早的请求并返回。
 *   </dl>
 *
 *   Other plausible policies include raising the maximum pool size
 *   after checking with some other objects that this is OK.  <p>
 *   其他可行的策略包括在与其他一些对象确认这没问题之后提高最大池大小。<p>
 *
 *   These cases can never occur if the maximum pool size is unbounded
 *   or the queue is unbounded. (In these cases you instead face
 *   potential resource exhaustion.)  The execute method does not
 *   throw any checked exceptions in any of these cases since any
 *   errors associated with them must normally be dealt with via
 *   handlers or callbacks. (Although in some cases, these might be
 *   associated with throwing unchecked exceptions.)  You may wish to
 *   add special implementations even if you choose one of the listed
 *   policies. For example, the supplied Discard policy does not
 *   inform the caller of the drop. You could add your own version
 *   that does so.  Since choice of policies is normally a system-wide
 *   decision, selecting a policy affects all calls to
 *   <code>execute</code>.  If for some reason you would instead like
 *   to make per-call decisions, you could add variant versions of the
 *   <code>execute</code> method (for example,
 *   <code>executeIfWouldNotBlock</code>) in subclasses.  <p>
 *   如果最大池大小没有限制，或队列是无界永远不会发生这些情况。（在这些情况下，您将面临潜在的资源枯竭问题。）
 *   在这些情况下，execute方法均不会引发任何检查的异常，因为与它们相关的任何错误通常都必须通过处理程序或回调进行处理。
 *   （尽管在某些情况下，这些可能与引发未经检查的异常有关。）即使您选择列出的策略之一，您也希望添加一些特殊的策略实现以便进行选择。
 *   例如，提供的“丢弃”策略不会将丢弃通知给呼叫者。您可以添加自己的版本。
 *   由于策略的选择通常是整个系统的决定，因此选择策略会影响所有<code>execute</code>的调用。
 *   如果出于某种原因您想按呼叫做出决定，则可以在子类中添加<code>execute</code>方法的变体版本
 *   （例如，<code>executeIfWouldNotBlock</code>）。 <p>
 *
 *   <dt> Thread construction parameters <p>线程构造参数
 *
 *   <dd> A settable ThreadFactory establishes each new thread.  By
 *   default, it merely generates a new instance of class Thread, but
 *   can be changed to use a Thread subclass, to set priorities,
 *   ThreadLocals, etc.  <p>
 *   可设置的ThreadFactory建立每个新线程。
 *   默认情况下，它仅生成Thread类的新实例，但可以更改为使用Thread子类，设置优先级，ThreadLocals等。<p>
 *
 *   <dt> Interruption policy <p>中断策略
 *
 *   <dd> Worker threads check for interruption after processing each
 *   command, and terminate upon interruption.  Fresh threads will
 *   replace them if needed. Thus, new tasks will not start out in an
 *   interrupted state due to an uncleared interruption in a previous
 *   task. Also, unprocessed commands are never dropped upon
 *   interruption. It would conceptually suffice simply to clear
 *   interruption between tasks, but implementation characteristics of
 *   interruption-based methods are uncertain enough to warrant this
 *   conservative strategy. It is a good idea to be equally
 *   conservative in your code for the tasks running within pools.
 *   <p>
 *   工作线程在处理每个命令后检查是否中断，并在中断后终止。如果需要，新线程将替换它们。
 *   因此，由于先前任务中未清除的中断，新任务将不会以中断状态开始。同样，未处理的命令也不会在中断时被丢弃。
 *   从概念上讲，只需清除任务之间的中断就足够了，但是基于中断的方法的实现特性尚不确定，足以保证采取这种保守的策略。
 *   对于池中运行的任务，在代码中保持同样的保守是一个好主意。<p>
 *
 *   <dt> Shutdown policy <p>关机策略
 *
 *   <dd> The interruptAll method interrupts, but does not disable the
 *   pool. Two different shutdown methods are supported for use when
 *   you do want to (permanently) stop processing tasks. Method
 *   shutdownAfterProcessingCurrentlyQueuedTasks waits until all
 *   current tasks are finished. The shutDownNow method interrupts
 *   current threads and leaves other queued requests unprocessed.
 *   <p>
 *   interruptAll方法会中断，但不会禁用池。当您确实想（永久）停止处理任务时，支持使用两种不同的关闭方法。
 *   方法shutdownAfterProcessingCurrentlyQueuedTasks等待直到所有当前任务完成。
 *   shutDownNow方法中断当前线程，并使其他排队的请求未处理。<p>
 *
 *   <dt> Handling requests after shutdown <p>关闭后处理请求
 *
 *   <dd> When the pool is shutdown, new incoming requests are handled
 *   by the blockedExecutionHandler. By default, the handler is set to
 *   discard new requests, but this can be set with an optional
 *   argument to method
 *   shutdownAfterProcessingCurrentlyQueuedTasks. <p> Also, if you are
 *   using some form of queuing, you may wish to call method drain()
 *   to remove (and return) unprocessed commands from the queue after
 *   shutting down the pool and its clients. If you need to be sure
 *   these commands are processed, you can then run() each of the
 *   commands in the list returned by drain().<p>
 *   当池关闭时，新的传入请求由blockedExecutionHandler处理。
 *   默认情况下，该处理程序设置为丢弃新请求，但是可以使用方法shutdownAfterProcessingCurrentlyQueuedTasks的可选参数来设置它。<p>
 *   另外，如果您使用某种形式的排队，则可能希望在关闭池及其客户端之后，调用方法drain()从队列中删除（并返回）未处理的命令。
 *   如果需要处理这些命令，则可以使用run()来执行drain()返回的每个命令。
 *
 * </dl>
 * <p>
 *
 * <b>Usage examples.</b>
 * <b>用法示例</b>
 * <p>
 *
 * Probably the most common use of pools is in statics or singletons
 * accessible from a number of classes in a package; for example:<p>
 * 池的最常见用法是从包中的多个类访问的静态变量或单例变量；例如：
 *
 * <pre>
 * class MyPool {
 *   // initialize to use a maximum of 8 threads.
 *   // 初始化设置，最大线程为8。
 *   static PooledExecutor pool = new PooledExecutor(8);
 * }
 * </pre>
 * Here are some sample variants in initialization:<p>
 * 以下是初始化的一些示例变体：
 * <ol>
 *  <li> Using a bounded buffer of 10 tasks, at least 4 threads (started only
 *       when needed due to incoming requests), but allowing
 *       up to 100 threads if the buffer gets full.<p>
 *       使用10个任务的有界缓冲区，至少要存在4个线程（仅由于传入请求而在需要时才启动），但如果缓冲区已满，则最多允许100个线程。
 *     <pre>
 *        pool = new PooledExecutor(new BoundedBuffer(10), 100);
 *        pool.setMinimumPoolSize(4);
 *     </pre>
 *  <li> Same as (1), except pre-start 9 threads, allowing them to
 *        die if they are not used for five minutes.<p>
 *        与（1）相同，只是预启动了9个线程，如果它们在五分钟内不使用，它们将被回收。
 *     <pre>
 *        pool = new PooledExecutor(new BoundedBuffer(10), 100);
 *        pool.setMinimumPoolSize(4);
 *        pool.setKeepAliveTime(1000 * 60 * 5);
 *        pool.createThreads(9);
 *     </pre>
 *  <li> Same as (2) except clients abort if both the buffer is full and
 *       all 100 threads are busy:<p>
 *       与（2）相同，不同之处在于如果两个缓冲区都已满并且所有100个线程都忙，则客户端将中止：
 *     <pre>
 *        pool = new PooledExecutor(new BoundedBuffer(10), 100);
 *        pool.setMinimumPoolSize(4);
 *        pool.setKeepAliveTime(1000 * 60 * 5);
 *        pool.abortWhenBlocked();
 *        pool.createThreads(9);
 *     </pre>
 *  <li> An unbounded queue serviced by exactly 5 threads:<p>
 *  由5个线程服务的无限队列：
 *     <pre>
 *        pool = new PooledExecutor(new LinkedQueue());
 *        pool.setKeepAliveTime(-1); // live forever
 *        pool.createThreads(5);
 *     </pre>
 *  </ol>
 *
 * <p>
 * <b>Usage notes.</b>
 * <b>使用说明</b>
 * <p>
 *
 * Pools do not mesh well with using thread-specific storage via
 * java.lang.ThreadLocal.  ThreadLocal relies on the identity of a
 * thread executing a particular task. Pools use the same thread to
 * perform different tasks.  <p>
 * 池不能很好地通过 java.lang.ThreadLocal.ThreadLocal 使用特定于线程的存储，依赖于执行特定任务的线程的标识。
 * 池使用相同的线程来执行不同的任务。<p>
 *
 * If you need a policy not handled by the parameters in this class
 * consider writing a subclass.  <p>
 * 如果您需要一个此类中的参数无法处理的策略，请考虑编写一个子类。<p>
 *
 * Version note: Previous versions of this class relied on
 * ThreadGroups for aggregate control. This has been removed, and the
 * method interruptAll added, to avoid differences in behavior across
 * JVMs.<p>
 * 版本说明：此类的早期版本依赖ThreadGroups进行聚合控制。
 * 为了避免跨JVM的行为差异，已将其删除，并添加了方法interruptAll。
 *
 * <p>[<a href="http://gee.cs.oswego.edu/dl/classes/EDU/oswego/cs/dl/util/concurrent/intro.html"> Introduction to this package. </a>]
 * <p>[<a href="http://gee.cs.oswego.edu/dl/classes/EDU/oswego/cs/dl/util/concurrent/intro.html"> 此软件包的简介 </a>]
 **/

public class PooledExecutor extends ThreadFactoryUser implements Executor {

    /**
     * The maximum pool size; used if not otherwise specified.  Default
     * value is essentially infinite (Integer.MAX_VALUE)<p>
     * 最大池大小；除非另有说明，否则使用默认值。本质上是无限的（Integer.MAX_VALUE）
     **/
    public static final int  DEFAULT_MAXIMUMPOOLSIZE = Integer.MAX_VALUE;

    /**
     * The minimum pool size; used if not otherwise specified.  Default
     * value is 1.<p>
     * 最小池大小；除非另有说明，否则使用预设值，为1。
     **/
    public static final int  DEFAULT_MINIMUMPOOLSIZE = 1;

    /**
     * The maximum time to keep worker threads alive waiting for new
     * tasks; used if not otherwise specified. Default value is one
     * minute (60000 milliseconds).<p>
     * 使工作线程保持活动状态以等待新任务的最长时间；除非另有说明，否则使用默认值，为一分钟（60000毫秒）。
     **/
    public static final long DEFAULT_KEEPALIVETIME = 60 * 1000;

    /**
     * The maximum number of threads allowed in pool.<p>
     * 池中允许的最大线程数
     */
    protected int maximumPoolSize = DEFAULT_MAXIMUMPOOLSIZE;

    /**
     * The minumum number of threads to maintain in pool.<p>
     * 池中要维护的最小线程数。
     */
    protected int minimumPoolSize = DEFAULT_MINIMUMPOOLSIZE;

    /**
     * Current pool size.<p>
     * 当前池的大小
     */
    protected int poolSize = 0;

    /**
     * The maximum time for an idle thread to wait for new task.<p>
     * 空闲线程等待新任务的最长时间。
     */
    protected long keepAliveTime = DEFAULT_KEEPALIVETIME;

    /**
     * Shutdown flag - latches true when a shutdown method is called
     * in order to disable queuing/handoffs of new tasks.<p>
     * 关闭标志 - 在调用关闭方法时锁定 true，以禁用新任务的排队/切换。
     **/
    protected boolean shutdown = false;

    /**
     * The channel used to hand off the command to a thread in the pool.<p>
     * 用于将命令移交给池中线程的通道。
     **/
    protected final Channel handOff;

    /**
     * The set of active threads, declared as a map from workers to
     * their threads.  This is needed by the interruptAll method.  It
     * may also be useful in subclasses that need to perform other
     * thread management chores.<p>
     * 活动线程集，声明为从工作线程到其线程的映射。 中断所有方法需要此参数。
     * 在需要执行其他线程管理杂务的子类中，它也可能很有用。
     **/
    protected final Map threads;

    /**
     * The current handler for unserviceable requests.<p>
     * 处理当前无法请求的程序。
     **/
    protected BlockedExecutionHandler blockedExecutionHandler;

    /**
     * Create a new pool with all default settings<p>
     * 创建具有所有默认设置的新池
     **/

    public PooledExecutor() {
        this (new SynchronousChannel(), DEFAULT_MAXIMUMPOOLSIZE);
    }

    /**
     * Create a new pool with all default settings except
     * for maximum pool size.<p>
     * 使用最大池大小以外的所有默认设置创建一个新池。
     **/

    public PooledExecutor(int maxPoolSize) {
        this(new SynchronousChannel(), maxPoolSize);
    }

    /**
     * Create a new pool that uses the supplied Channel for queuing, and
     * with all default parameter settings.<p>
     * 创建一个使用提供的通道进行排队以及使用其他所有默认参数设置的新池。
     **/

    public PooledExecutor(Channel channel) {
        this(channel, DEFAULT_MAXIMUMPOOLSIZE);
    }

    /**
     * Create a new pool that uses the supplied Channel for queuing, and
     * with all default parameter settings except for maximum pool size.
     * <p>创建一个使用提供的通道进行排队的新池，并使用除最大池大小外的所有默认参数设置。
     **/

    public PooledExecutor(Channel channel, int maxPoolSize) {
        maximumPoolSize = maxPoolSize;
        handOff = channel;
        runWhenBlocked();
        threads = new HashMap();
    }

    /**
     * Return the maximum number of threads to simultaneously execute
     * New unqueued requests will be handled according to the current
     * blocking policy once this limit is exceeded.
     * <p>一旦超过此限制，将根据当前的阻塞策略处理新的未排队请求，并返回同时执行的最大线程数。
     **/
    public synchronized int getMaximumPoolSize() {
        return maximumPoolSize;
    }

    /**
     * Set the maximum number of threads to use. Decreasing the pool
     * size will not immediately kill existing threads, but they may
     * later die when idle.
     * <p>设置要使用的最大线程数。减少池大小不会立即杀死现有的线程，但它们可能会在空闲时死亡。
     * @exception IllegalArgumentException if less or equal to zero.
     * (It is
     * not considered an error to set the maximum to be less than than
     * the minimum. However, in this case there are no guarantees
     * about behavior.)
     * <p>如果小于或等于零（将最大值设置为小于最小值并不认为是错误。但是，在这种情况下，不能保证行为）
     **/
    public synchronized void setMaximumPoolSize(int newMaximum) {
        if (newMaximum <= 0) throw new IllegalArgumentException();
        maximumPoolSize = newMaximum;
    }

    /**
     * Return the minimum number of threads to simultaneously execute.
     * (Default value is 1).  If fewer than the mininum number are
     * running upon reception of a new request, a new thread is started
     * to handle this request.
     * <p>返回要同时执行的最少线程数(默认值是1)。
     * 如果接收到一个新请求时，运行的最小数量小于这个数，则会启动一个新的线程来处理这个请求。
     **/
    public synchronized int getMinimumPoolSize() {
        return minimumPoolSize;
    }

    /**
     * Set the minimum number of threads to use.
     * <p>设置要使用的最小线程数。
     * @exception IllegalArgumentException if less than zero. (It is not
     * considered an error to set the minimum to be greater than the
     * maximum. However, in this case there are no guarantees about
     * behavior.)
     * <p>如果小于零（将最小值设置为大于最大值并不认为是错误的。但是，在这种情况下，无法保证行为）
     **/
    public synchronized void setMinimumPoolSize(int newMinimum) {
        if (newMinimum < 0) throw new IllegalArgumentException();
        minimumPoolSize = newMinimum;
    }

    /**
     * Return the current number of active threads in the pool.  This
     * number is just a snaphot, and may change immediately upon
     * returning
     * <p>返回池中的当前活动线程数。这个数字只是一个快照，返回时可能会立即更改
     **/
    public synchronized int getPoolSize() {
        return poolSize;
    }

    /**
     * Return the number of milliseconds to keep threads alive waiting
     * for new commands. A negative value means to wait forever. A zero
     * value means not to wait at all.
     * <p>返回保持线程处于活动状态等待新命令的毫秒数。负值意味着永远等待。零值意味着根本不需要等待。
     **/
    public synchronized long getKeepAliveTime() {
        return keepAliveTime;
    }

    /**
     * Set the number of milliseconds to keep threads alive waiting for
     * new commands. A negative value means to wait forever. A zero
     * value means not to wait at all.
     * <p>设置线程等待新命令时的毫秒数。负值意味着永远等待。零值意味着根本不需要等待。
     **/
    public synchronized void setKeepAliveTime(long msecs) {
        keepAliveTime = msecs;
    }

    /**
     *  Get the handler for blocked execution
     *  <p>获取阻塞执行的处理程序
     **/
    public synchronized BlockedExecutionHandler getBlockedExecutionHandler() {
        return blockedExecutionHandler;
    }

    /**
     * Set the handler for blocked execution
     * <p>设置处理程序以阻止执行
     **/
    public synchronized void setBlockedExecutionHandler(BlockedExecutionHandler h) {
        blockedExecutionHandler = h;
    }

    /**
     * Create and start a thread to handle a new command.  Call only
     * when holding lock.
     * <p>创建并启动一个线程来处理一个新命令。只有在保持锁定时才调用。
     **/
    protected void addThread(Runnable command) {
        Worker worker = new Worker(command);
        Thread thread = getThreadFactory().newThread(worker);
        threads.put(worker, thread);
        ++poolSize;
        thread.start();
    }

    /**
     * Create and start up to numberOfThreads threads in the pool.
     * Return the number created. This may be less than the number
     * requested if creating more would exceed maximum pool size bound.
     * <p>在池中创建并启动numberOfThreads线程。返回创建的数字。
     * 如果创建超过最大池大小限制的线程数目，那么这个创建数可能小于请求的数目。
     **/
    public int createThreads(int numberOfThreads) {
        int ncreated = 0;
        for (int i = 0; i < numberOfThreads; ++i) {
            synchronized(this) {
                if (poolSize < maximumPoolSize) {
                    addThread(null);
                    ++ncreated;
                }
                else
                    break;
            }
        }
        return ncreated;
    }

    /**
     * Interrupt all threads in the pool, causing them all to
     * terminate. Assuming that executed tasks do not disable (clear)
     * interruptions, each thread will terminate after processing its
     * current task. Threads will terminate sooner if the executed tasks
     * themselves respond to interrupts.
     * <p>中断线程池中的所有线程，导致它们全部终止。
     * 假设正在执行的任务没有禁用(清除)中断，每个线程将在处理其当前任务后终止。
     * 如果执行的任务本身响应中断，那么线程将更快终止。
     **/
    public synchronized void interruptAll() {
        for (Object o : threads.values()) {
            Thread t = (Thread) o;
            t.interrupt();
        }
    }

    /**
     * Interrupt all threads and disable construction of new
     * threads. Any tasks entered after this point will be discarded. A
     * shut down pool cannot be restarted.
     * <p>中断所有线程并禁用新线程的构建。在此之后输入的所有任务将被丢弃。已关闭的池无法重新启动。
     */
    public void shutdownNow() {
        shutdownNow(new DiscardWhenBlocked());
    }

    /**
     * Interrupt all threads and disable construction of new
     * threads. Any tasks entered after this point will be handled by
     * the given BlockedExecutionHandler.  A shut down pool cannot be
     * restarted.
     * <p>中断所有线程并禁用新线程的构建。
     * 在此之后输入的所有任务将由给定的BlockedExecutionHandler处理。已关闭的池无法重新启动。
     */
    public synchronized void shutdownNow(BlockedExecutionHandler handler) {
        setBlockedExecutionHandler(handler);
        shutdown = true; // don't allow new tasks  不允许新任务
        minimumPoolSize = maximumPoolSize = 0; // don't make new threads 不要建立新线程
        interruptAll(); // interrupt all existing threads 中断所有现有线程
    }

    /**
     * Terminate threads after processing all elements currently in
     * queue. Any tasks entered after this point will be discarded. A
     * shut down pool cannot be restarted.
     * <p>处理完当前队列中的所有任务后终止线程。在此之后输入的任何任务都将被丢弃。已关闭的池不能重新启动。
     **/
    public void shutdownAfterProcessingCurrentlyQueuedTasks() {
        shutdownAfterProcessingCurrentlyQueuedTasks(new DiscardWhenBlocked());
    }

    /**
     * Terminate threads after processing all elements currently in
     * queue. Any tasks entered after this point will be handled by the
     * given BlockedExecutionHandler.  A shut down pool cannot be
     * restarted.
     * <p>处理完当前队列中的所有元素后终止线程。
     * 在此点之后输入的任何任务都将由给定的BlockedExecutionHandler处理。已关闭的池不能重新启动。
     **/
    public synchronized void shutdownAfterProcessingCurrentlyQueuedTasks(BlockedExecutionHandler handler) {
        setBlockedExecutionHandler(handler);
        shutdown = true;
        if (poolSize == 0) // disable new thread construction when idle 在空闲时不再创建新线程
            minimumPoolSize = maximumPoolSize = 0;
    }

    /**
     * Return true if a shutDown method has succeeded in terminating all
     * threads.
     * <p>如果shutDown方法成功地终止了所有线程，则返回true。
     */
    public synchronized boolean isTerminatedAfterShutdown() {
        return shutdown && poolSize == 0;
    }

    /**
     * Wait for a shutdown pool to fully terminate, or until the timeout
     * has expired. This method may only be called <em>after</em>
     * invoking shutdownNow or
     * shutdownAfterProcessingCurrentlyQueuedTasks.
     * <p>等待关闭池完全终止，或直到超时已过期。
     * 这个方法只能在调用shutdownNow或shutdownafterprocessingcurrentqueuedtasks<em>后</em>调用。
     *
     * @param maxWaitTime  the maximum time in milliseconds to wait
     *                     <p>等待的最大时间(以毫秒为单位)
     * @return true if the pool has terminated within the max wait period
     * <p>如果池在最大等待时间内终止，则为true
     * @exception IllegalStateException if shutdown has not been requested
     * <p>如果没有请求关闭
     * @exception InterruptedException if the current thread has been interrupted in the course of waiting
     * <p>如果当前线程在等待过程中被中断
     */
    public synchronized boolean awaitTerminationAfterShutdown(long maxWaitTime) throws InterruptedException {
        if (!shutdown)
            throw new IllegalStateException();
        if (poolSize == 0)
            return true;
        long waitTime = maxWaitTime;
        if (waitTime <= 0)
            return false;
        long start = System.currentTimeMillis();
        for (;;) {
            wait(waitTime);
            if (poolSize == 0)
                return true;
            waitTime = maxWaitTime - (System.currentTimeMillis() - start);
            if (waitTime <= 0)
                return false;
        }
    }

    /**
     * Wait for a shutdown pool to fully terminate.  This method may
     * only be called <em>after</em> invoking shutdownNow or
     * shutdownAfterProcessingCurrentlyQueuedTasks.
     * <p>等待关闭池完全终止。这个方法只能在调用shutdownNow或shutdownafterprocessingcurrentqueuedtasks<em>后</em>调用。
     *
     * @exception IllegalStateException if shutdown has not been requested
     * <p>如果没有请求关闭
     * @exception InterruptedException if the current thread has been interrupted in the course of waiting
     * <p>如果当前线程在等待过程中被中断
     */
    public synchronized void awaitTerminationAfterShutdown() throws InterruptedException {
        if (!shutdown)
            throw new IllegalStateException();
        while (poolSize > 0)
            wait();
    }

    /**
     * Remove all unprocessed tasks from pool queue, and return them in
     * a java.util.List. Thsi method should be used only when there are
     * not any active clients of the pool. Otherwise you face the
     * possibility that the method will loop pulling out tasks as
     * clients are putting them in.  This method can be useful after
     * shutting down a pool (via shutdownNow) to determine whether there
     * are any pending tasks that were not processed.  You can then, for
     * example execute all unprocessed commands via code along the lines
     * of:
     * <p>从池队列中删除所有未处理的任务，并将其返回到java.util.List中。
     * 仅当池中没有任何活动客户端时，才应使用此方法。
     * 否则，您可能会遇到该方法将在客户端放入任务时循环拉出任务的情况。
     * 关闭池（通过shutdownNow）以确定是否有未处理的待处理任务后，此方法很有用。
     * 然后，您可以例如通过以下代码通过代码执行所有未处理的命令：
     *
     * <pre>
     *   List tasks = pool.drain();
     *   for (Iterator it = tasks.iterator(); it.hasNext();)
     *     ( (Runnable)(it.next()) ).run();
     * </pre>
     **/
    public List drain() {
        boolean wasInterrupted = false;
        Vector tasks = new Vector();
        for (;;) {
            try {
                Object x = handOff.poll(0);
                if (x == null)
                    break;
                else
                    tasks.addElement(x);
            }
            catch (InterruptedException ex) {
                wasInterrupted = true; // postpone re-interrupt until drained 推迟再次中断直到耗尽
            }
        }
        if (wasInterrupted) Thread.currentThread().interrupt();
        return tasks;
    }

    /**
     * Cleanup method called upon termination of worker thread.
     * <p>在工作线程终止时调用的清除方法。
     **/
    protected synchronized void workerDone(Worker w) {
        threads.remove(w);
        if (--poolSize == 0 && shutdown) {
            maximumPoolSize = minimumPoolSize = 0; // disable new threads 不再创建新线程
            notifyAll(); // notify awaitTerminationAfterShutdown 通知awaitTerminationAfterShutdown
        }

        // Create a replacement if needed
        // 如果需要，创建一个替代品
        if (poolSize == 0 || poolSize < minimumPoolSize) {
            try {
                Runnable r = (Runnable)(handOff.poll(0));
                if (r != null && !shutdown) // just consume task if shut down 如果关闭则只消耗任务
                    addThread(r);
            } catch(InterruptedException ie) {
                ie.printStackTrace();
            }
        }
    }

    /**
     * Get a task from the handoff queue, or null if shutting down.
     * <p>从切换队列中获取任务，如果关闭则为null。
     **/
    protected Runnable getTask() throws InterruptedException {
        long waitTime;
        synchronized(this) {
            if (poolSize > maximumPoolSize) // Cause to die if too many threads 如果线程太多会导致死亡
                return null;
            waitTime = (shutdown)? 0 : keepAliveTime;
        }
        if (waitTime >= 0)
            return (Runnable)(handOff.poll(waitTime));
        else
            return (Runnable)(handOff.take());
    }


    /**
     * Class defining the basic run loop for pooled threads.
     * <p>定义池线程的基本运行循环的类
     **/
    protected class Worker implements Runnable {
        protected Runnable firstTask;

        protected Worker(Runnable firstTask) { this.firstTask = firstTask; }

        public void run() {
            try {
                Runnable task = firstTask;
                firstTask = null; // enable GC 允许垃圾回收

                if (task != null) {
                    task.run();
                    task = null;
                }

                while ( (task = getTask()) != null) {
                    task.run();
                    task = null;
                }
            }
            catch (InterruptedException ex) {
                ex.printStackTrace();
            } // fall through 失败
            finally {
                workerDone(this);
            }
        }
    }

    /**
     * Class for actions to take when execute() blocks. Uses Strategy
     * pattern to represent different actions. You can add more in
     * subclasses, and/or create subclasses of these. If so, you will
     * also want to add or modify the corresponding methods that set the
     * current blockedExectionHandler_.
     * <p>类用于在execute()阻塞时采取的操作。使用策略模式来表示不同的操作。
     * 你可以在子类中添加更多，或者创建它们的子类。
     * 如果需要，还可以添加或修改相应的方法来设置当前的blockedExectionHandler_。
     **/
    public interface BlockedExecutionHandler {
        /**
         * Return true if successfully handled so, execute should
         * terminate; else return false if execute loop should be retried.
         * <p>如果成功处理，则返回true，execute应该终止;否则，如果需要重试执行循环，则返回false。
         **/
        boolean blockedAction(Runnable command) throws InterruptedException;
    }

    /**
     * Class defining Run action.
     * <p>定义运行动作的类。
     **/
    protected static class RunWhenBlocked implements BlockedExecutionHandler {
        public boolean blockedAction(Runnable command) {
            command.run();
            return true;
        }
    }

    /**
     * Set the policy for blocked execution to be that the current
     * thread executes the command if there are no available threads in
     * the pool.
     * <p>将阻塞执行的策略设置为，如果池中没有可用的线程，则当前线程执行该命令。
     **/
    public void runWhenBlocked() {
        setBlockedExecutionHandler(new RunWhenBlocked());
    }

    /**
     * Class defining Wait action.
     * <p><p>定义等待动作的类。
     **/
    protected class WaitWhenBlocked implements BlockedExecutionHandler {
        public boolean blockedAction(Runnable command) throws InterruptedException{
            synchronized(PooledExecutor.this) {
                if (shutdown)
                    return true;
            }
            handOff.put(command);
            return true;
        }
    }

    /**
     * Set the policy for blocked execution to be to wait until a thread
     * is available, unless the pool has been shut down, in which case
     * the action is discarded.
     * <p>将阻塞执行的策略设置为等待线程可用，除非已关闭该池（在这种情况下将放弃该操作）
     **/
    public void waitWhenBlocked() {
        setBlockedExecutionHandler(new WaitWhenBlocked());
    }

    /**
     * Class defining Discard action.
     * <p>定义丢弃动作的类
     **/
    protected static class DiscardWhenBlocked implements BlockedExecutionHandler {
        public boolean blockedAction(Runnable command) {
            return true;
        }
    }

    /**
     * Set the policy for blocked execution to be to return without
     * executing the request.
     * <p>将阻止执行的策略设置为不执行请求就返回
     **/
    public void discardWhenBlocked() {
        setBlockedExecutionHandler(new DiscardWhenBlocked());
    }


    /**
     * Class defining Abort action.
     * <p>定义中止动作的类
     **/
    protected static class AbortWhenBlocked implements BlockedExecutionHandler {
        public boolean blockedAction(Runnable command) {
            throw new RuntimeException("Pool is blocked");
        }
    }

    /**
     * Set the policy for blocked execution to be to
     * throw a RuntimeException.
     * <p>将阻塞执行的策略设置为抛出RuntimeException
     **/
    public void abortWhenBlocked() {
        setBlockedExecutionHandler(new AbortWhenBlocked());
    }


    /**
     * Class defining DiscardOldest action.  Under this policy, at most
     * one old unhandled task is discarded.  If the new task can then be
     * handed off, it is.  Otherwise, the new task is run in the current
     * thread (i.e., RunWhenBlocked is used as a backup policy.)
     * <p>定义DiscardOldest动作的类。
     * 在此策略下，最多丢弃一个旧的未处理任务。
     * 如果新任务可以执行，那就可以了。
     * 否则，新任务将在当前线程中运行（即，RunWhenBlocked用作备份策略。）
     **/
    protected class DiscardOldestWhenBlocked implements BlockedExecutionHandler {
        public boolean blockedAction(Runnable command) throws InterruptedException{
            handOff.poll(0);
            if (!handOff.offer(command, 0))
                command.run();
            return true;
        }
    }

    /**
     * Set the policy for blocked execution to be to discard the oldest
     * unhandled request
     * <p>将阻止执行的策略设置为丢弃最早的未处理请求
     **/
    public void discardOldestWhenBlocked() {
        setBlockedExecutionHandler(new DiscardOldestWhenBlocked());
    }

    /**
     * Arrange for the given command to be executed by a thread in this
     * pool.  The method normally returns when the command has been
     * handed off for (possibly later) execution.
     * <p>安排此池中的线程执行给定的命令。该方法通常在命令已分发以执行（可能稍后）时返回。
     **/
    public void execute(Runnable command) throws InterruptedException {
        for (;;) {
            synchronized(this) {
                if (!shutdown) {
                    int size = poolSize;

                    // Ensure minimum number of threads
                    // 确保线程数最少
                    if (size < minimumPoolSize) {
                        addThread(command);
                        return;
                    }

                    // Try to give to existing thread
                    // 尝试赋予现有线程
                    if (handOff.offer(command, 0)) {
                        return;
                    }

                    // If cannot handoff and still under maximum, create new thread
                    // 如果不能切换并且仍然低于最大值，则创建新线程
                    if (size < maximumPoolSize) {
                        addThread(command);
                        return;
                    }
                }
            }

            // Cannot hand off and cannot create -- ask for help
            // 无法hand off也无法创建-寻求帮助
            if (getBlockedExecutionHandler().blockedAction(command)) {
                return;
            }
        }
    }
}
