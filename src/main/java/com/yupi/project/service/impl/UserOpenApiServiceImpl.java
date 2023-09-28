package com.yupi.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xianyu.xianyucommon.model.entity.OpenApi;
import com.xianyu.xianyucommon.model.entity.UserOpenApi;
import com.xianyu.xianyucommon.model.vo.UserOpenApiVO;
import com.yupi.project.common.ErrorCode;
import com.yupi.project.exception.BusinessException;
import com.yupi.project.mapper.UserOpenApiMapper;

import com.yupi.project.service.OpenApiService;
import com.yupi.project.service.UserOpenApiService;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;


import javax.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author happyxianfish
 * @description 针对表【user_open_api(用户调用接口关系表)】的数据库操作Service实现
 * @createDate 2023-07-26 09:27:07
 */
@Service
public class UserOpenApiServiceImpl extends ServiceImpl<UserOpenApiMapper, UserOpenApi>
        implements UserOpenApiService {


    @Resource
    private OpenApiService openApiService;

    @Override
    public boolean validUserOpenApi(UserOpenApi userOpenApi, boolean add) {
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
            //业务是创建用户接口关系时。必须是首次开通，修改调用次数另写方法
            if (getUserOpenApiRelation(userId, openApiId)) {
                //接口关系已存在，不是首次开通，返回false
                return false;
            }
            return true;
        }
        //业务是更新用户接口关系时。接口关系必须已存在
        //接口关系已存在，可以修改关系
        return getUserOpenApiRelation(userId, openApiId);
    }

    /**
     * 用户接口关系存在true，不存在返回false
     *
     * @param userId
     * @param openApiId
     * @return
     */
    @Override
    public boolean getUserOpenApiRelation(Long userId, Long openApiId) {
        if (ObjectUtils.anyNull(userId, openApiId)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "调用者id不符合要求");
        }
        if (openApiId == null || openApiId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "调用的openApiId不符合要求");
        }
        QueryWrapper<UserOpenApi> userOpenApiQueryWrapper = new QueryWrapper<>();
        userOpenApiQueryWrapper.eq("userId", userId);
        userOpenApiQueryWrapper.eq("openApiId", openApiId);
        UserOpenApi theUserOpenApi = this.getOne(userOpenApiQueryWrapper);
        //todo 有些问题：如果是网络问题导致的接口关系不存在，实则接口关系存在。在新建用户接口关系时会创建重复的userId-openApiId
        return theUserOpenApi != null;
    }

    @Override
    public List<UserOpenApi> getUserOpenApiByUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<UserOpenApi> userOpenApiQueryWrapper = new QueryWrapper<>();
        userOpenApiQueryWrapper.eq("userId",userId);
        List<UserOpenApi> list = this.list(userOpenApiQueryWrapper);
        if (CollectionUtils.isEmpty(list)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return list;
    }

    @Override
    public List<UserOpenApiVO> getUserOpenApiVO(List<UserOpenApi> userOpenApiList) {
       if (CollectionUtils.isEmpty(userOpenApiList)) {
           throw new BusinessException(ErrorCode.PARAMS_ERROR);
       }
        return userOpenApiList.stream().filter(userOpenApi -> {
            //filter过滤：返回true保留，返回false过滤掉。
            return userOpenApi != null && userOpenApi.getOpenApiId() != null && userOpenApi.getOpenApiId() > 0;
        }).map(userOpenApi -> {
            UserOpenApiVO userOpenApiVO = new UserOpenApiVO();
            BeanUtils.copyProperties(userOpenApi, userOpenApiVO);
            OpenApi api = openApiService.getById(userOpenApi.getOpenApiId());
            if (api == null || api.getName() == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
            }
            userOpenApiVO.setName(api.getName());
            return userOpenApiVO;
        }).collect(Collectors.toList());
    }

    @Override
    public boolean invokeCount(Long userId, Long openApiId) {
        //校验
        if (userId <= 0 || openApiId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //update user_open_api set totalNum = totalNum +1,leftNum = leftNum -1 where userId = 1 and  openApiId = 1 and leftNum > 0;
        UpdateWrapper<UserOpenApi> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("userId", userId);
        updateWrapper.eq("openApiId", openApiId);
        //leftNum需要>0;
        //todo 并发（锁、分布式锁、事务）.用户只剩5次调用时，同时发出1000个调用接口请求，-999
        updateWrapper.gt("leftNum", 0);
        updateWrapper.setSql("totalNum=totalNum+1,leftNum=leftNum-1");
        return this.update(updateWrapper);
    }
}




