package com.yupi.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xianyu.xianyucommon.model.entity.UserOpenApi;
import com.yupi.project.common.ErrorCode;
import com.yupi.project.exception.BusinessException;
import com.yupi.project.mapper.UserOpenApiMapper;

import com.yupi.project.service.UserOpenApiService;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;


import java.util.Date;

/**
 * @author happyxianfish
 * @description 针对表【user_open_api(用户调用接口关系表)】的数据库操作Service实现
 * @createDate 2023-07-26 09:27:07
 */
@Service
public class UserOpenApiServiceImpl extends ServiceImpl<UserOpenApiMapper, UserOpenApi>
        implements UserOpenApiService {

    @Override
    public void validUserOpenApi(UserOpenApi userOpenApi, boolean add) {
        if (userOpenApi == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = userOpenApi.getId();
        Long userId = userOpenApi.getUserId();
        Long openApiId = userOpenApi.getOpenApiId();
        Integer totalNum = userOpenApi.getTotalNum();
        Integer leftNum = userOpenApi.getLeftNum();
        Integer status = userOpenApi.getStatus();
        Date createTime = userOpenApi.getCreateTime();
        Date updateTime = userOpenApi.getUpdateTime();
        Integer isDeleted = userOpenApi.getIsDeleted();
        // 创建时，所有参数必须非空
        if (add) {
            //|| ObjectUtils.anyNull(userId)
            if (ObjectUtils.anyNull(userId, openApiId)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
        }
        //todo 校验不完整
//        if (StringUtils.isNotBlank(name) && name.length() > 20) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR, "接口名称过长");
//        }
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "调用者id不符合要求");
        }
    }

    @Override
    public boolean invokeCount(Long userId, Long openApiId) {
        //校验
        if (userId<=0 || openApiId<=0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //update user_open_api set totalNum = totalNum +1,leftNum = leftNum -1 where userId = 1 and  openApiId = 1 and leftNum > 0;
        UpdateWrapper<UserOpenApi> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("userId", userId);
        updateWrapper.eq("openApiId", openApiId);
        //leftNum需要>0;
        //todo 并发（锁、分布式锁、事务）.用户只剩5次调用时，同时发出1000个调用接口请求，-999
        updateWrapper.gt("leftNum",0);
        updateWrapper.setSql("totalNum=totalNum+1,leftNum=leftNum-1");
        return this.update(updateWrapper);
    }
}




