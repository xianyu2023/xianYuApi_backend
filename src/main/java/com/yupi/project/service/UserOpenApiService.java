package com.yupi.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xianyu.xianyucommon.model.entity.UserOpenApi;

/**
* @author happyxianfish
* @description 针对表【user_open_api(用户调用接口关系表)】的数据库操作Service
* @createDate 2023-07-26 09:27:07
*/
public interface UserOpenApiService extends IService<UserOpenApi> {
    /**
     * 校验
     *
     * @param userOpenApi
     * @param add 是否为创建校验
     */
    void validUserOpenApi(UserOpenApi userOpenApi, boolean add);

    /**
     * 用户调用接口次数+1（修改表）
     * @param userId
     * @param openApiId
     */
    boolean invokeCount(Long userId,Long openApiId);

}
