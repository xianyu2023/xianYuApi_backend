package com.yupi.project.controller;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.xianyu.xianyucommon.model.entity.OpenApi;
import com.xianyu.xianyucommon.model.entity.User;
import com.xianyu.xianyucommon.model.enums.OpenApiStatusEnum;
import com.xianyu.xianyuopenapiclientsdk.client.XianYuOpenApiClient;
import com.yupi.project.annotation.AuthCheck;
import com.yupi.project.common.*;
import com.yupi.project.constant.CommonConstant;
import com.yupi.project.exception.BusinessException;
import com.yupi.project.model.dto.openApiRequest.OpenApiAddRequest;
import com.yupi.project.model.dto.openApiRequest.OpenApiInvokeRequest;
import com.yupi.project.model.dto.openApiRequest.OpenApiQueryRequest;
import com.yupi.project.model.dto.openApiRequest.OpenApiUpdateRequest;

import com.yupi.project.service.OpenApiService;
import com.yupi.project.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 帖子接口
 *
 * @author yupi
 */
@RestController
@RequestMapping("/openApi")
@Slf4j
public class OpenApiController {

    @Resource
    private OpenApiService openApiService;

    @Resource
    private UserService userService;

    @Resource
    private XianYuOpenApiClient xianYuOpenApiClient;

    // region 增删改查

    /**
     * 创建
     *
     * @param openApiAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addOpenApi(@RequestBody OpenApiAddRequest openApiAddRequest, HttpServletRequest request) {
        if (openApiAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        OpenApi openApi = new OpenApi();
        BeanUtils.copyProperties(openApiAddRequest, openApi);
        // 校验
        openApiService.validOpenApi(openApi, true);
        User loginUser = userService.getLoginUser(request);
        openApi.setUserId(loginUser.getId());
        boolean result = openApiService.save(openApi);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        long newOpenApiId = openApi.getId();
        return ResultUtils.success(newOpenApiId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteOpenApi(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        OpenApi oldOpenApi = openApiService.getById(id);
        if (oldOpenApi == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 仅本人或管理员可删除
        if (!oldOpenApi.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = openApiService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新
     *
     * @param openApiUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateOpenApi(@RequestBody OpenApiUpdateRequest openApiUpdateRequest,
                                            HttpServletRequest request) {
        if (openApiUpdateRequest == null || openApiUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        OpenApi openApi = new OpenApi();
        BeanUtils.copyProperties(openApiUpdateRequest, openApi);
        // 参数校验
        openApiService.validOpenApi(openApi, false);
        User user = userService.getLoginUser(request);
        long id = openApiUpdateRequest.getId();
        // 判断是否存在
        OpenApi oldOpenApi = openApiService.getById(id);
        if (oldOpenApi == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 仅本人或管理员可修改
        if (!oldOpenApi.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = openApiService.updateById(openApi);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<OpenApi> getOpenApiById(long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        OpenApi openApi = openApiService.getById(id);
        return ResultUtils.success(openApi);
    }

    /**
     * 获取列表（仅管理员可使用）
     *
     * @param openApiQueryRequest
     * @return
     */
    @AuthCheck(mustRole = "admin")
    @GetMapping("/list")
    public BaseResponse<List<OpenApi>> listOpenApi(OpenApiQueryRequest openApiQueryRequest) {
        OpenApi openApiQuery = new OpenApi();
        if (openApiQueryRequest != null) {
            BeanUtils.copyProperties(openApiQueryRequest, openApiQuery);
        }
        QueryWrapper<OpenApi> queryWrapper = new QueryWrapper<>(openApiQuery);
        List<OpenApi> openApiList = openApiService.list(queryWrapper);
        return ResultUtils.success(openApiList);
    }

