package com.xianyu.xianyucommon.service;

public interface InnerRedisService {

    /**
     * 写redis缓存
     * @param key
     */
    boolean writeCache(String key);
}
