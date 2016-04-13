package com.ai.apaking.indexer;

import akka.actor.ActorSystem;
import akka.actor.Props;
import com.ai.apaking.indexer.actor.ClusterListener;
import com.ai.apaking.indexer.actor.IndexReceiver;
import com.ai.apaking.indexer.bucket.BucketManager;
import com.ai.apaking.indexer.buffer.impl.QueueBuffer;
import com.ai.apaking.indexer.service.IPreStat;
import com.ai.apaking.indexer.service.IWriter;
import com.ai.apaking.indexer.service.impl.IndexWriterService;
import com.ai.apaking.indexer.service.impl.SimplePreStatService;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

/**
 * 类型说明: 入口类
 *
 * @author guh
 *         create time 2016/3/15 17:27
 */
public class Main {

    private Main() {
    }

    /**
     * main 方法.
     *
     * @param args args
     */
    public static void main(String[] args) {

        final String port = args.length > 0 ? args[0] : "2551";

        //启动Bucket
        Context.getContext().setBucketManager(BucketManager.getInstance());

        //FFS缓存
        Context.getContext().setBuffer(QueueBuffer.getInstance());


        //启动索引服务
        IWriter writer = IndexWriterService.getInstance();
        writer.start();

        //启动搜索服务

        //实例化预统计服务
        IPreStat preStat = SimplePreStatService.getInstance();
        Context.getContext().setPreStat(preStat);

        //启动akka actors
        final Config config = ConfigFactory.parseString("akka.remote.netty.tcp.port=" + port)
                .withFallback(ConfigFactory.parseString("akka.cluster.roles = [indexer]"))
                .withFallback(ConfigFactory.load());

        ActorSystem system = ActorSystem.create("IndexCluster", config);

        system.actorOf(Props.create(IndexReceiver.class), "indexReceiver");

        system.actorOf(Props.create(ClusterListener.class));

    }
}
