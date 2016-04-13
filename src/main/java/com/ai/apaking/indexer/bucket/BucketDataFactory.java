package com.ai.apaking.indexer.bucket;

import com.ai.apaking.common.commonClass.Bucket;
import com.ai.apaking.common.commonClass.BucketState;
import com.ai.apaking.indexer.Cons;
import com.ai.apaking.indexer.Context;
import com.ai.apaking.indexer.util.ConfigReader;

import java.io.File;

/**
 * 类型说明: 用于生成桶数据.
 * @author  guh
 * create time 2016/3/31 17:29
 */
public final class BucketDataFactory {

    private BucketDataFactory(){}

    /**
     * 生成一个热桶的BucketData实例.
     *
     * @return BucketData Instance
     */
    public static BucketData getHotBucketData() {
        Bucket bucket = new Bucket();
        bucket.setBucketId(Cons.HOT_BUCKET_NAME);
        bucket.setPath(ConfigReader.getKeyValue(Cons.INDEX_HOME_PROP_NAME)+ File.separator+Cons.HOT_BUCKET_NAME);
        bucket.setIndexerId(Context.getContext().getIndexerId());
        bucket.setState(BucketState.HOT);


        BucketData bucketData= new BucketData(bucket);
        bucketData.setManifest(getHotBucketManifest());

        return bucketData;

    }

    /**
     * 生成一个热桶的BucketManifest实例.
     *
     * @return BucketManifest Instance
     */
    private static BucketManifest getHotBucketManifest() {
        BucketManifest manifest = new BucketManifest();
        return manifest;
    }
}
