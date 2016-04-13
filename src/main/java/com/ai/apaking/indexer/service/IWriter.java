package com.ai.apaking.indexer.service;

import java.io.IOException;

/**
 * 写索引的接口
 * Created by guh on 2016/3/8.
 */
public interface IWriter {


    /**
     * init .
     * @throws IOException IO Exception
     */
    void init() throws IOException;

    /**
     * start to write
     */
    void start();

    /**
     * stop to write
     */
    void stop();


}
