/*
  File: ThreadFactory.java

  Originally written by Doug Lea and released into the public domain.
  This may be used for any purposes whatsoever without acknowledgment.
  Thanks for the assistance and support of Sun Microsystems Labs,
  and everyone contributing, testing, and using this code.

  History:
  Date       Who                What
  30Jun1998  dl               Create public version
*/

package de.simplicit.vjdbc.server.concurrent;

/**
 * Interface describing any class that can generate
 * new Thread objects. Using ThreadFactories removes
 * hardwiring of calls to <code>new Thread</code>, enabling
 * applications to use special thread subclasses, default
 * prioritization settings, etc.<p>
 * 描述可以生成新Thread对象的任何类的接口。
 * 使用ThreadFactories可以消除对<code>new Thread</code>的繁琐调用，从而使应用程序可以使用特殊的线程子类，默认优先级设置等。
 * <p>
 * [<a href="http://gee.cs.oswego.edu/dl/classes/EDU/oswego/cs/dl/util/concurrent/intro.html"> Introduction to this package. </a>] <p>
 * [<a href="http://gee.cs.oswego.edu/dl/classes/EDU/oswego/cs/dl/util/concurrent/intro.html"> 此软件包的简介 </a>] <p>
 **/

public interface ThreadFactory {
    /**
     * Create a new thread that will run the given command when started
     * 创建一个新线程，该线程在启动时将运行给定命令
     **/
    Thread newThread(Runnable command);
}
