package com.ai.apaking.indexer.actor;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.pattern.Patterns;
import akka.util.Timeout;
import com.ai.apaking.common.commonClass.FFS;
import com.ai.apaking.common.commonClass.Result;
import com.ai.apaking.common.commonClass.ResultCode;
import com.ai.apaking.indexer.Context;
import com.ai.apaking.indexer.buffer.impl.QueueBuffer;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.*;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

/**
 * test
 * Created by guh on 2016/3/9.
 */
public class IndexReceiverTest {

    static ActorRef ref;
    static ActorSystem system;

    @BeforeClass
    public static void setUp() throws Exception {
        final String port = "2551";

        //FFS缓存
        Context.getContext().setBuffer(QueueBuffer.getInstance());


        final Config config = ConfigFactory.parseString("akka.remote.netty.tcp.port=" + port)
                .withFallback(ConfigFactory.parseString("akka.cluster.roles = [indexer]")).withFallback(ConfigFactory.load());

        system = ActorSystem.create("IndexCluster", config);


        ref = system.actorOf(Props.create(IndexReceiver.class), "indexReceiver");
    }

    @AfterClass
    public static void tearDown() throws Exception {
        system.shutdown();
    }

    @Test
    public void testOnReceive() throws Exception {
        FFS ffs = new FFS();
        ffs.setHost("guhao");
        ffs.setSource("e:\\syslog.log");
        ffs.setText("qqqqqqqqqqqqqqqqqq");

        Timeout timeout = new Timeout(Duration.create(5, "seconds"));
        Future<Object> future = Patterns.ask(ref, ffs, timeout);

        Result result=(Result) Await.result(future, timeout.duration());


        Assert.assertEquals(ResultCode.EXCEPTION_FFS_FORMAT_ERROR,result.getResultCode());

    }

    @Test
    public void testOnReceive1() throws Exception {
        FFS ffs1 = new FFS();
        ffs1.setHost("host_name");
        ffs1.setSource("e:\\syslog.log");
        ffs1.setText("texts");
        ffs1.setSourceType("source_type");

        Timeout timeout = new Timeout(Duration.create(5, "seconds"));
        Future<Object> future1 = Patterns.ask(ref, ffs1, timeout);
        Result result1=(Result) Await.result(future1, timeout.duration());


        Assert.assertEquals(ResultCode.SUCCESS,result1.getResultCode());
    }

}