package com.ai.apaking.indexer.bucket;

import com.ai.apaking.common.commonClass.Bucket;
import com.ai.apaking.common.commonClass.BucketState;
import com.ai.apaking.indexer.Cons;
import com.ai.apaking.indexer.Context;
import com.ai.apaking.indexer.index.ThreadPoolIndexWriter;
import com.ai.apaking.indexer.util.ConfigReader;
import com.ai.apaking.indexer.util.DirectoryUtil;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * 类型说明: 桶管理.
 *
 * @author guh
 *         create time 2016/3/22 10:54
 */
public class BucketManager {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(BucketManager.class);

    //桶大小,单位MB
    private long bucketSizeMB;

    //向Naming汇报,超时时间
    private long timeOutSecs;

    private String indexHome;

    private final Map<String, BucketData> bucketDataMap = new HashMap<>();

    //单例,
    private static BucketManager instance = new BucketManager();

    private ThreadPoolIndexWriter indexWriter;

    private String hotBucketPath;

    //private constructor
    private BucketManager() {
        init();
    }

    /**
     * Get a Instance of BucketManager.
     *
     * @return A Instance of BucketManager
     */
    public static BucketManager getInstance() {
        return instance;
    }

    /**
     * Initial Method.
     */
    public void init() {
        // 初始化参数,加载本地桶信息等
        indexHome = ConfigReader.getKeyValue(Cons.INDEX_HOME_PROP_NAME);

        LOGGER.info("INDEX_HOME:" + indexHome);

        hotBucketPath = indexHome + File.separator + Cons.HOT_BUCKET_NAME;

        bucketSizeMB = Long.parseLong(ConfigReader.getKeyValue(Cons.BUCKET_SIZE_PROP_NAME));
        LOGGER.info("BUCKET_SIZE:" + bucketSizeMB);

        timeOutSecs = Long.parseLong(ConfigReader.getKeyValue(Cons.TIME_OUT_PROP_NAME));
        LOGGER.info("TIME_OUT:" + timeOutSecs);

        loadBucketData();

        if (null == bucketDataMap.get(Cons.HOT_BUCKET_NAME)) {
            bucketDataMap.put(Cons.HOT_BUCKET_NAME, BucketDataFactory.getHotBucketData());
        }
    }


    /**
     * 加载索引桶信息
     */
    private void loadBucketData() {
        File dir = new File(indexHome);
        if (dir.exists() && dir.isDirectory()) {
            String[] bucketIds = dir.list();
            for (String bucketId : bucketIds) {
                Bucket bucket =new Bucket();
                BucketData bucketData = new BucketData(bucket);
                bucket.setBucketId(bucketId);
                bucket.setIndexerId(Context.getContext().getIndexerId());
                bucket.setPath(indexHome+File.separator+bucketId);
                if(!Cons.HOT_BUCKET_NAME.equals(bucketId)&&!Cons.TEMP_BUCKET_NAME.equals(bucketId)){
                    String [] strArray =bucketId.split(Cons.BUCKET_NAME_SEPARATOR);
                    bucket.setFinishTime(Long.parseLong(strArray[2]));
                    bucket.setStartTime(Long.parseLong(strArray[3]));
                    bucket.setState(BucketState.WARM);
                }else{
                    bucket.setState(BucketState.HOT);
                    //TODO 需加载BucketManifest
                    bucketData.setManifest(new BucketManifest());
                }

                bucketDataMap.put(bucketId,bucketData);
            }
        }
    }

    /**
     * @return indexWriter 实例
     */
    public ThreadPoolIndexWriter getIndexWriter() {
        if (isHotBucketFull()) {
            //桶满
            try {
                //先关闭桶,释放资源
                closeWriter();
            } catch (IOException ioe) {
                LOGGER.warn("热桶转温桶时,关闭 IndexWriter 出现IOException", ioe);
                return null;
            }
            //桶满转零时区
            if (dealBucketFull()) {
                //TODO 如果热转零时成功，启动任务调度 零时转温桶

                return createIndexWriter();
            } else {
                return null;
            }
        } else {
            //桶没满
            //如果IndexWriter为null，则新建
            if (indexWriter == null) {
                indexWriter = createIndexWriter();
            }
            return indexWriter;
        }
    }

    /**
     * 每次判断桶满不满的时候顺带更新一下桶的大小.
     *
     * @return boolean 热桶是否满
     */
    private boolean isHotBucketFull() {
        File file = new File(hotBucketPath);

        long bucketsSize = DirectoryUtil.getDirSize(file);

        bucketDataMap.get(Cons.HOT_BUCKET_NAME).getManifest().setBucketSize(bucketsSize);

        //转成MB
        return bucketsSize / 1024 / 1024 >= bucketSizeMB;
    }


    /**
     * 关闭热桶的indexWriter和相关资源.
     *
     * @throws IOException
     */
    private void closeWriter() throws IOException {

        indexWriter.close();
        indexWriter.getAnalyzer().close();
        indexWriter.getDirectory().close();
    }

