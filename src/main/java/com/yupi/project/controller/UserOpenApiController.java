package com.yupi.project.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;


import com.xianyu.xianyucommon.model.entity.User;
import com.xianyu.xianyucommon.model.entity.UserOpenApi;
import com.xianyu.xianyucommon.model.vo.UserOpenApiVO;
import com.yupi.project.annotation.AuthCheck;
import com.yupi.project.common.*;
import com.yupi.project.constant.CommonConstant;
import com.yupi.project.constant.UserConstant;
import com.yupi.project.exception.BusinessException;

import com.yupi.project.model.dto.userOpenApi.UserOpenApiAddRequest;
import com.yupi.project.model.dto.userOpenApi.UserOpenApiQueryRequest;
import com.yupi.project.model.dto.userOpenApi.UserOpenApiUpdateRequest;

import com.yupi.project.service.UserOpenApiService;
import com.yupi.project.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;

/**
 * 用户调用接口
 *
 * @author yupi
 */
@RestController
@RequestMapping("/userOpenApi")
@Slf4j
public class UserOpenApiController {

    @Resource
    private UserOpenApiService userOpenApiService;

    @Resource
    private UserService userService;


    // region 增删改查

    /**
     * 创建
     *  开通调用接口权限（需要管理员权限并审核。暂时不用管理员权限）
     * @param userOpenApiAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
//    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUserOpenApi(@RequestBody UserOpenApiAddRequest userOpenApiAddRequest, HttpServletRequest request) {
        if (userOpenApiAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserOpenApi userOpenApi = new UserOpenApi();
        BeanUtils.copyProperties(userOpenApiAddRequest, userOpenApi);
        // 校验
        if (!userOpenApiService.validUserOpenApi(userOpenApi, true)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        userOpenApi.setUserId(loginUser.getId());
        boolean result = userOpenApiService.save(userOpenApi);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        long newUserOpenApiId = userOpenApi.getId();
        return ResultUtils.success(newUserOpenApiId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUserOpenApi(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        UserOpenApi oldUserOpenApi = userOpenApiService.getById(id);
        if (oldUserOpenApi == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 仅本人或管理员可删除
        if (!oldUserOpenApi.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = userOpenApiService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新
     *
     * @param userOpenApiUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUserOpenApi(@RequestBody UserOpenApiUpdateRequest userOpenApiUpdateRequest,
                                            HttpServletRequest request) {
        if (userOpenApiUpdateRequest == null || userOpenApiUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserOpenApi userOpenApi = new UserOpenApi();
        BeanUtils.copyProperties(userOpenApiUpdateRequest, userOpenApi);
        // 参数校验
        if (!userOpenApiService.validUserOpenApi(userOpenApi, false)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = userOpenApiUpdateRequest.getId();
        // 判断是否存在
        UserOpenApi oldUserOpenApi = userOpenApiService.getById(id);
        if (oldUserOpenApi == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 仅本人或管理员可修改
        if (!oldUserOpenApi.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = userOpenApiService.updateById(userOpenApi);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<UserOpenApi> getUserOpenApiById(long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserOpenApi userOpenApi = userOpenApiService.getById(id);
        return ResultUtils.success(userOpenApi);
    }

    /**
     * 获取列表（仅管理员可使用）
     *
     * @param userOpenApiQueryRequest
     * @return
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @GetMapping("/list")
    public BaseResponse<List<UserOpenApi>> listUserOpenApi(UserOpenApiQueryRequest userOpenApiQueryRequest) {
        UserOpenApi userOpenApiQuery = new UserOpenApi();
        if (userOpenApiQueryRequest != null) {
            BeanUtils.copyProperties(userOpenApiQueryRequest, userOpenApiQuery);
        }
        QueryWrapper<UserOpenApi> queryWrapper = new QueryWrapper<>(userOpenApiQuery);
        List<UserOpenApi> userOpenApiList = userOpenApiService.list(queryWrapper);
        return ResultUtils.success(userOpenApiList);
    }

    /**
     * 分页获取列表
     *
     * @param userOpenApiQueryRequest
     * @param request
     * @return
     */
    @GetMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<UserOpenApi>> listUserOpenApiByPage(UserOpenApiQueryRequest userOpenApiQueryRequest, HttpServletRequest request) {
        if (userOpenApiQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserOpenApi userOpenApiQuery = new UserOpenApi();
        BeanUtils.copyProperties(userOpenApiQueryRequest, userOpenApiQuery);
        long current = userOpenApiQueryRequest.getCurrent();
        long size = userOpenApiQueryRequest.getPageSize();
        String sortField = userOpenApiQueryRequest.getSortField();
        String sortOrder = userOpenApiQueryRequest.getSortOrder();
        // 限制爬虫
        if (size > 50) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<UserOpenApi> queryWrapper = new QueryWrapper<>(userOpenApiQuery);
        queryWrapper.orderBy(StringUtils.isNotBlank(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        Page<UserOpenApi> userOpenApiPage = userOpenApiService.page(new Page<>(current, size), queryWrapper);
        return ResultUtils.success(userOpenApiPage);
    }

    // endregion


    //region业务


    /**
     * 用户接口关系存在true，不存在返回false
     * @param userOpenApiQueryRequest
     * @return
     */
    @GetMapping("/get/relation")
    public BaseResponse<Boolean> getUserOpenApiRelation(UserOpenApiQueryRequest userOpenApiQueryRequest) {
        if (userOpenApiQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long userId = userOpenApiQueryRequest.getUserId();
        Long openApiId = userOpenApiQueryRequest.getOpenApiId();
        if (ObjectUtils.anyNull(userId,openApiId)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (userId <= 0 || openApiId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = userOpenApiService.getUserOpenApiRelation(userId, openApiId);
        return ResultUtils.success(result);
    }

    /**
     * 获取登录用户已开通的接口
     * loginUser
     * @return
     */
    @GetMapping("/get/relation/userId")
    public BaseResponse<List<UserOpenApiVO>> getUserOpenApiByUserId(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long userId = loginUser.getId();
        if (ObjectUtils.anyNull(userId) || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        List<UserOpenApi> userOpenApiList = userOpenApiService.getUserOpenApiByUserId(userId);
        return ResultUtils.success(userOpenApiService.getUserOpenApiVO(userOpenApiList));
    }
}
