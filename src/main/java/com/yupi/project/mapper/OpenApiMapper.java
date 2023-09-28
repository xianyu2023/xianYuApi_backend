package com.yupi.project.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xianyu.xianyucommon.model.entity.OpenApi;

import java.util.Date;
import java.util.List;

/**
* @author happyxianfish
* @description 针对表【open_api(开放接口信息)】的数据库操作Mapper
* @createDate 2023-07-21 00:08:20
* @Entity com.yupi.project.model.entity.OpenApi
*/
public interface OpenApiMapper extends BaseMapper<OpenApi> {

    /**用于logstash
     * 查询接口列表（包括已被删除的数据）
     */
    List<OpenApi> listOpenApiWithDelete(Date minUpdateTime);

}




