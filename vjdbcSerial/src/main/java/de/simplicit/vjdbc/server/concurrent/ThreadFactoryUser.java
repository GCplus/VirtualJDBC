/*
  File: ThreadFactoryUser.java

  Originally written by Doug Lea and released into the public domain.
  This may be used for any purposes whatsoever without acknowledgment.
  Thanks for the assistance and support of Sun Microsystems Labs,
  and everyone contributing, testing, and using this code.
  最初由Doug Lea编写，并已发布到公共领域。可以未经许可用于任何目的。
  感谢Sun微系统实验室以及所有贡献、测试和使用此代码的人们的帮助和支持。

  History:
  Date       Who                What
  28aug1998  dl               refactored from Executor classes
*/

package de.simplicit.vjdbc.server.concurrent;

/**
 *
 * Base class for Executors and related classes that rely on thread factories.
 * Generally intended to be used as a mixin-style abstract class, but
 * can also be used stand-alone.<p>
 * 依赖线程工厂的执行器和相关类的基类。通常用作混合式抽象类，但也可用于独立。
 * <p>[<a href="http://gee.cs.oswego.edu/dl/classes/EDU/oswego/cs/dl/util/concurrent/intro.html"> Introduction to this package. </a>]
 * <p>[<a href="http://gee.cs.oswego.edu/dl/classes/EDU/oswego/cs/dl/util/concurrent/intro.html"> 此软件包的简介 </a>]
 **/

public class ThreadFactoryUser {

    protected ThreadFactory threadFactory = new DefaultThreadFactory();

    protected static class DefaultThreadFactory implements ThreadFactory {
        public Thread newThread(Runnable command) {
            return new Thread(command);
        }
    }

    /**
     * Set the factory for creating new threads.
     * By default, new threads are created without any special priority,
     * threadgroup, or status parameters.
     * You can use a different factory
     * to change the kind of Thread class used or its construction
     * parameters.<p>
     * 设置用于创建新线程的工厂方法。默认情况下，创建新线程时没有任何特殊的优先级，线程组或状态参数。
     * 您可以使用其他工厂方法来更改使用的 Thread 类类型或其构造参数。
     * @param factory the factory to use，要使用的工厂类
     * @return the previous factory 上一个工厂方法
     **/

    public synchronized ThreadFactory setThreadFactory(ThreadFactory factory) {
        ThreadFactory old = threadFactory;
        threadFactory = factory;
        return old;
    }

    /**
     * Get the factory for creating new threads.
     * 获取用于创建新线程的工厂。
     **/
    public synchronized ThreadFactory getThreadFactory() {
        return threadFactory;
    }

}
