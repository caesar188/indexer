package com.ai.apaking.indexer.exception;

/**
 * @author guhao
 * @ClassName: APakingRuntimeException
 * @Description: (APaking 运行时异常)
 */
public class APakingRuntimeException extends RuntimeException {
    private static final long serialVersionUID = -7153142425412203936L;

    /**
     * 构造函数.
     * @param cause Exception
     */
    public APakingRuntimeException(final Exception cause) {
        super(cause);
    }

    /**
     * 构造函数.
     * @param message 异常信息
     */
    public APakingRuntimeException(final String message) {
        super(message);
    }

    /**
     * 构造函数.
     * @param message 异常信息
     * @param cause Exception
     */
    public APakingRuntimeException(final String message, final Exception cause) {
        super(message, cause);
    }
}
