package com.ai.apaking.indexer.util;

import com.ai.apaking.indexer.Cons;
import org.junit.Assert;
import org.junit.Test;


/**
 * Created by guh on 2016/4/6.
 */
public class ConfigReaderTest {

    @Test
    public void testGetKeyValue() throws Exception {
        Assert.assertEquals("50",ConfigReader.getKeyValue(Cons.MERGE_FACTOR_PROP_NAME));
    }
}