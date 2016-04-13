package com.ai.apaking.indexer.event;

import java.util.ArrayList;
import java.util.List;

/**
 * @author guhao
 * @ClassName: MultiListenerHandler
 * @Description: Multiplexing an event. Sending it to different handlers that
 * are interested in the event.
 */
public class MultiListenerHandler implements IEventHandler<IEvent> {

    List<IEventHandler<IEvent>> listofHandlers;

    /**
     * 构造函数.
     */
    public MultiListenerHandler() {
        listofHandlers = new ArrayList<>();
    }

    @Override
    public void handle(final IEvent event) {
        for (IEventHandler<IEvent> handler : listofHandlers) {
            handler.handle(event);
        }
    }

    void addHandler(final IEventHandler<IEvent> handler) {
        listofHandlers.add(handler);
    }

}
