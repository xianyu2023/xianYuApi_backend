package com.yupi.project.service;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xianyu.xianyucommon.model.entity.OpenApi;
import com.xianyu.xianyucommon.model.entity.User;
import com.xianyu.xianyucommon.model.vo.OpenApiVO;
import com.yupi.project.model.dto.openApiRequest.OpenApiInvokeRequest;
import com.yupi.project.model.dto.openApiRequest.OpenApiQueryRequest;


import javax.servlet.http.HttpServletRequest;

/**
* @author happyxianfish
* @description 针对表【open_api(开放接口信息)】的数据库操作Service
* @createDate 2023-07-21 00:08:21
*/
public interface OpenApiService extends IService<OpenApi> {

    /**
     * 校验
     *
     * @param openApi
     * @param add 是否为创建校验
     */
    void validOpenApi(OpenApi openApi, boolean add);


    /**
     * 根据id校验接口
     *
     * @param id
     * @return
     */
    boolean judgeApiById(Long id);

    /**
     * 已登录+在线测试
     *
     * @param openApiInvokeRequest
     * @param openApi
     * @param loginUser
     * @return
     */
    String invokeApiByOnline(OpenApiInvokeRequest openApiInvokeRequest, OpenApi openApi, User loginUser);

    /**
     * 客户端SDK调用接口或者在线未登录测试
     *
     * @param openApiInvokeRequest
     * @param request
     * @return
     */
    String invokeApiBySdk(OpenApiInvokeRequest openApiInvokeRequest, HttpServletRequest request, OpenApi openApi);


    /**
     * 从 ES 查询
     *
     * @param openApiQueryRequest
     * @return
     */
    Page<OpenApi> searchFromEsBySearchText(OpenApiQueryRequest openApiQueryRequest);



    /**
     * 分页获取接口VO封装
     * @param openApiPage
     * @return
     */
    Page<OpenApiVO> getOpenApiVOPage(Page<OpenApi> openApiPage);
}
