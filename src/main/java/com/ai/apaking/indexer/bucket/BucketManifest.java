package com.ai.apaking.indexer.bucket;


import java.util.HashSet;
import java.util.Set;

/**
 * 类型说明: 记录桶预统计信息.
 *
 * @author guh
 *         create time 2016/3/22 15:51
 */
public final class BucketManifest {

    private long docCount;
    private long sourceCount;
    private long sourceTypeCount;
    private long hostCount;
    private long bucketSize;

    private Set<String> sources;
    private Set<String> sourceTypes;
    private Set<String> hosts;


    /**
     * 构造函数.
     */
    public BucketManifest() {
        sources = new HashSet<>();
        sourceTypes = new HashSet<>();
        hosts = new HashSet<>();
    }

    /**
     * get docCount.
     *
     * @return docCount
     */
    public long getDocCount() {
        return docCount;
    }

    /**
     * @param docCount Document Count
     */
    public void setDocCount(long docCount) {
        this.docCount = docCount;
    }

    /**
     * get Source Count.
     *
     * @return sourceCount
     */
    public long getSourceCount() {
        return sourceCount;
    }

    /**
     * @param sourceCount Source Count
     */
    public void setSourceCount(long sourceCount) {
        this.sourceCount = sourceCount;
    }

    /**
     * @return sourceTypeCount
     */
    public long getSourceTypeCount() {
        return sourceTypeCount;
    }

    /**
     * @param sourceTypeCount SourceType Count
     */
    public void setSourceTypeCount(long sourceTypeCount) {
        this.sourceTypeCount = sourceTypeCount;
    }

    /**
     * @return Host Count
     */
    public long getHostCount() {
        return hostCount;
    }

    /**
     * @param hostCount Host Count
     */
    public void setHostCount(long hostCount) {
        this.hostCount = hostCount;
    }

    /**
     * @return Bucket Size
     */
    public long getBucketSize() {
        return bucketSize;
    }

    /**
     * @param bucketSize Bucket Size
     */
    public void setBucketSize(long bucketSize) {
        this.bucketSize = bucketSize;
    }


    /**
     * get sources set
     *
     * @return Set of sources
     */
    public Set<String> getSources() {
        return this.sources;
    }

    /**
     * get sourceTypes set
     *
     * @return Set of sourceTypes
     */
    public Set<String> getSourceTypes() {
        return this.sourceTypes;
    }

    /**
     * get hosts set
     *
     * @return Set of hosts
     */
    public Set<String> getHosts() {
        return this.hosts;
    }

}
