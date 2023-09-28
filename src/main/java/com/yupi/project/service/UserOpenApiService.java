package com.yupi.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xianyu.xianyucommon.model.entity.UserOpenApi;
import com.xianyu.xianyucommon.model.vo.UserOpenApiVO;

import java.util.List;

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
     * @param add         是否为创建校验
     * @return
     */
    boolean validUserOpenApi(UserOpenApi userOpenApi, boolean add);

    /**
     * 用户调用接口次数+1（修改表）
     * @param userId
     * @param openApiId
     */
    boolean invokeCount(Long userId,Long openApiId);

    /**
     * 用户接口关系存在true，不存在返回false
     * @param userId
     * @param openApiId
     * @return
     */
    boolean getUserOpenApiRelation(Long userId,Long openApiId);

    /**
     * 通过用户id获取该用户开通的所有接口信息（调用剩余次数、接口名称）
     * @param userId
     * @return
     */
    List<UserOpenApi> getUserOpenApiByUserId(Long userId);


    /**
     * 脱敏
     * @param userOpenApiList
     * @return
     */
    List<UserOpenApiVO> getUserOpenApiVO(List<UserOpenApi> userOpenApiList);
}
