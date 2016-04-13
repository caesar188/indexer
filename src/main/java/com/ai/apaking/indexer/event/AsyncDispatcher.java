package com.ai.apaking.indexer.event;

import com.ai.apaking.indexer.exception.APakingRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 类型说明: 异步的事件分发器类.
 *
 * @author guh
 *         create time 2016/4/11 14:48
 */
public class AsyncDispatcher implements IDispatcher {

    private static final Logger LOG = LoggerFactory.getLogger(AsyncDispatcher.class);

    private final BlockingQueue<IEvent> eventQueue;
    private volatile boolean stopped = false;

    // Configuration flag for enabling/disabling draining dispatcher's events on
    // stop functionality.
    private volatile boolean drainEventsOnStop = false;

    // Indicates all the remaining dispatcher's events on stop have been drained
    // and processed.
    private volatile boolean drained = true;
    private final Object waitForDrained = new Object();

    // For drainEventsOnStop enabled only, block newly coming events into the
    // queue while stopping.
    private volatile boolean blockNewEvents = false;
    private IEventHandler handlerInstance = null;

    private Thread eventHandlingThread;
    protected final Map<Class<? extends Enum>, IEventHandler> eventDispatchers;
    private final boolean exitOnDispatchException = true;

    /**
     * 无参构造函数.
     */
    public AsyncDispatcher() {
        this(new LinkedBlockingQueue<IEvent>());
    }

    /**
     * 有参数的构造函数.
     *
     * @param eventQueue 阻塞队列
     */
    public AsyncDispatcher(final BlockingQueue<IEvent> eventQueue) {
        this.eventQueue = eventQueue;
        this.eventDispatchers = new HashMap<>();
    }

    /**
     * 生成一个事件分发线程.
     *
     * @return
     */
    Runnable createThread() {
        return new Runnable() {
            @Override
            public void run() {
                while (!stopped && !Thread.currentThread().isInterrupted()) {
                    drained = eventQueue.isEmpty();
                    // blockNewEvents is only set when dispatcher is draining to
                    // stop,
                    // adding this check is to avoid the overhead of acquiring
                    // the lock
                    // and calling notify every time in the normal run of the
                    // loop.
                    if (blockNewEvents) {
                        synchronized (waitForDrained) {
                            if (drained) {
                                waitForDrained.notify();
                            }
                        }
                    }
                    IEvent event;
                    try {
                        event = eventQueue.take();
                    } catch (InterruptedException ie) {
                        if (!stopped) {
                            LOG.warn("AsyncDispatcher thread interrupted", ie);
                        }
                        return;
                    }
                    if (event != null) {
                        dispatch(event);
                    }
                }
            }
        };
    }

    /**
     * 分发器启动.
     *
     * @throws Exception 异常
     */
    public void serviceStart() throws Exception {
        // start all the components
        eventHandlingThread = new Thread(createThread());
        eventHandlingThread.setName("AsyncDispatcher event handler");
        eventHandlingThread.start();
    }

    /**
     * 设为停止服务时，需等待所有事件分发完.
     */
    public void setDrainEventsOnStop() {
        drainEventsOnStop = true;
    }

    /**
     * 停止分发器:当然遇到drainEventsOnStop=true时，等待事件分发.
     *
     * @throws Exception 抛出 Exception
     */
    public void serviceStop() throws Exception {
        if (drainEventsOnStop) {
            blockNewEvents = true;
            LOG.info("AsyncDispatcher is draining to stop, igonring any new events.");
            synchronized (waitForDrained) {
                while (!drained && eventHandlingThread.isAlive()) {
                    waitForDrained.wait(1000);
                    LOG.info("Waiting for AsyncDispatcher to drain.");
                }
            }
        }
        stopped = true;
        if (eventHandlingThread != null) {
            eventHandlingThread.interrupt();
            try {
                eventHandlingThread.join();
            } catch (InterruptedException ie) {
                LOG.warn("Interrupted Exception while stopping", ie);
            }
        }
    }

    /**
     * Dispatch events .
     *
     * @param event 事件
     */
    protected void dispatch(final IEvent event) {
        // all events go throw this loop

        if (LOG.isDebugEnabled()) {
            LOG.debug("Dispatching the event " + event.getClass().getName()
                    + "." + event.toString());
        }

        Class<? extends Enum> type = event.getType().getDeclaringClass();

        try {
            IEventHandler handler = eventDispatchers.get(type);
            if (handler != null) {
                handler.handle(event);
            } else {
                throw new Exception("No handler for registered for " + type);
            }
        } catch (Throwable t) {
            LOG.warn("Error in dispatcher thread", t);

            if (exitOnDispatchException) {
                LOG.info("Exiting, bye..");
                // System.exit(-1);
            }
        }
    }

    @Override
    public void register(final Class<? extends Enum> eventType,
                         final IEventHandler handler) {
        /* check to see if we have a listener registered */
        IEventHandler<IEvent> registeredHandler = eventDispatchers.get(eventType);
        LOG.info("Registering " + eventType + " for " + handler.getClass());
        if (registeredHandler == null) {
            //没注册过处理器则直接put
            eventDispatchers.put(eventType, handler);
        } else if (!(registeredHandler instanceof MultiListenerHandler)) {
            //如果一个事件被2次注册处理器，则生成一个MultiListenerHandler实例.
            MultiListenerHandler multiHandler = new MultiListenerHandler();
            multiHandler.addHandler(registeredHandler);
            multiHandler.addHandler(handler);
            eventDispatchers.put(eventType, multiHandler);
        } else {
            /* already a multilistener, just add to it */
            MultiListenerHandler multiHandler = (MultiListenerHandler) registeredHandler;
            multiHandler.addHandler(handler);
        }
    }

    @Override
    public IEventHandler getEventHandler() {
        if (handlerInstance == null) {
            handlerInstance = new GenericEventHandler();
        }
        return handlerInstance;
    }

    /**
     * 分发器本身的EventHandler是负责往队列里面放事件的处理器，
     * 外部通过调用这个处理的handle方法往分发器里面放事件。
     */
    class GenericEventHandler implements IEventHandler<IEvent> {
        @Override
        public void handle(final IEvent event) {
            if (blockNewEvents) {
                return;
            }
            drained = false;

	    /* all this method does is enqueue all the events onto the queue */
            int qSize = eventQueue.size();
            if (qSize != 0 && qSize % 1000 == 0) {
                LOG.info("Size of event-queue is " + qSize);
            }
            int remCapacity = eventQueue.remainingCapacity();
            if (remCapacity < 1000) {
                LOG.warn("Very low remaining capacity in the event-queue: "
                        + remCapacity);
            }
            try {
                eventQueue.put(event);
            } catch (InterruptedException e) {
                if (!stopped) {
                    LOG.warn("AsyncDispatcher thread interrupted", e);
                }
                throw new APakingRuntimeException(e);
            }
        }
    }

}
