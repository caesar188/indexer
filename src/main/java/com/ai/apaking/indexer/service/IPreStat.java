package com.ai.apaking.indexer.service;

import com.ai.apaking.common.commonClass.FFS;

import java.util.List;

/**
 * Created by guh on 2016/4/6.
 */
public interface IPreStat {

    /**
     * 处理预统计.
     *
     * @param list ffs list
     */
    void doStat(List<FFS> list);
}
