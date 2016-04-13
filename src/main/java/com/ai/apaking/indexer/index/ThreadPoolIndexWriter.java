package com.ai.apaking.indexer.index;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 类型说明: 继承lucene的IndexWriter，采用线程池方式实现多线程建索引.
 *
 * @author guh
 *         create time 2016/3/18 10:39
 */
public class ThreadPoolIndexWriter extends IndexWriter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadPoolIndexWriter.class);

    private ExecutorService threadPool;

    /**
     * 内部类,实现了Runnable接口,负责调用Lucene API建索引.
     */
    private class Job implements Runnable {
        Document document;

        Term delTerm;

        public Job(Document doc, Term delTerm) {
            this.delTerm = delTerm;
            this.document = doc;
        }

        @Override
        public void run() {
            try {
                if (delTerm != null) {
                    ThreadPoolIndexWriter.super.updateDocument(delTerm, document);
                } else {
                    ThreadPoolIndexWriter.super.addDocument(document);
                }
            } catch (IOException ioe) {
                LOGGER.warn(ThreadPoolIndexWriter.class + " Job.run() 发生异常!", ioe);
            }
        }

    }

    /**
     * @param dir          Directory
     * @param config       IndexWriterConfig
     * @param numThreads   Thread Number
     * @param maxQueueSize Max QueueSize
     * @throws IOException throws IOException
     */
    public ThreadPoolIndexWriter(Directory dir, IndexWriterConfig config, int numThreads, int maxQueueSize) throws IOException {
        super(dir, config);

        threadPool = new ThreadPoolExecutor(numThreads, numThreads, 0, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(maxQueueSize),
                new ThreadPoolExecutor.AbortPolicy());
    }

    /**
     * Add Document
     *
     * @param doc Document
     */
    public void addDocument(Document doc) {

        threadPool.execute(new Job(doc, null));
    }


    /**
     * Update Document
     *
     * @param term Term
     * @param doc  Document
     */
    public void updateDocument(Term term, Document doc) {

        threadPool.execute(new Job(doc, term));
    }

    /**
     * close 方法,负责关闭线程池和释放lucene资源
     *
     * @throws IOException IOException
     */
    @Override
    public void close() throws IOException {
        finish();
        super.close();
    }

    /**
     * Rollback
     *
     * @throws IOException IOException
     */
    @Override
    public void rollback() throws IOException {
        finish();
        super.rollback();
    }

    private void finish() {
        //关闭线程池
        threadPool.shutdown();
        try {
            threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
        } catch (InterruptedException ie) {
            LOGGER.warn(ThreadPoolIndexWriter.class + " finish() 发生异常!", ie);
            Thread.currentThread().interrupt();
        }
    }

}
