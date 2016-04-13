package com.ai.apaking.indexer.service.impl;

import com.ai.apaking.common.commonClass.FFS;
import com.ai.apaking.indexer.Cons;
import com.ai.apaking.indexer.Context;
import com.ai.apaking.indexer.bucket.BucketData;
import com.ai.apaking.indexer.bucket.BucketManifest;
import com.ai.apaking.indexer.service.IPreStat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 类型说明: Implementation class of interface "IPreStat".
 * 目前仅对以下信息统计：统计桶内Document总数、host总数、sources总数、source type总数.
 *
 * @author guh
 *         create time 2016/4/6 11:26
 */
public class SimplePreStatService implements IPreStat {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimplePreStatService.class);

    /**
     * private constructor.
     */
    private SimplePreStatService() {
    }

    /**
     * @return instance
     */
    public static SimplePreStatService getInstance() {
        return instance;
    }

    private static SimplePreStatService instance = new SimplePreStatService();

    @Override
    public void doStat(List<FFS> list) {

        BucketManifest manifest;
        BucketData bucketData = Context.getContext().getBucketManager().getBucketData(Cons.HOT_BUCKET_NAME);


        if (null == bucketData || null == bucketData.getManifest()) {
            //异常情况 汇报
            LOGGER.warn("预统计异常,bucketData或者manifest为空");
        } else {
            manifest = bucketData.getManifest();
            long docCount = manifest.getDocCount() + list.size();
            manifest.setDocCount(docCount);

            //统计
            Set<String> hosts = manifest.getHosts();
            Set<String> sources = manifest.getSources();
            Set<String> sourceTypes = manifest.getSourceTypes();

            //新增的host、source、sourcetype 放在下面的set里
            Set<String> hostsAdd = new HashSet<>();
            Set<String> sourcesAdd = new HashSet<>();
            Set<String> sourceTypesAdd = new HashSet<>();

            //找新
            for (FFS ffs : list) {
                if (!hosts.contains(ffs.getHost())) {
                    hostsAdd.add(ffs.getHost());
                }
                if (!sources.contains(ffs.getSource())) {
                    sourcesAdd.add(ffs.getSource());
                }
                if (!sourceTypes.contains(ffs.getSourceType())) {
                    sourceTypesAdd.add(ffs.getSourceType());
                }
            }

            String bucketPath = bucketData.getBucket().getPath();

            persistAdd(bucketPath + File.separator + Cons.FIELD_HOST + Cons.DATA_FILE_POSTFIX, hosts, hostsAdd);
            persistAdd(bucketPath + File.separator + Cons.FIELD_SOURCE + Cons.DATA_FILE_POSTFIX, sources, sourcesAdd);
            persistAdd(bucketPath + File.separator + Cons.FIELD_SOURCE_TYPE + Cons.DATA_FILE_POSTFIX, sourceTypes, sourceTypesAdd);


            manifest.setHostCount(hosts.size());
            manifest.setSourceCount(sources.size());
            manifest.setSourceTypeCount(sourceTypes.size());
        }
    }

    /**
     * 新增数据持久化.
     *
     * @param path    index path
     * @param dataAll 现有
     * @param dataAdd 写入的数据
     */
    protected void persistAdd(String path, Set<String> dataAll, Set<String> dataAdd) {
        if (dataAdd.size() <= 0) {
            return;
        }

        try (FileWriter writer = new FileWriter(path, true)) {
            // 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
            for (String data : dataAdd) {
                writer.write(data + "\n");
            }

            writer.flush();
            LOGGER.debug("Write data into " + path + ":" + dataAdd);
        } catch (IOException e) {

            LOGGER.warn("数据写入文档" + path + "时出现IOException", e);
        }
        dataAll.addAll(dataAdd);
    }
}
