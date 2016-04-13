package com.ai.apaking.indexer.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 类型说明: 读文件配置
 *
 * @author guh
 *         create time 2016/3/18 14:10
 */
public class ConfigReader {

    private ConfigReader() {
    }

    /**
     * 采用静态方法
     */
    private static final Properties PROPS = loadPropertyFile("/config.properties");

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigReader.class);

    /**
     * Load Property File.
     *
     * @param fullFile full file path
     * @return Properties
     */
    public static Properties loadPropertyFile(String fullFile) {
        if ((null == fullFile) || ("".equals(fullFile))) {
            String msg = "Properties file path can not be null : " + fullFile;
            LOGGER.warn(msg);
            throw new IllegalArgumentException(msg);
        }


        try (
                InputStream inputStream = ConfigReader.class.getResourceAsStream(fullFile)
        ) {
            Properties p = new Properties();
            p.load(inputStream);

            return p;
        } catch (FileNotFoundException e) {
            String msg = "Properties file path can not be null : " + fullFile;
            LOGGER.warn(msg);
            throw new IllegalArgumentException(msg, e);
        } catch (IOException e) {
            String msg = "Properties file can not be loading: " + fullFile;
            throw new IllegalArgumentException(msg, e);
        }
    }

    /**
     * @param key Key of Prop
     * @return value
     */
    public static String getKeyValue(String key) {

        return PROPS.getProperty(key);
    }

}
