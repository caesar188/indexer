package com.ai.apaking.indexer.event;

/**
 * 类型说明: 事件处理接口
 *
 * @param <T>
 * @author guh
 *         create time 2016/4/11 11:18
 */
public interface IEventHandler<T extends IEvent> {

    /**
     * 处理事件的方法.
     *
     * @param event T 事件类型
     */
    void handle(T event);
}

