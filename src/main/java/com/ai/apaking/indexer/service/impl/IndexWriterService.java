package com.ai.apaking.indexer.service.impl;


import com.ai.apaking.common.commonClass.FFS;
import com.ai.apaking.indexer.Cons;
import com.ai.apaking.indexer.Context;
import com.ai.apaking.indexer.buffer.IBuffer;
import com.ai.apaking.indexer.index.ThreadPoolIndexWriter;
import com.ai.apaking.indexer.service.IWriter;
import org.apache.lucene.document.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 类型说明: Implementation class of interface "IWriter".
 *
 * @author guh
 *         create time 2016/3/17 16:22
 */
public class IndexWriterService implements IWriter {

    private static final Logger LOGGER = LoggerFactory.getLogger(IndexWriterService.class);

    private ThreadPoolIndexWriter indexWriter;

    private boolean running = true;

    private static IndexWriterService instance = new IndexWriterService();


    /**
     * private constructor.
     */
    private IndexWriterService() {
    }


    /**
     * @return instance
     */
    public static IndexWriterService getInstance() {
        return instance;
    }


    /**
     * Write Thread
     */
    private final class WriteThread extends Thread {
        @Override
        public void run() {
            while (running) {
                IBuffer buffer = Context.getContext().getBuffer();

                if (buffer.isEmpty()) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        running = false;
                        //TODO 出现 InterruptedException异常处理,向 Audit Service进行汇报
                        LOGGER.warn("Index Writer Service WriteThread sleep 时发生了 InterruptedException,线程将退出!", e);
                        Thread.currentThread().interrupt();
                    }
                }

                //从BucketManager获得indexWriter实例,此时BucketManager会检查桶是否满，进行相应处理

                indexWriter = Context.getContext().getBucketManager().getIndexWriter();

                if(indexWriter==null){
                    LOGGER.warn("Index Writer Service 索引服务获取IndexWriter失败!");
                    break;
                }

                List<FFS> ffsList = buffer.get(10000);

                for (Object obj : ffsList) {
                    indexWriter.addDocument(getDoc((FFS) obj));
                }

                Context.getContext().getPreStat().doStat(ffsList);

            }
            LOGGER.info("Index Writer Service 索引服务退出!");
        }

        private Document getDoc(FFS ffs) {
            Document doc = new Document();

            doc.add(new LongField(Cons.FIELD_TIME, ffs.getTimestamp(),
                    Cons.LONG_FIELD_TYPE_STORED_SORTED));

            doc.add(new StringField(Cons.FIELD_HOST, ffs.getHost(), Field.Store.YES));

            doc.add(new StringField(Cons.FIELD_SOURCE, ffs.getSource(),
                    Field.Store.YES));

            doc.add(new StringField(Cons.FIELD_SOURCE_TYPE, ffs.getSourceType(),
                    Field.Store.YES));

            doc.add(new TextField(Cons.FIELD_TEXT, ffs.getText(), Field.Store.YES));

            return doc;
        }
    }


    @Override
    public void init() {
        //TODO 做一些参数的初始化,暂未开发
        LOGGER.info("Index Writer Service 初始化成功!");
    }


    @Override
    public void start() {
        running = true;
        new WriteThread().start();

        LOGGER.info("Index Writer Service 启动索引服务成功!");
    }

    @Override
    public void stop() {
        running = false;
    }

}