    /**
     * 分页获取列表
     *
     * @param openApiQueryRequest
     * @param request
     * @return
     */
    @GetMapping("/list/page")
    public BaseResponse<Page<OpenApi>> listOpenApiByPage(OpenApiQueryRequest openApiQueryRequest, HttpServletRequest request) {
        if (openApiQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        OpenApi openApiQuery = new OpenApi();
        BeanUtils.copyProperties(openApiQueryRequest, openApiQuery);
        long current = openApiQueryRequest.getCurrent();
        long size = openApiQueryRequest.getPageSize();
        String sortField = openApiQueryRequest.getSortField();
        String sortOrder = openApiQueryRequest.getSortOrder();
        String description = openApiQuery.getDescription();
        // description 需支持模糊搜索
        openApiQuery.setDescription(null);
        // 限制爬虫
        if (size > 50) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<OpenApi> queryWrapper = new QueryWrapper<>(openApiQuery);
        queryWrapper.like(StringUtils.isNotBlank(description), "description", description);
        queryWrapper.orderBy(StringUtils.isNotBlank(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        Page<OpenApi> openApiPage = openApiService.page(new Page<>(current, size), queryWrapper);
        return ResultUtils.success(openApiPage);
    }

    // endregion

    /**
     * 管理员发布接口
     * @param idRequest
     * @param request
     * @return
     */
    @PostMapping("/online")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<Boolean> onlineOpenApi(@RequestBody IdRequest idRequest,
                                               HttpServletRequest request) {
        if (idRequest == null || idRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //1. 校验该接口是否存在
        // 判断是否存在
        Long id = idRequest.getId();
        OpenApi oldOpenApi = openApiService.getById(id);
        if (oldOpenApi == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        //2. 判断该接口是否可以调用
        //实际调用该接口试试（使用SDK快速调用接口）
        //todo 这里先暂时访问模拟接口来代替访问这个查询出的接口（由于我们发HTTP请求的客户端访问的接口地址是固定写死的）
        //判断该接口是否可以调用时有固定方法名改为**根据测试地址来调用**？？？
        com.xianyu.xianyuopenapiclientsdk.model.User xianyuUser = new com.xianyu.xianyuopenapiclientsdk.model.User();
        xianyuUser.setUsername("test");
        String nameByPost = xianYuOpenApiClient.getUserNameByPost(xianyuUser);
        if (StringUtils.isEmpty(nameByPost)) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"无法调用该接口");
        }
        // 仅本人或管理员可修改
        //3. 修改接口数据库中的状态字段为 1
        OpenApi openApi = new OpenApi();
        openApi.setId(id);
        openApi.setStatus(OpenApiStatusEnum.ONLINE.getValue());
        boolean result = openApiService.updateById(openApi);
        return ResultUtils.success(result);
    }

    /**
     * 管理员下线接口
     * @param idRequest
     * @param request
     * @return
     */
    @PostMapping("/offline")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<Boolean> offlineOpenApi(@RequestBody IdRequest idRequest,
                                               HttpServletRequest request) {
        if (idRequest == null || idRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //1. 校验该接口是否存在
        // 判断是否存在
        Long id = idRequest.getId();
        OpenApi oldOpenApi = openApiService.getById(id);
        if (oldOpenApi == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 仅本人或管理员可修改
        //3. 修改接口数据库中的状态字段为 0
        OpenApi openApi = new OpenApi();
        openApi.setId(id);
        openApi.setStatus(OpenApiStatusEnum.OFFLINE.getValue());
        boolean result = openApiService.updateById(openApi);
        return ResultUtils.success(result);
    }

    /**
     * 在线测试接口调用（供用户使用）
     * @param openApiInvokeRequest
     * @param request
     * @return
     */
    @PostMapping("/invoke")
    public BaseResponse<Object> invokeOpenApi(@RequestBody OpenApiInvokeRequest openApiInvokeRequest,
                                                HttpServletRequest request) {
        if (openApiInvokeRequest == null || openApiInvokeRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //todo （二）调用前可以做一些校验
        //校验该接口是否存在
        // 判断是否存在
        Long id = openApiInvokeRequest.getId();
        OpenApi oldOpenApi = openApiService.getById(id);
        if (oldOpenApi == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
       //（三）平台后端去调用模拟接口
        //1.不能使用注解自动注入的那个SD客户端，该客户端的ak、sk是后台管理的，生成的签名不是用户的签名。所以需要自己new个客户端，让客户的签名用于调用模拟接口
        //2.前提：假设是那些已登录平台的客户在调用，在线客户
        //todo 3.考虑给未登录、未开通的客户也给与一定次数的调用
        User loginUser = userService.getLoginUser(request);
        String accessKey = loginUser.getAccessKey();
        String secretKey = loginUser.getSecretKey();
        XianYuOpenApiClient xianYuOpenApiClient = new XianYuOpenApiClient(accessKey, secretKey);
        //todo 4.调用指定id的模拟接口（暂时调用固定模拟接口getUserNameByPost来代替）
        String userRequestParams = openApiInvokeRequest.getUserRequestParams();
        Gson gson = new Gson();
        com.xianyu.xianyuopenapiclientsdk.model.User user = gson.fromJson(userRequestParams, com.xianyu.xianyuopenapiclientsdk.model.User.class);
        String result = xianYuOpenApiClient.getUserNameByPost(user);
        return ResultUtils.success(result);
    }
}
