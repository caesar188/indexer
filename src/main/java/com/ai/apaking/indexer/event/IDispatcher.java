package com.ai.apaking.indexer.event;

/**
 * @author guh
 * @ClassName: Dispatcher
 * @Description: 事件分发器接口
 */
public interface IDispatcher {

    /**
     * @return 事件处理器
     */
    IEventHandler getEventHandler();

    /**
     * 注册事件和处理器
     *
     * @param eventType 事件类型
     * @param handler   处理器
     */
    void register(final Class<? extends Enum> eventType,
                  final IEventHandler handler);
}
