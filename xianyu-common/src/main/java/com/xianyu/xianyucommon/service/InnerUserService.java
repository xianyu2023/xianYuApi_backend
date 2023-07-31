package com.xianyu.xianyucommon.service;

import com.xianyu.xianyucommon.model.entity.User;

/**
 * 用户服务
 *
 * @author yupi
 */
public interface InnerUserService {
    /**
     * 根据用户的ak获取用户
     * @param accessKey
     * @return
     */
    User getInvokeUser(String accessKey);
}
