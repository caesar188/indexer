package com.ai.apaking.indexer.event;

/**
 * @param <TYPE>
 * @author guhao
 * @ClassName: IEvent
 * create time 2016/4/11 11:18
 */
public interface IEvent<TYPE extends Enum> {

    /**
     * Get Event Type
     *
     * @return Event Type
     */
    TYPE getType();

    /**
     * Get Event Type
     *
     * @param type Event Type
     */
    void setType(TYPE type);

    /**
     * @return timeStamp
     */
    long getTimestamp();

    @Override
    String toString();
}

