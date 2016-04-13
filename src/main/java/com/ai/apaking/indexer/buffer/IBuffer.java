package com.ai.apaking.indexer.buffer;

import com.ai.apaking.common.commonClass.FFS;

import java.util.List;

/**
 * 类型说明: interface IBuffer
 *
 * @author guh
 *         create time 2016/3/16 9:59
 */
public interface IBuffer {

    /**
     * Add a FFS Object.
     *
     * @param ffs FFS Object
     */
    void add(FFS ffs);

    /**
     * Add a FFS Object.
     *
     * @param num num
     * @return FFS List
     */
    List<FFS> get(int num);

    /**
     * return the size of buffer.
     *
     * @return size
     */
    long size();

    /**
     * Is Buffer Empty.
     *
     * @return boolean value is empty
     */
    boolean isEmpty();
}
