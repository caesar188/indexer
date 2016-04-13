package com.ai.apaking.indexer.bucket;

import com.ai.apaking.common.commonClass.BucketState;
import com.ai.apaking.indexer.Cons;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by guh on 2016/4/5.
 */
public class BucketDataFactoryTest {

    @Test
    public void testGetHotBucketData() throws Exception {
        BucketData bd = BucketDataFactory.getHotBucketData();

        Assert.assertEquals(BucketState.HOT,bd.getBucket().getState());

        Assert.assertEquals(Cons.HOT_BUCKET_NAME,bd.getBucket().getBucketId());
    }
}