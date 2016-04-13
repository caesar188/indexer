package com.ai.apaking.indexer.event;

/**
 * @param <TYPE>
 * @author guhao
 * @ClassName: AbstractEvent
 * @Description: (EVENT 抽象类)
 */
public abstract class AbstractEvent<TYPE extends Enum<TYPE>> implements IEvent<TYPE> {

    private TYPE type;

    /**
     * 设置 Event Type.
     *
     * @param type Event Type
     */
    public void setType(TYPE type) {
        this.type = type;
    }

    private final long timestamp;

    /**
     * <p>
     * Title:
     * </p>
     * <p>
     * Description: 如果不在意timestamp 用这个构造函.
     *
     * @param type 类型
     * @author guhao
     */
    public AbstractEvent(final TYPE type) {
        this.type = type;
        // We're not generating a real timestamp here. It's too expensive.
        timestamp = -1L;
    }

    /**
     * <p>
     * Title:
     * </p>
     * <p>
     * Description: 如果在意timestamp 用这个构造函数.
     *
     * @param type      类型
     * @param timestamp timestamp
     * @author author
     */
    public AbstractEvent(final TYPE type, final long timestamp) {
        this.type = type;
        this.timestamp = timestamp;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public TYPE getType() {
        return type;
    }

    @Override
    public String toString() {
        return "EventType: " + getType();
    }
}
