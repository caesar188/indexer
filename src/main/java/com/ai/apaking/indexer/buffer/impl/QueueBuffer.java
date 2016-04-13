package com.ai.apaking.indexer.buffer.impl;

import com.ai.apaking.indexer.buffer.IBuffer;
import com.ai.apaking.common.commonClass.FFS;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

/**
 * 类型说明: Class QueueBuffer implements the interface IBuffer
 *
 * @author guh
 *         create time 2016/3/16 9:58
 */
public final class QueueBuffer implements IBuffer {

    private static final Logger LOGGER = Logger.getLogger(QueueBuffer.class);

    private QueueBuffer() {
    }

    // Singleton
    private static QueueBuffer instance = new QueueBuffer();

    /**
     * Get Queue Buffer Instance.
     *
     * @return QueueBuffer
     */
    public static QueueBuffer getInstance() {
        return instance;
    }

    /**
     * @param ffs FFS
     */
    @Override
    public void add(FFS ffs){
        try {
            queue.put(ffs);
        } catch (InterruptedException e) {
            String msg="Add "+ffs.toString()+"发生异常!";
            LOGGER.error(msg,e);
            Thread.currentThread().interrupt();
        }
    }

    // 用于缓存FFS
    private BlockingQueue<FFS> queue = new LinkedBlockingQueue<>();

    /**
     * got a batch of FFS.
     *
     * @param num How many FFS you want to got
     * @return a list of FFS
     */
    @Override
    public List<FFS> get(int num) {

        List<FFS> ffsList = new ArrayList<>();
        FFS ffs;

        try {
            for (int i = 0; i < num; i++) {
                ffs = queue.poll(100, TimeUnit.MILLISECONDS);
                if (ffs != null) {
                    ffsList.add(ffs);
                } else {
                    break;
                }
            }
        } catch (InterruptedException e) {
            String msg="Get FFS 发生异常!";
            LOGGER.error(msg,e);
            Thread.currentThread().interrupt();
        }

        return ffsList;

    }


    @Override
    public long size() {
        return queue.size();
    }


    @Override
    public boolean isEmpty(){
        return queue.isEmpty();
    }

}
