package com.xianyu.xianyucommon.service;

import com.xianyu.xianyucommon.model.entity.OpenApi;

/**
* @author happyxianfish
* @description 针对表【open_api(开放接口信息)】的数据库操作Service
* @createDate 2023-07-21 00:08:21
*/
public interface InnerOpenApiService{

    /**
     * 根据接口的url和请求方法获取接口
     * @param url
     * @param method
     * @return
     */
   OpenApi getInvokeOpenApi(String url,String method);

    /**
     * 根据id获取接口
     * @param apiId
     * @return
     */
    OpenApi getInvokeOpenApiById(String apiId);
}
