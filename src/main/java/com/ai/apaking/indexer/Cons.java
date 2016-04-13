package com.ai.apaking.indexer;

import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.DocValuesType;
import org.apache.lucene.index.IndexOptions;

/**
 * 类型说明: 常量
 *
 * @author guh
 *         create time 2016/3/18 14:34
 */
public final class Cons {

    private Cons() {
    }


    public static final String DATA_FILE_POSTFIX = ".data";
    public static final String BUCKET_NAME_SEPARATOR = "_";
    public static final String TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    //----Bucket PreStat Field Name End

    //----Bucket name Start
    public static final String HOT_BUCKET_NAME = "HOT";
    public static final String TEMP_BUCKET_NAME = "TEMP";
    //----Bucket name End

    //----Properties Name Start
    public static final String INDEX_HOME_PROP_NAME = "INDEX_HOME";
    public static final String TIME_OUT_PROP_NAME = "TIME_OUT";
    public static final String MERGE_FACTOR_PROP_NAME = "MERGE_FACTOR";
    public static final String BUFFER_SIZE_PROP_NAME = "BUFFER_SIZE";
    public static final String THREAD_NUM_PROP_NAME = "THREAD_NUM";
    public static final String BUCKET_SIZE_PROP_NAME = "BUCKET_SIZE";
    //----Properties Name End

    //----FFS Start
    public static final String FIELD_TIME = "timestamp";
    public static final String FIELD_SOURCE = "source";
    public static final String FIELD_SOURCE_TYPE = "sourcetype";
    public static final String FIELD_HOST = "host";
    public static final String FIELD_TEXT = "text";
    //----FFS End


    //----sort field Start
    public static final FieldType LONG_FIELD_TYPE_STORED_SORTED = new FieldType();

    static {
        LONG_FIELD_TYPE_STORED_SORTED.setTokenized(true);
        LONG_FIELD_TYPE_STORED_SORTED.setOmitNorms(true);
        LONG_FIELD_TYPE_STORED_SORTED.setIndexOptions(IndexOptions.DOCS);
        LONG_FIELD_TYPE_STORED_SORTED
                .setNumericType(FieldType.NumericType.LONG);
        LONG_FIELD_TYPE_STORED_SORTED.setStored(true);
        LONG_FIELD_TYPE_STORED_SORTED.setDocValuesType(DocValuesType.NUMERIC);
        LONG_FIELD_TYPE_STORED_SORTED.freeze();
    }
    //----sort field End
}
