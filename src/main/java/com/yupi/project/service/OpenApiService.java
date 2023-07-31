package com.yupi.project.service;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xianyu.xianyucommon.model.entity.OpenApi;

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
}
