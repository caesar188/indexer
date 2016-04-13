package com.ai.apaking.indexer;

import akka.cluster.Member;
import com.ai.apaking.indexer.bucket.BucketManager;
import com.ai.apaking.indexer.buffer.IBuffer;
import com.ai.apaking.indexer.service.IPreStat;

import java.util.ArrayList;
import java.util.List;

/**
 * 类型说明: Indexer Context,it keeps the instances of service and global variables
 *
 * @author guh
 *         create time 2016/3/15 17:23
 */
public final class Context {

    private static Context instance = new Context();

    private IBuffer buffer;

    private List<Member> namingServiceList = new ArrayList<>();

    private BucketManager bucketManager;

    private String indexerId;

    private IPreStat preStat;

    private Context() {
    }

    /**
     * Get Instance of Context.
     *
     * @return instance Instance
     */
    public static Context getContext() {
        return instance;
    }


    /**
     * Get NamingService List.
     *
     * @return namingServiceList
     */
    public synchronized List<Member> getNamingServiceList() {
        return namingServiceList;
    }

    /**
     * Add a Naming Service.
     *
     * @param namingService Member is naming Service
     */
    public synchronized void addNamingService(final Member namingService) {
        namingServiceList.add(namingService);
    }

    /**
     * Remove a Naming Service.
     *
     * @param namingService Member is naming Service
     */
    public synchronized void removeNamingService(final Member namingService) {
        namingServiceList.remove(namingService);
    }

    /**
     * @return buffer
     */
    public IBuffer getBuffer() {
        return buffer;
    }

    /**
     * @param buffer buffer
     */
    public void setBuffer(IBuffer buffer) {
        this.buffer = buffer;
    }

    /**
     * @return bucketManager
     */
    public BucketManager getBucketManager() {
        return bucketManager;
    }

    /**
     * @param bucketManager bucketManager
     */
    public void setBucketManager(BucketManager bucketManager) {
        this.bucketManager = bucketManager;
    }

    /**
     * @return indexer id
     */
    public String getIndexerId() {
        return indexerId;
    }

    /**
     * @param indexerId indexer id
     */
    public void setIndexerId(String indexerId) {
        this.indexerId = indexerId;
    }

    /**
     * get IPreStat 实例
     *
     * @return IPreStat 实例
     */
    public IPreStat getPreStat() {
        return preStat;
    }

    /**
     * set IPreStat 实例
     *
     * @param preStat IPreStat 实例
     */
    public void setPreStat(IPreStat preStat) {
        this.preStat = preStat;
    }
}
