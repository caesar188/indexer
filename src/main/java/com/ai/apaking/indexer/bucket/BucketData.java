package com.ai.apaking.indexer.bucket;

import com.ai.apaking.common.commonClass.Bucket;


/**
 * 类型说明: Indexer 内部用于存储桶信息
 *
 * @author guh
 *         create time 2016/3/22 15:20
 */
public class BucketData {

    private Bucket bucket;

    private BucketManifest manifest;


    /**
     * constructor
     *
     * @param bucket Bucket
     */
    public BucketData(Bucket bucket) {
        this.bucket = bucket;
    }

    /**
     * get bucket.
     *
     * @return bucket
     */
    public Bucket getBucket() {
        return this.bucket;
    }

    /**
     * 返回BucketManifest对象.
     * @return manifest
     */
    public BucketManifest getManifest() {
        return manifest;
    }

    /**
     * 设置Manifest.
     * @param manifest manifest对象
     */
    public void setManifest(BucketManifest manifest) {
        this.manifest = manifest;
    }
}
