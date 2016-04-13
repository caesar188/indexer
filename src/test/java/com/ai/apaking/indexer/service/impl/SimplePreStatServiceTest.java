package com.ai.apaking.indexer.service.impl;

import com.ai.apaking.common.commonClass.FFS;
import com.ai.apaking.indexer.Cons;
import com.ai.apaking.indexer.Context;
import com.ai.apaking.indexer.bucket.BucketData;
import com.ai.apaking.indexer.bucket.BucketManager;
import com.ai.apaking.indexer.service.IPreStat;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by guh on 2016/4/6.
 */
public class SimplePreStatServiceTest {

    @BeforeClass
    public static void setUp() throws Exception {
        Context.getContext().setBucketManager(BucketManager.getInstance());

    }

    @Test
    public void testDoStat() throws Exception {
        IPreStat preStat = SimplePreStatService.getInstance();
        Context.getContext().setPreStat(preStat);

        List<FFS> list=new ArrayList();

        FFS ffs1 = new FFS();
        ffs1.setHost("host_name");
        ffs1.setSource("e:\\syslog.log");
        ffs1.setText("texts");
        ffs1.setSourceType("source_type");

        FFS ffs2 = new FFS();
        ffs2.setHost("host_name2");
        ffs2.setSource("e:\\syslog.log");
        ffs2.setText("texts");
        ffs2.setSourceType("source_type2");

        list.add(ffs1);
        list.add(ffs2);

        preStat.doStat(list);

        BucketData bd=Context.getContext().getBucketManager().getBucketData(Cons.HOT_BUCKET_NAME);
        long docCount=bd.getManifest().getDocCount();
        long hostCount=bd.getManifest().getHostCount();

        long sourceCount=bd.getManifest().getSourceCount();

        Assert.assertEquals(2,docCount);

        Assert.assertEquals(2,hostCount);

        Assert.assertEquals(1,sourceCount);

    }
}