    /**
     * 热桶内的索引转零时区.
     */
    private boolean dealBucketFull() {

        //热桶索引先转零时区

        String distPath = ConfigReader.getKeyValue(Cons.INDEX_HOME_PROP_NAME) + File.separator + Cons.TEMP_BUCKET_NAME;


        boolean isSucceed = DirectoryUtil.renameDirectory(hotBucketPath, distPath);

        if (isSucceed) {
            //如果成功，则indexWriter至为null,桶的状态改成warm
            BucketData bucketData = bucketDataMap.get(Cons.HOT_BUCKET_NAME);
            //状态转温，id和path改成零时桶的
            bucketData.getBucket().setState(BucketState.WARM);
            bucketData.getBucket().setBucketId(Cons.TEMP_BUCKET_NAME);
            bucketData.getBucket().setPath(distPath);


            bucketDataMap.put(Cons.TEMP_BUCKET_NAME, bucketData);

            bucketDataMap.put(Cons.HOT_BUCKET_NAME, BucketDataFactory.getHotBucketData());

            //TODO 调度任务处理零时区，并向Naming汇报
            return true;
        } else {
            return false;
        }
    }

    private ThreadPoolIndexWriter createIndexWriter() {

        Analyzer analyzer = new StandardAnalyzer();

        Directory directory;
        try {
            directory = FSDirectory.open(Paths.get(hotBucketPath));

            LogMergePolicy lmp = new LogDocMergePolicy();

            lmp.setMergeFactor(Integer.parseInt(ConfigReader.getKeyValue(Cons.MERGE_FACTOR_PROP_NAME)));


            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            config.setMergePolicy(lmp);

            config.setRAMBufferSizeMB(Integer.parseInt(ConfigReader.getKeyValue(Cons.BUFFER_SIZE_PROP_NAME)));

            return new ThreadPoolIndexWriter(directory, config,
                    Integer.parseInt(ConfigReader.getKeyValue(Cons.THREAD_NUM_PROP_NAME)), Integer.MAX_VALUE);

        } catch (IOException ioe) {
            LOGGER.warn("创建IndexWriter实例时出现IOException!");
            return null;
        }
    }

    /**
     * 生成桶的名字
     *
     * @return bucket name
     */
    private String genBucketName() {
        StringBuilder name = new StringBuilder();
        try (
                Directory directory = FSDirectory.open(Paths.get(hotBucketPath));
                IndexReader reader = DirectoryReader.open(directory)
        ) {

            IndexSearcher searcher = new IndexSearcher(reader);

            Analyzer analyzer = new StandardAnalyzer();

            QueryParser parser = new QueryParser(Cons.FIELD_TIME, analyzer);

            Query query = parser.parse("+*:*");

            Sort sort = new Sort(new SortField(Cons.FIELD_TIME, SortField.Type.LONG,
                    true));

            Sort sort1 = new Sort(new SortField(Cons.FIELD_TIME, SortField.Type.LONG,
                    false));

            TopFieldDocs docs = searcher.search(query, 1, sort);

            TopFieldDocs docs1 = searcher.search(query, 1, sort1);

            Document doc = searcher.doc(docs.scoreDocs[0].doc);

            Document doc1 = searcher.doc(docs1.scoreDocs[0].doc);

            String time = doc.get(Cons.FIELD_TIME);

            String time1 = doc1.get(Cons.FIELD_TIME);

            //bucket name=indexid_starttime_finishtime_seq
            name.append(Context.getContext().getIndexerId()).append(Cons.BUCKET_NAME_SEPARATOR)
                    .append(time).append(Cons.BUCKET_NAME_SEPARATOR)
                    .append(time1).append(Cons.BUCKET_NAME_SEPARATOR)
                    .append(bucketDataMap.keySet().size());


        } catch (IOException ioe) {
            LOGGER.warn("搜索热桶获得时间范围时发生IOException!", ioe);
        } catch (ParseException pe) {
            LOGGER.warn("搜索热桶获得时间范围时发生ParseException!", pe);
        }

        return name.toString();
    }

    /**
     * 根据bucket id 获取创建IndexReader,如果bucket id不存在则要向Audit Service汇报.
     *
     * @param bucketId bucket id
     * @return org.apache.lucene.index.IndexReader
     */
    public IndexReader getIndexReader(String bucketId) {
        if (bucketDataMap.containsKey(bucketId)) {
            //桶存在
            Directory directory = null;
            try {
                directory = FSDirectory.open(Paths.get(indexHome + File.separator + bucketId));
                return DirectoryReader.open(directory);
            } catch (IOException ioe) {
                LOGGER.warn("创建IndexReader 出现IOException", ioe);
                if (directory != null) {
                    try {
                        directory.close();
                    } catch (IOException e) {
                        LOGGER.warn("关闭FSDirectory 出现IOException", e);
                    }
                }
            }
        } else {
            //桶不存在
            dealBucketNotExist();
        }
        return null;
    }


    /**
     * 处理桶不存在的情况.
     */
    private void dealBucketNotExist() {
        //TODO 汇报Audit Service
        throw new UnsupportedOperationException("暂不支持");
    }


    /**
     * 根据id获得桶信息.
     *
     * @param id bucket id
     * @return BucketData Instance
     */
    public BucketData getBucketData(String id) {
        return bucketDataMap.get(id);
    }


    /**
     * test
     *
     * @param args args
     */
    public static void main(String[] args) {

        //BucketManager bm = BucketManager.getInstance();

        LOGGER.info(Long.valueOf(ConfigReader.getKeyValue(Cons.BUCKET_SIZE_PROP_NAME)) + " MB");

        //LOGGER.info(bm.isHotBucketFull());

    }

}
